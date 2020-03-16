package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.model.Span;
import io.opentracing.tag.Tags;
import io.prometheus.client.Counter;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * This class calculates number of errors.
 *
 * @author Pavol Loffay
 */
public class NumberOfErrors implements ModelRunner {

  private static final Counter counter = Counter.build()
      .name("network_latency_seconds")
      .help("Network latency between client and server span")
      .labelNames("service", "operation")
      .create()
      .register();


  @Override
  public void runWithMetrics(Graph graph) {
    Set<Span> errorSpans = calculate(graph);
    for (Span span: errorSpans) {
      counter.labels(span.serviceName, span.operationName).inc();
    }
  }

  /**
   * @return error spans within a graph.
   */
  public static Set<Span> calculate(Graph graph) {
    TraceTraversal<Vertex, Vertex> errorSpansTraversal = graph.traversal(TraceTraversalSource.class)
        .hasTag(Tags.ERROR.getKey(), "true");

    Set<Span> result = new LinkedHashSet<>();
    while (errorSpansTraversal.hasNext()) {
      Vertex errorSpanVertex = errorSpansTraversal.next();
      Span errorSpan = GraphCreator.toSpan(errorSpanVertex);
      result.add(errorSpan);
    }

    return result;
  }
}
