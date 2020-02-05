package io.jaegertracing.analytics.tracequality;

import io.jaegertracing.analytics.ModelRunner;
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

    private static final Counter counterClientTag = Counter.build()
        .name("trace_quality_client_tag_total")
        .help("The service emits spans with client span.kind")
        .labelNames("pass", "service")
        .create()
        .register();

    private static final Counter counterServerTag = Counter.build()
        .name("trace_quality_server_tag_total")
        .help("The service emits spans with server span.kind")
        .labelNames("pass", "service")
        .create()
        .register();

    @Override
    public void runWithMetrics(Graph graph) {
        Result result = computeScore(graph);
        for (Span span: result.missingServerTag) {
            counterServerTag.labels("false", span.serviceName).inc();
        }
        for (Span span: result.missingClientTag) {
            counterClientTag.labels("false", span.serviceName).inc();
        }
        for (Span span: result.hasClientTag) {
            counterClientTag.labels("true", span.serviceName).inc();
        }
        for (Span span: result.hasServerTag) {
            counterServerTag.labels("true", span.serviceName).inc();
        }
    }

    public static class Result {
        public List<Span> missingClientTag = new ArrayList<>();
        public List<Span> missingServerTag = new ArrayList<>();
        public List<Span> hasServerTag = new ArrayList<>();
        public List<Span> hasClientTag = new ArrayList<>();
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
                } else {
                    result.hasServerTag.add(childSpan);
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
            } else {
                result.hasClientTag.add(parentSpan);
            }
        }
        return result;
    }
}

