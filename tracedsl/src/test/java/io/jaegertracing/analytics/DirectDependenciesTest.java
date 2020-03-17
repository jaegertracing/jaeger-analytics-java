package io.jaegertracing.analytics;

import io.jaegertracing.analytics.DirectDependencies.Result;
import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.Trace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class DirectDependenciesTest {

  @Test
  public void calculateOnlyRoot() {
    Span root = Util.newTrace("root", "root");
    Trace trace = new Trace();
    trace.spans = Arrays.asList(root);
    Graph graph = GraphCreator.create(trace);

    Result result = DirectDependencies.calculate(graph);
    Assert.assertEquals(0, result.dependencies.size());
    Assert.assertEquals(0, result.parents.size());
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

    Result result = DirectDependencies.calculate(graph);

    Assert.assertEquals(3, result.dependencies.size());
    List<String> deps = new ArrayList<>(result.dependencies.get("root"));
    Collections.sort(deps);
    Assert.assertEquals(Arrays.asList("child", "child2"), deps);
    deps = new ArrayList<>(result.dependencies.get("child"));
    Collections.sort(deps);
    Assert.assertEquals(Arrays.asList("childChild"), deps);
    deps = new ArrayList<>(result.dependencies.get("child2"));
    Collections.sort(deps);
    Assert.assertEquals(Arrays.asList("child2ChildChild"), deps);

    Assert.assertEquals(4, result.parents.size());
    List<String> parents = new ArrayList<>(result.parents.get("child"));
    Collections.sort(deps);
    Assert.assertEquals(Arrays.asList("root"), parents);
    parents = new ArrayList<>(result.parents.get("childChild"));
    Collections.sort(deps);
    Assert.assertEquals(Arrays.asList("child"), parents);
    parents = new ArrayList<>(result.parents.get("child2"));
    Collections.sort(deps);
    Assert.assertEquals(Arrays.asList("root"), parents);
    parents = new ArrayList<>(result.parents.get("child2ChildChild"));
    Collections.sort(deps);
    Assert.assertEquals(Arrays.asList("child2"), parents);
  }
}
