package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import io.prometheus.client.Summary;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
 */
public class ServiceHeight implements ModelRunner {

  private static final Summary SERVICE_HEIGHT_SUMMARY = Summary.build()
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
      .labelNames("service")
      .help("Service height - number of service hops from a service to the leaf service")
      .register();

  @Override
  public void runWithMetrics(Graph graph) {
    Map<String, Integer> depths = calculate(graph);
    for (Map.Entry<String, Integer> entry: depths.entrySet()) {
      SERVICE_HEIGHT_SUMMARY.labels(entry.getKey())
          .observe(entry.getValue());
    }
  }

  public static Map<String, Integer> calculate(Graph graph) {
    // get all leafs and go from them to parents
    TraceTraversal<Vertex, Vertex> leafs = graph.traversal(TraceTraversalSource.class)
        .leafSpans();

    // service to height
    Map<String, Integer> heights = new LinkedHashMap<>();

    while (leafs.hasNext()) {
      int height = 0;

      Vertex node = leafs.next();
      Vertex parent = Util.parent(node);
      Span nodeSpan = GraphCreator.toSpan(node);

      Integer heightNode = heights.get(nodeSpan.serviceName);
      if (heightNode == null) {
        heights.put(nodeSpan.serviceName, height);
      }

      while (parent != null) {
        Span parentSpan = GraphCreator.toSpan(parent);
        if (!nodeSpan.serviceName.equals(parentSpan.serviceName)) {
          height++;
        }

        heightNode = heights.get(parentSpan.serviceName);
        if (heightNode == null || height > heightNode) {
          heights.put(parentSpan.serviceName, height);
        }

        node = parent;
        nodeSpan = GraphCreator.toSpan(node);
        parent = Util.parent(parent);
      }
    }

    return heights;
  }
}
