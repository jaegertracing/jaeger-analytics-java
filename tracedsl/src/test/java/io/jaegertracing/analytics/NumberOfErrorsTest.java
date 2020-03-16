package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.Trace;
import io.opentracing.tag.Tags;
import java.util.Arrays;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class NumberOfErrorsTest {

  @Test
  public void testEmpty() {
    Assert.assertEquals(0, NumberOfErrors.calculate(GraphCreator.create(new Trace())).size());
  }

  @Test
  public void testCalculate() {
    Span root = Util.newTrace("root", "root");
    root.tags.put(Tags.ERROR.getKey(), "true");

    Span child = Util.newChild("root", "child", root);
    child.tags.put(Tags.ERROR.getKey(), "false");

    Span childChild = Util.newChild("root", "child", child);
    child.tags.put(Tags.ERROR.getKey(), "true");

    Trace trace = new Trace();
    trace.spans.addAll(Arrays.asList(root, child, childChild));
    Set<Span> errorSpans = NumberOfErrors.calculate(GraphCreator.create(trace));
    Assert.assertEquals(2, errorSpans.size());
  }
}
