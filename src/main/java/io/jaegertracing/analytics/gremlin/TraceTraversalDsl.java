package io.jaegertracing.analytics.gremlin;

import io.opentracing.References;
import java.util.function.Predicate;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

@GremlinDsl(traversalSource = "io.jaegertracing.analytics.gremlin.TraceTraversalSourceDsl")
public interface TraceTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

  default GraphTraversal<S, Vertex> trace(int traceId) {
    return (GraphTraversal<S, Vertex>) has(Keys.TRACE_ID, traceId);
  }

  default GraphTraversal<S, Vertex> hasName(String name) {
    return (GraphTraversal<S, Vertex>) has(Keys.OPERATION_NAME, name);
  }

  default GraphTraversal<S, Vertex> hasTag(String key) {
    return (GraphTraversal<S, Vertex>) has(key);
  }

  default GraphTraversal<S, Vertex> hasTag(String key, Object value) {
    return (GraphTraversal<S, Vertex>) has(key, value);
  }

  default GraphTraversal<S, Vertex> startTime(Predicate<Integer> p) {
    return (GraphTraversal<S, Vertex>) has(Keys.START_TIME, p);
  }

  default GraphTraversal<S, Vertex> duration(Predicate<Integer> p) {
    return (GraphTraversal<S, Vertex>) has(Keys.DURATION, p);
  }

  default GraphTraversal<S, Vertex> child() {
    return out(References.CHILD_OF);
  }

  default GraphTraversal<S, Vertex> rootSpan() {
    return (GraphTraversal<S, Vertex>) not(__.inE());
  }

  default GraphTraversal<S, Vertex> leafSpan() {
    return (GraphTraversal<S, Vertex>) not(__.outE());
  }
}
