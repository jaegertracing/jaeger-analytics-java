package io.jaegertracing.dsl.gremlin;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

@GremlinDsl(traversalSource = "io.jaegertracing.dsl.gremlin.TraceTraversalSourceDsl")
public interface TraceTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

  default GraphTraversal<S, Vertex> trace(int traceId) {
    return (GraphTraversal<S, Vertex>) has(Keys.TRACE_ID, traceId);
  }

  default GraphTraversal<S, Vertex> hasTag(String key) {
    return (GraphTraversal<S, Vertex>) has(key);
  }

  default GraphTraversal<S, Vertex> hasTag(String key, Object value) {
    return (GraphTraversal<S, Vertex>) has(key, value);
  }


//  /**
//   * Filters objects by the "person" label. This step is designed to work with incoming vertices.
//   */
//  @GremlinDsl.AnonymousMethod(returnTypeParameters = {"A", "A"}, methodTypeParameters = {"A"})
//  default GraphTraversal<S, E> person() {
//    return hasLabel(Keys.SPAN_TYPE);
//  }
}
