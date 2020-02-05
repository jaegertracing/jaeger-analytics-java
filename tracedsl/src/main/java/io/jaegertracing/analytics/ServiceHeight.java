package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.prometheus.client.Summary;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Maximum number of service changes from root to leaf.
 *
 * @author Pavol Loffay
 */
public class ServiceHeight implements ModelRunner {

  private static final Summary TRACE_SERVICE_HEIGHT_SUMMARY = Summary.build()
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
      .name("service_height_total")
      .help("Service height - maximum number of service changes from root to leaf")
      .register();

  @Override
  public void runWithMetrics(Graph graph) {
    int maxServiceDepth = calculate(graph);
    TRACE_SERVICE_HEIGHT_SUMMARY.observe(maxServiceDepth);
  }

  public static int calculate(Graph graph) {
    int height = 0;
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
      if (branchDepth > height) {
        height = branchDepth;
      }
    }
    return height;
  }

}
