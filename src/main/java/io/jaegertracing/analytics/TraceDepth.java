package io.jaegertracing.analytics;

import static java.util.stream.Collectors.*;

import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.__;
import io.prometheus.client.Summary;
import java.util.List;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
 *
 * {@link TraceDepth} calculates the trace depth based on number of spans.
 * The maximum distance between a leaf node and the root.
 */
public class TraceDepth implements ModelRunner {

  private static final Summary TRACE_DEPTH_SUMMARY = Summary.build()
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
      .name("trace_depth_total")
      .help("Trace depth")
      .register();

  public void runWithMetrics(Graph graph) {
    int depth = calculate(graph);
    TRACE_DEPTH_SUMMARY.observe(depth);
  }

  public static int calculate(Graph graph) {
    TraceTraversal<Vertex, Comparable> maxDepth = graph.traversal(TraceTraversalSource.class).V()
        .repeat(__.in()).emit().path().count(Scope.local).max();
    List<Integer> depths = maxDepth.toStream().map(depth -> {
      int depthInt = Integer.valueOf(depth.toString());
      return depthInt;
    }).collect(toList());
    return depths.size() > 0 ? depths.get(0) - 1 : 0;
  }
}
