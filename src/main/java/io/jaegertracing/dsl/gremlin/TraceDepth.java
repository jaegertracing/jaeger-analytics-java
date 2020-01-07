package io.jaegertracing.dsl.gremlin;

import static java.util.stream.Collectors.*;

import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
 *
 * {@link TraceDepth} calculates the trace depth based on number of spans.
 */
public class TraceDepth {

  private static final Histogram TRACE_DEPTH_HISTOGRAM = Histogram.build()
      .linearBuckets(1, 1, 15)
      .name("trace_depth_histogram")
      .help("Trace graph depth")
      .register();

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
      .name("trace_depth_summary")
      .help("Trace graph depth")
      .register();

  private TraceDepth() {}

  public static void calculateWithMetrics(Graph graph) {
    int depth = calculate(graph);
    TRACE_DEPTH_HISTOGRAM.observe(depth);
    TRACE_DEPTH_SUMMARY.observe(depth);
    TRACE_DEPTH_SUMMARY.observe(depth);
  }

  public static int calculate(Graph graph) {
    TraceTraversal<Vertex, Comparable> maxDepth = graph.traversal(TraceTraversalSource.class).V()
        .repeat(__.in()).emit().path().count(Scope.local).max();
    List<Integer> depths = maxDepth.toStream().map(depth -> {
      int depthInt = Integer.valueOf(depth.toString());
      return depthInt;
    }).collect(toList());
    return depths.size() > 0 ? depths.get(0) : 0;
  }
}
