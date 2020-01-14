package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.Keys;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.opentracing.tag.Tags;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Child;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Network latency between client and server spans. Name contains service names.
 *
 * @author Pavol Loffay
 */
public class NetworkLatency implements ModelRunner {

  private static final Histogram histogram = Histogram.build()
      .name("network_latency_seconds")
      .help("Network latency between client and server span")
      .labelNames("client", "server")
      .create()
      .register();

  public void runWithMetrics(Graph graph) {
    Map<Name, Set<Double>> latencies = calculate(graph);
    for (Map.Entry<Name, Set<Double>> entry: latencies.entrySet()) {
      Child child = histogram.labels(entry.getKey().client, entry.getKey().server);
      for (Double latency: entry.getValue()) {
        child.observe(latency);
      }
    }
  }

  public static Map<Name, Set<Double>> calculate(Graph graph) {
    Map<Name, Set<Double>> results = new LinkedHashMap<>();

    TraceTraversal<Vertex, Vertex> clientSpans = graph
        .traversal(TraceTraversalSource.class).V()
        .hasTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

    clientSpans.forEachRemaining(client -> {
      String clientService = (String)client.property(Keys.SERVICE_NAME).value();
      for (Vertex child : Util.descendants(client)) {
        if (child.property(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).isPresent()) {
          String serverService = (String)child.property(Keys.SERVICE_NAME).value();
          Long clientStartTime = (Long)client.property(Keys.START_TIME).value();
          Long serverStartTime = (Long)child.property(Keys.START_TIME).value();
          Long latency = serverStartTime - clientStartTime;

          Name name = new Name(clientService, serverService);
          Set<Double> latencies = results.get(name);
          if (latencies == null) {
            latencies = new LinkedHashSet<>();
            results.put(name, latencies);
          }
          latencies.add(latency/(1000.0*1000.0));
        }
      }
    });

    return results;
  }

  public static class Name {
    public final String client;
    public final String server;

    public Name(String client, String server) {
      this.client = client;
      this.server = server;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Name name = (Name) o;
      return client.equals(name.client) &&
          server.equals(name.server);
    }

    @Override
    public int hashCode() {
      return Objects.hash(client, server);
    }

    @Override
    public String toString() {
      return "Name{" +
          "client='" + client + '\'' +
          ", server='" + server + '\'' +
          '}';
    }
  }
}
