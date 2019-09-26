package io.jaegertracing.dsl.gremlin;

import io.opentracing.tag.Tags;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class App {

  public static void main(String[] args) {
    TinkerGraph graph = TinkerGraph.open();
    TestData.initData(graph);

    TraceTraversalSource traceSource = graph.traversal(TraceTraversalSource.class);
    GraphTraversal<Vertex, Vertex> spans = traceSource
        .hasTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
        .duration(P.gt(100));

    for (Vertex v : spans.toList()) {
      System.out.println(v.label());
      System.out.println(v.property(Keys.OPERATION_NAME).value());
      System.out.println(v.keys());
    }
  }
}
