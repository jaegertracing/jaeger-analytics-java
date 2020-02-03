package io.jaegertracing.analytics.gremlin;

import io.jaegertracing.analytics.query.Reference;
import io.opentracing.References;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
 */
public class Util {

  private Util() {}

  public static TraceTraversalSource traceTraversal(Graph graph) {
    return graph.traversal(TraceTraversalSource.class);
  }

  // Depth first search, we could have a variant with BiConsumer and pass node and parent
  public static void dfs(Vertex node, Consumer<Vertex> vertexConsumer) {
    vertexConsumer.accept(node);
    Iterator<Edge> edges = node.edges(Direction.OUT, References.CHILD_OF);
    while (edges.hasNext()) {
      Edge edge = edges.next();
      dfs(edge.inVertex(), vertexConsumer);
    }
  }

  public static void dfs(Vertex node, BiConsumer<Vertex, Vertex> vertexConsumer) {
    Iterator<Edge> edges = node.edges(Direction.OUT, References.CHILD_OF);
    if (!edges.hasNext()) {
      vertexConsumer.accept(node, null);
    }
    while (edges.hasNext()) {
      Edge edge = edges.next();
      vertexConsumer.accept(node, edge.inVertex());
      dfs(edge.inVertex(), vertexConsumer);
    }
  }

  public static Vertex parent(Vertex vertex) {
    Iterator<Edge> edges = vertex.edges(Direction.IN, References.CHILD_OF);
    if (!edges.hasNext()) {
      return null;
    }
    return edges.next().outVertex();
  }

  public static List<Vertex> children(Vertex vertex) {
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
    System.out.printf("%s[%s:%s], tags = %s\n", operation, traceId, spanId, vertex.keys());
  }

  public static void printPath(Path path) {
    System.out.printf("Size=%d\n", path.size());
    Iterator<Object> iterator =  path.iterator();
    while (iterator.hasNext()) {
      Vertex vertex = (Vertex) iterator.next();
      printVertex(vertex);
      if (iterator.hasNext()) {
        System.out.println("|");
      }
    }
  }
}
