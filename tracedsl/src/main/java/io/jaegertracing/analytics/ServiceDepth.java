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
 * Maximum number of service changes from root to leaf.
 *
 * @author Pavol Loffay
 */
public class ServiceDepth implements ModelRunner {

  private static final Summary SERVICE_DEPTH_SUMMARY = Summary.build()
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
      .labelNames("service")
      .help("Service depth - number of service hops from a service to the root service")
      .register();

  @Override
  public void runWithMetrics(Graph graph) {
    Map<String, Integer> depths = calculate(graph);
    for (Map.Entry<String, Integer> entry: depths.entrySet()) {
      SERVICE_DEPTH_SUMMARY.labels(entry.getKey())
          .observe(entry.getValue());
    }
  }

  public static Map<String, Integer> calculate(Graph graph) {
    // get all leafs and go from them to parents
    TraceTraversal<Vertex, Vertex> leafs = graph.traversal(TraceTraversalSource.class)
        .leafSpans();

    // service to depth
    List<Map<String, Integer>> depths = new ArrayList<>();
    Set<String> serviceNames = new HashSet<>();

    while (leafs.hasNext()) {
      Map<String, Integer> branchDepths = new LinkedHashMap<>();
      depths.add(branchDepths);

      Vertex node = leafs.next();
      Vertex parent = Util.parent(node);
      Span nodeSpan = GraphCreator.toSpan(node);
      branchDepths.put(nodeSpan.operationName, 0);
      serviceNames.add(nodeSpan.serviceName);

      while (parent != null) {
        Span parentSpan = GraphCreator.toSpan(parent);
        if (!nodeSpan.serviceName.equals(parentSpan.serviceName)) {
          for (String service: branchDepths.keySet()) {
            Integer d = branchDepths.get(service);
            branchDepths.put(service, d + 1);
          }
          // increment all nodes
        }
        Integer parentDepth = branchDepths.get(parentSpan.serviceName);
        if (parentDepth == null) {
          branchDepths.put(parentSpan.serviceName, 0);
          serviceNames.add(parentSpan.serviceName);
        }

        node = parent;
        nodeSpan = GraphCreator.toSpan(node);
        parent = Util.parent(parent);
      }
    }

    Map<String, Integer> result = new LinkedHashMap<>();
    for (String serviceName: serviceNames) {
      Integer depth = 0;
      for (Map<String, Integer> map: depths) {
        Integer d = map.get(serviceName);
        if (d != null && d > depth) {
          depth = d;
        }
      }
      result.put(serviceName, depth);
    }
    return result;
  }
}
