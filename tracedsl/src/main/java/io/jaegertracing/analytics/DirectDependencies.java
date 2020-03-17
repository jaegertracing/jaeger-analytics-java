package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import io.prometheus.client.Summary;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
 */
public class DirectDependencies implements ModelRunner {

  private static final Summary DEPENDENCIES_SUMMARY = Summary.build()
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
      .name("service_dependencies_total")
      .labelNames("service")
      .help("Service dependencies - number of services a service directly calls")
      .register();

  private static final Summary PARENTS_SUMMARY = Summary.build()
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
      .name("service_parents_total")
      .labelNames("service")
      .help("Service parents - number of services directly calling a service")
      .register();

  @Override
  public void runWithMetrics(Graph graph) {
    Result result = calculate(graph);
    for (Map.Entry<String, Set<String>> entry: result.dependencies.entrySet()) {
      DEPENDENCIES_SUMMARY.labels(entry.getKey()).observe(entry.getValue().size());
    }
    for (Map.Entry<String, Set<String>> entry: result.parents.entrySet()) {
      PARENTS_SUMMARY.labels(entry.getKey()).observe(entry.getValue().size());
    }
  }

  public static Result calculate(Graph graph) {
    TraceTraversal<Vertex, Vertex> leafs = graph.traversal(TraceTraversalSource.class).leafSpans();

    // service to its dependencies
    Map<String, Set<String>> dependencies = new LinkedHashMap<>();
    // service to its parents
    Map<String, Set<String>> parents = new LinkedHashMap<>();

    while (leafs.hasNext()) {
      Vertex node = leafs.next();
      Vertex parent = Util.parent(node);
      Span nodeSpan = GraphCreator.toSpan(node);

      while (parent != null) {
        Span parentSpan = GraphCreator.toSpan(parent);
        if (!nodeSpan.serviceName.equals(parentSpan.serviceName)) {
          Set<String> d = dependencies.get(parentSpan.serviceName);
          if (d == null) {
            d = new LinkedHashSet<>();
            dependencies.put(parentSpan.serviceName, d);
          }
          d.add(nodeSpan.serviceName);

          Set<String> p = parents.get(nodeSpan.serviceName);
          if (p == null) {
            p = new LinkedHashSet<>();
            parents.put(nodeSpan.serviceName, p);
          }
          p.add(parentSpan.serviceName);
        }

        node = parent;
        nodeSpan = GraphCreator.toSpan(node);
        parent = Util.parent(parent);
      }
    }
    return new Result(Collections.unmodifiableMap(dependencies), Collections.unmodifiableMap(parents));
  }

  public static class Result {
    public final Map<String, Set<String>> dependencies;
    public final Map<String, Set<String>> parents;

    public Result(Map<String, Set<String>> deps, Map<String, Set<String>> parents) {
      this.dependencies = deps;
      this.parents = parents;
    }
  }
}
