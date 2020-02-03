package io.jaegertracing.analytics.tracequality;

import io.jaegertracing.analytics.Util;
import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.Trace;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class UniqueSpanIdTest {

  @Test
  public void empty() {
    Map<String, List<Span>> result = UniqueSpanId.computeScore(GraphCreator.create(new Trace()));
    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void uniqueSpan() {
    Span root = Util.newTrace("foo", "op");
    Span child = Util.newChild("foo", "op2", root);
    Span child2 = Util.newChild("foo", "op23", root);
    child.spanId = root.spanId;

    Trace trace = new Trace();
    trace.spans = Arrays.asList(root, child, child2);
    Graph graph = GraphCreator.create(trace);

    Map<String, List<Span>> result = UniqueSpanId.computeScore(graph);
    Assert.assertEquals(2, result.size());
    Assert.assertEquals(2, result.get(root.spanId).size());
    Assert.assertEquals(1, result.get(child2.spanId).size());
  }
}
