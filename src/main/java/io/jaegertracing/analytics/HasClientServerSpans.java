package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import io.opentracing.tag.Tags;
import io.prometheus.client.Counter;
import java.util.ArrayList;
import java.util.List;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class HasClientServerSpans implements ModelRunner {

    private static final Counter counterMissingClientTag = Counter.build()
        .name("trace_quality_missing_client_tag_total")
        .help("The service didn't emit spans with client span.kind")
        .labelNames("service")
        .create()
        .register();

    private static final Counter counterMissingServerTag = Counter.build()
        .name("trace_quality_missing_server_tag_total")
        .help("The service didn't emit spans with server span.kind")
        .labelNames("service")
        .create()
        .register();

    @Override
    public void runWithMetrics(Graph graph) {
        Result result = computeScore(graph);
        for (Span span: result.missingServerTag) {
            counterMissingServerTag.labels(span.serviceName).inc();
        }
        for (Span span: result.missingClientTag) {
            counterMissingClientTag.labels(span.serviceName).inc();
        }
    }

    public static class Result {
        // TODO should be use multi set/list?
        // TODO in other words should we more penalize services reporting more spans with missing data?
        public List<Span> missingClientTag = new ArrayList<>();
        public List<Span> missingServerTag = new ArrayList<>();
    }

    public Result computeScore(Graph graph) {
        Result result = new Result();

        TraceTraversal<Vertex, Vertex> traversal = graph.traversal(TraceTraversalSource.class)
            .hasTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
        while (traversal.hasNext()) {
            Vertex vertex = traversal.next();
            List<Vertex> children = Util.children(vertex);
            if (children == null || children.isEmpty()) {
                Span clientSpan = GraphCreator.toSpan(vertex);
                String peerService = clientSpan.tags.get(Tags.PEER_SERVICE.getKey());
                if (peerService != null) {
                    // TODO this could be exported as a not instrumented service
                    Span span = new Span();
                    span.serviceName = peerService;
                    result.missingServerTag.add(span);
                }
                continue;
            }
            for (Vertex child : children) {
                Span childSpan = GraphCreator.toSpan(child);
                String spanKindTag = childSpan.tags.get(Tags.SPAN_KIND.getKey());
                if (!Tags.SPAN_KIND_SERVER.equals(spanKindTag)) {
                    result.missingServerTag.add(childSpan);
                }
            }
        }

        traversal = graph.traversal(TraceTraversalSource.class)
            .hasTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        while (traversal.hasNext()) {
            Vertex vertex = traversal.next();
            Vertex parent = Util.parent(vertex);
            if (parent == null) {
                Span serverSpan = GraphCreator.toSpan(vertex);
                String peerService = serverSpan.tags.get(Tags.PEER_SERVICE.getKey());
                if (peerService != null) {
                    // TODO this could be exported as a not instrumented service
                    Span span = new Span();
                    span.serviceName = peerService;
                    result.missingClientTag.add(span);
                }
                continue;
            }
            Span parentSpan = GraphCreator.toSpan(parent);
            String spanKindTag = parentSpan.tags.get(Tags.SPAN_KIND.getKey());
            if (!Tags.SPAN_KIND_CLIENT.equals(spanKindTag)) {
                result.missingClientTag.add(parentSpan);
            }
        }
        return result;
    }
}

