package io.jaegertracing.dsl.gremlin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class TraceTraversalSourceDsl extends GraphTraversalSource {

  public TraceTraversalSourceDsl(Graph graph,
      TraversalStrategies traversalStrategies) {
    super(graph, traversalStrategies);
  }

  public TraceTraversalSourceDsl(Graph graph) {
    super(graph);
  }

  public TraceTraversalSourceDsl(
      RemoteConnection connection) {
    super(connection);
  }

  public GraphTraversal<Vertex, Vertex> trace(int traceId) {
    GraphTraversal traversal = this.clone().V();
    return traversal.has(Keys.TRACE_ID, traceId);
  }

  public GraphTraversal<Vertex, Vertex> service(String service) {
    GraphTraversal traversal = this.clone().V();
    traversal = traversal.has(Keys.SERVICE_NAME, service);
    return traversal;
  }

  public GraphTraversal<Vertex, Vertex> hasTag(String key) {
    GraphTraversal traversal = this.clone().V();
    traversal = traversal.has(key);
    return traversal;
  }

  public GraphTraversal<Vertex, Vertex> hasTag(String key, Object value) {
    GraphTraversal traversal = this.clone().V();
    traversal = traversal.has(key, value);
    return traversal;
  }

  public GraphTraversal<Vertex, Vertex> hasTagWithValues(String key, Object... values) {
    GraphTraversal traversal = this.clone().V();
    traversal = traversal.has(key, P.within(values));
    return traversal;
  }

  public GraphTraversal<Vertex, Vertex> hasAnyTag(String... keys) {
    GraphTraversal traversal = this.clone().V();
    List<GraphTraversal> traversals = new ArrayList<>(keys.length);
    for (String key: keys) {
      traversals.add(this.clone().V().has(key));
    }
    return traversal.union(traversals.toArray(new GraphTraversal[0]));
  }

  public GraphTraversal<Vertex, Vertex> startTime(Predicate<Integer> p) {
    GraphTraversal traversal = this.clone().V();
    traversal = traversal.has(Keys.START_TIME, p);
    return traversal;
  }

  public GraphTraversal<Vertex, Vertex> duration(Predicate<Integer> p) {
    GraphTraversal traversal = this.clone().V();
    traversal = traversal.has(Keys.DURATION, p);
    return traversal;
  }

  // Two spans are connected (tag, tag)
  // Distance between two spans
  // What is the maximum/average service depth
}
