package io.jaegertracing.analytics.gremlin;

import static io.jaegertracing.analytics.gremlin.Util.dfs;
import static io.jaegertracing.analytics.gremlin.Util.printPath;
import static io.jaegertracing.analytics.gremlin.Util.printVertex;

import io.opentracing.tag.Tags;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * Number of services in each trace? (What is the maximum/average service depth)
 * Number of operations/spans in each service?
 */
public class Examples {

  public static void main(String[] args) {
    Graph graph = TinkerGraph.open();
    ExampleTrace.initData(graph);

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

}

