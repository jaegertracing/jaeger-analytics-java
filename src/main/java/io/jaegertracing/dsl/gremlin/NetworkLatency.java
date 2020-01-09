package io.jaegertracing.dsl.gremlin;

import static java.util.stream.Collectors.toList;

import io.opentracing.tag.Tags;
import java.util.List;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
 */
public class NetworkLatency {

  public static final String HOSTNAME_TAG = "hostname";

  public static void calculate(Graph graph) {
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
          Long networkTime = serverStartTime - clientStartTime;
          System.out.printf("Network time of %s:%s=%d", serviceA, serviceB, networkTime);
        }
      }
    });
  }
}
