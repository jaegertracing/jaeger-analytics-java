package io.jaegertracing.analytics;

import static java.util.stream.Collectors.toList;

import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.__;
import io.prometheus.client.Summary;
import java.util.List;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Maximum number of spans from root to leaf.
 *
 * @author Pavol Loffay
 */
public class TraceHeight implements ModelRunner {

  private static final Summary TRACE_HEIGHT_SUMMARY = Summary.build()
      .quantile(0.1, 0.01)
      .quantile(0.2, 0.01)
      .quantile(0.3, 0.01)
      .quantile(0.4, 0.01)
      .quantile(0.5, 0.01)
      .quantile(0.6, 0.01)
      .quantile(0.7, 0.01)
      .quantile(0.8, 0.01)
      .quantile(0.9, 0.01)
      .quantile(0.99, 0.01)
      .name("trace_height_total")
      .help("Trace height - maximum number of spans from root to leaf")
      .register();

  public void runWithMetrics(Graph graph) {
    int height = calculate(graph);
    TRACE_HEIGHT_SUMMARY.observe(height);
  }

  public static int calculate(Graph graph) {
    TraceTraversal<Vertex, Comparable> maxHeight = graph.traversal(TraceTraversalSource.class).V()
        .repeat(__.in()).emit().path().count(Scope.local).max();
    List<Integer> heights = maxHeight.toStream()
        .map(height -> Integer.valueOf(height.toString()))
        .collect(toList());
    return heights.size() > 0 ? heights.get(0) - 1 : 0;
  }
}
