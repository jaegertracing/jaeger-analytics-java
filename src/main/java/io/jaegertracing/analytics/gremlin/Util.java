package io.jaegertracing.analytics.gremlin;

import io.opentracing.References;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
 */
public class Util {

  private Util() {}

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
