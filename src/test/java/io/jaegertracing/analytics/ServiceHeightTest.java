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
public class ServiceHeightTest {

  @Test
  public void calculateOnlyRoot() {
    Span root = io.jaegertracing.analytics.Util.newTrace("root", "root");
    Trace trace = new Trace();
    trace.spans = Arrays.asList(root);
    Graph graph = GraphCreator.create(trace);

    int serviceHeight = ServiceHeight.calculate(graph);
    Assert.assertEquals(0, serviceHeight);
  }

  @Test
  public void calculate() {
    Span root = io.jaegertracing.analytics.Util.newTrace("root", "root");
    Span child = io.jaegertracing.analytics.Util.newChild("child", "child", root);
    Span child2 = io.jaegertracing.analytics.Util.newChild("child2", "child2", root);
    Span child2Child = io.jaegertracing.analytics.Util.newChild("child2", "child2Child", child2);
    Span child2ChildChild = io.jaegertracing.analytics.Util.newChild("child2ChildChild", "child2ChildChild", child2Child);
    Span childChild = Util.newChild("childChild", "childChild", child);
    Span childChildChild = Util.newChild("childChild", "childChildChild", childChild);

    Trace trace = new Trace();
    trace.spans = Arrays.asList(root, child, child2, childChild, child2Child, childChildChild, child2ChildChild);
    Graph graph = GraphCreator.create(trace);

    int serviceHeight = ServiceHeight.calculate(graph);
    Assert.assertEquals(2, serviceHeight);
  }
}
