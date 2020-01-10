package io.jaegertracing.dsl.gremlin;

import io.opentracing.tag.Tags;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
 */
public class NetworkLatency {

  public static Map<String, Set<Long>> calculate(Graph graph) {
    Map<String, Set<Long>> results = new LinkedHashMap<>();

    TraceTraversal<Vertex, Vertex> clientSpans = graph
        .traversal(TraceTraversalSource.class).V()
        .hasTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

    clientSpans.forEachRemaining(client -> {
      String serviceA = (String)client.property(Keys.SERVICE_NAME).value();
      for (Vertex child : GraphDSLExamples.descendants(client)) {
        if (child.property(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).isPresent()) {
          String serviceB = (String)child.property(Keys.SERVICE_NAME).value();
          Long clientStartTime = (Long)client.property(Keys.START_TIME).value();
          Long serverStartTime = (Long)child.property(Keys.START_TIME).value();
          Long latency = serverStartTime - clientStartTime;

          String name = getName(serviceA, serviceB);
          Set<Long> latencies = results.get(name);
          if (latencies == null) {
            latencies = new LinkedHashSet<>();
            results.put(name, latencies);
          }
          latencies.add(latency);
        }
      }
    });

    return results;
  }

  public static String getName(String serviceA, String serviceB) {
    return String.format("%s:%s", serviceA, serviceB);
  }
}
