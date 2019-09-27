package io.jaegertracing.dsl.gremlin;

import io.opentracing.References;
import io.opentracing.tag.Tags;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class Main {

  public static void main(String[] args) {
    TinkerGraph graph = TinkerGraph.open();
    TestData.initData(graph);

    // has tag and it is root span and duration is greater than 2
    TraceTraversal<Vertex, Vertex> traversal = graph.traversal(TraceTraversalSource.class)
        .hasTag(Tags.SPAN_KIND.getKey())
        .rootSpan()
        .duration(P.gt(2));
    Vertex vertex = traversal.next();
    printVertex(vertex);
    dfs(vertex, v -> printVertex(v));

    // client span and duration is gt
    traversal = graph.traversal(TraceTraversalSource.class)
        .hasTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
        .duration(P.gt(120));
    System.out.println("--------");
    traversal.forEachRemaining(v -> printVertex(v));

    // client span and duration is gt X group by traceId
    TraceTraversal<Vertex, Map<Object, Object>> by = graph.traversal(TraceTraversalSource.class)
        .hasTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
        .duration(P.gt(120))
        .group()
        .by(Keys.TRACE_ID);
    System.out.println("--------");
    by.forEachRemaining(v -> {
      System.out.println(v.keySet());
      System.out.println(v.values());
    });
  }

  // Depth first search, we could have a variant with BiConsumer and pass node and parent
  public static void dfs(Vertex root, Consumer<Vertex> vertexConsumer) {
    vertexConsumer.accept(root);
    Iterator<Edge> edges = root.edges(Direction.OUT, References.CHILD_OF);
    while (edges.hasNext()) {
      Edge edge = edges.next();
      dfs(edge.inVertex(), vertexConsumer);
    }
  }

  public static List<Vertex> descendants(Vertex vertex) {
    Iterator<Edge> edges = vertex.edges(Direction.OUT, References.CHILD_OF);
    List<Vertex> vertices = new ArrayList<>();
    while (edges.hasNext()) {
      Edge edge = edges.next();
      vertices.add(edge.inVertex());
    }
    return Collections.unmodifiableList(vertices);
  }

  public static void printVertex(Vertex vertex) {
    String operation = vertex.property(Keys.OPERATION_NAME) != null ? String.valueOf(vertex.property(Keys.OPERATION_NAME).value()) : "null";
    String traceId = vertex.property(Keys.TRACE_ID) != null ? String.valueOf(vertex.property(Keys.TRACE_ID).value()) : "null";
    String spanId = vertex.property(Keys.SPAN_ID) != null ? String.valueOf(vertex.property(Keys.SPAN_ID).value()) : "null";
    System.out.printf("%s[%s/%s], tags = %s\n", operation, traceId, spanId, vertex.keys());
  }
}

