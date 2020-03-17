package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.Trace;
import java.util.Arrays;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class ServiceHeightTest {

  @Test
  public void calculateOnlyRoot() {
    Span root = Util.newTrace("root", "root");
    Trace trace = new Trace();
    trace.spans = Arrays.asList(root);
    Graph graph = GraphCreator.create(trace);

    Map<String, Integer> depths = ServiceHeight.calculate(graph);
    Assert.assertEquals(1, depths.size());
    Assert.assertEquals(new Integer(0), depths.get("root"));
  }

  @Test
  public void calculate() {
    Span root = Util.newTrace("root", "root");
    Span child = Util.newChild("child", "child", root);
    Span child2 = Util.newChild("child2", "child2", root);
    Span child2Child = Util.newChild("child2", "child2Child", child2);
    Span child2ChildChild = Util.newChild("child2ChildChild", "child2ChildChild", child2Child);
    Span childChild = Util.newChild("childChild", "childChild", child);
    Span childChildChild = Util.newChild("childChild", "childChildChild", childChild);

    Trace trace = new Trace();
    trace.spans = Arrays.asList(root, child, child2, childChild, child2Child, childChildChild, child2ChildChild);
    Graph graph = GraphCreator.create(trace);

    Map<String, Integer> depths = ServiceHeight.calculate(graph);
    Assert.assertEquals(5, depths.size());
    Assert.assertEquals(new Integer(2), depths.get("root"));
    Assert.assertEquals(new Integer(1), depths.get("child"));
    // 1 because child2 contains internal spans
    Assert.assertEquals(new Integer(1), depths.get("child2"));
    Assert.assertEquals(new Integer(0), depths.get("childChild"));
    Assert.assertEquals(new Integer(0), depths.get("child2ChildChild"));
  }
}
