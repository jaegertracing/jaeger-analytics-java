package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * The maximum service depth.
 *
 * The depth of a node is the number of edges from the node to the tree's root node.
 * A root node has a depth of 0.
 *
 * @author Pavol Loffay
 */
public class ServiceDepth implements ModelRunner {


  private static final Summary TRACE_SERVICE_DEPTH_SUMMARY = Summary.build()
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
      .name("service_depth_total")
      .help("Service depth")
      .register();

  @Override
  public void runWithMetrics(Graph graph) {
    int maxServiceDepth = calculate(graph);
    TRACE_SERVICE_DEPTH_SUMMARY.observe(maxServiceDepth);
  }

  public static int calculate(Graph graph) {
    int depth = 0;
    // get all leafs and go from them to parents
    TraceTraversal<Vertex, Vertex> leafs = graph.traversal(TraceTraversalSource.class)
        .leafSpans();

    while (leafs.hasNext()) {
      Vertex leaf = leafs.next();
      Vertex parent = Util.parent(leaf);

      int branchDepth = 0;
      while (parent != null) {
        if (!GraphCreator.toSpan(parent).serviceName.equals(GraphCreator.toSpan(leaf).serviceName)) {
          branchDepth++;
        }
        leaf = parent;
        parent = Util.parent(parent);
      }
      if (branchDepth > depth) {
        depth = branchDepth;
      }
    }
    return depth;
  }

}
