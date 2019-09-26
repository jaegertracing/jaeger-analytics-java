package io.jaegertracing.dsl.gremlin;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class App {

  public static void main(String[] args) {
    TinkerGraph graph = TinkerGraph.open();
    TestData.initData(graph);

    TraceTraversalSource traceSource = graph.traversal(TraceTraversalSource.class);
    TraceTraversal<Vertex, Vertex> spans = traceSource.hasTag("foo")
        .trace(1)
        .startTime(P.gt(1));

    for (Vertex v : spans.toList()) {
      System.out.println(v.label());
      System.out.println(v.property(Keys.OPERATION_NAME).value());
      System.out.println(v.keys());
    }
  }
}
