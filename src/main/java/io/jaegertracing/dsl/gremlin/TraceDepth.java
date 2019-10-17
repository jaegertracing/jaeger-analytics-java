package io.jaegertracing.dsl.gremlin;

import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
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

  public static void calculate(Graph graph) {
    TraceTraversal<Vertex, Comparable> maxDepth = graph.traversal(TraceTraversalSource.class).V()
        .repeat(__.in()).emit().path().count(Scope.local).max();
    maxDepth.forEachRemaining(depth -> {
      int depthInt = Integer.valueOf(depth.toString());
      System.out.println(depthInt);
      TRACE_DEPTH_HISTOGRAM.observe(depthInt);
      TRACE_DEPTH_SUMMARY.observe(depthInt);
      TRACE_DEPTH_SUMMARY.observe(depthInt);
    });
  }
}
