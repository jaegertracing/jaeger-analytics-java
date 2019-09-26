package io.jaegertracing.dsl.gremlin;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

@GremlinDsl(traversalSource = "io.jaegertracing.dsl.gremlin.TraceTraversalSourceDsl")
public interface TraceTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

  default GraphTraversal<S, Vertex> traceId(int traceId) {
    return (GraphTraversal<S, Vertex>) has(Keys.TRACE_ID, traceId);
  }


//  /**
//   * Filters objects by the "person" label. This step is designed to work with incoming vertices.
//   */
//  @GremlinDsl.AnonymousMethod(returnTypeParameters = {"A", "A"}, methodTypeParameters = {"A"})
//  default GraphTraversal<S, E> person() {
//    return hasLabel(Keys.SPAN_TYPE);
//  }
}
