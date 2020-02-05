package io.jaegertracing.analytics.gremlin;

import io.jaegertracing.analytics.Util;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.Trace;
import java.util.Arrays;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class TraceTraversalDslTest {

  @Test
  public void testLeafSpans_onlyRoot() {
    Span root = Util.newTrace("root", "root");

    Trace trace = new Trace();
    trace.spans = Arrays.asList(root);
    Graph graph = GraphCreator.create(trace);

    GraphTraversal<Vertex, Vertex> traversal = graph
        .traversal(io.jaegertracing.analytics.gremlin.TraceTraversalSource.class).leafSpans();
    Assert.assertEquals(1, (long)traversal.count().next());
  }

  @Test
  public void testLeafSpans() {
    Span root = Util.newTrace("root", "root");
    Span child = Util.newChild("child", "child", root);
    Span child2 = Util.newChild("child2", "child2", root);
    Span childChild = Util.newChild("childChild", "childChild", child);

    Trace trace = new Trace();
    trace.spans = Arrays.asList(root, child, child2, childChild);
    Graph graph = GraphCreator.create(trace);

    GraphTraversal<Vertex, Vertex> traversal = graph
        .traversal(io.jaegertracing.analytics.gremlin.TraceTraversalSource.class).leafSpans();
    Assert.assertEquals(2, (long)traversal.count().next());
  }
}
