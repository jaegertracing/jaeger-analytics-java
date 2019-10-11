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
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * Number of services in each trace? (What is the maximum/average service depth)
 * Number of operations/spans in each service?
 */
public class GraphDSLExamples {

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
    by.forEachRemaining(map -> {
      System.out.println(map.keySet());
      map.values().forEach(o -> {
        List<Vertex> vertices = (List<Vertex>) o;
        vertices.forEach(vv -> printVertex(vv));
      });
    });

    // Are two spans connected?
    // Are two spans with given name connected? The real graph is root-child-child2
    // Limitation is that it finds spans with shortest connection, e.g. span with "child2" name after span with "child2" will not be found
    traversal = graph.traversal(TraceTraversalSource.class)
        .hasName("root")
        .repeat(__.out())
        .until(__.hasName("child2"));
    System.out.println("\nIs root span connected with (tag/operation name)?");
    traversal.forEachRemaining(v -> printVertex(v));

    // Distance between two spans?
    // First we need to find out whether two spans are connected, then return path
    TraceTraversal<Vertex, Path> pathTraversal = graph.traversal(TraceTraversalSource.class)
        .hasName("root")
        .repeat(__.out()).until(__.hasName("child2"))
        .path();
    System.out.println("\nWhat is the path between two spans?");
    pathTraversal.clone().forEachRemaining(path -> System.out.println(path.size()));
    pathTraversal.forEachRemaining(path -> {
      printPath(path);
    });

    //maximum depth
    TraceTraversal<Vertex, Comparable> maxDepth = graph.traversal(TraceTraversalSource.class).V()
        .repeat(__.in()).emit().path().count(Scope.local).max();
    System.out.println("\nMax graph depth");
    maxDepth.forEachRemaining(comparable -> System.out.println(comparable));
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

