package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.Trace;
import java.util.Arrays;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class TraceHeightTest {

  @Test
  public void calculateEmpty() {
    int height = TraceHeight.calculate(GraphCreator.create(new Trace()));
    Assert.assertEquals(0, height);
  }

  @Test
  public void calculateOnlyRoot() {
    Span root = Util.newTrace("root", "root");
    root.serviceName = "root";
    Trace trace = new Trace();
    trace.spans = Arrays.asList(root);
    Graph graph = GraphCreator.create(trace);
    int height = TraceHeight.calculate(graph);
    Assert.assertEquals(0, height);
  }

  @Test
  public void calculate() {
    Span root = Util.newTrace("root", "root");

    Span child = Util.newChild("child", "child", root);
    Span child2 = Util.newChild("child2", "child2", root);
    Span childChild = Util.newChild("childChild", "childChild", child);

    Trace trace = new Trace();
    trace.spans = Arrays.asList(root, child, child2, childChild);
    Graph graph = GraphCreator.create(trace);
    int height = TraceHeight.calculate(graph);
    Assert.assertEquals(2, height);
  }
}
