package io.jaegertracing.analytics;

import io.jaegertracing.analytics.NetworkLatency.Name;
import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.Trace;
import io.opentracing.tag.Tags;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class NetworkLatencyTest {

  @Test
  public void calculate() {
    Span root = Util.newTrace("root", "root");

    Span child = Util.newChild("gandalf", "child", root);
    child.startTimeMicros = 10000000;
    child.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

    Span childChild = Util.newChild("frodo", "childChild", child);
    childChild.startTimeMicros = 15000000;
    childChild.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);

    // simulates another server span
    Span childChild2 = Util.newChild("frodo2", "childChild2", child);
    childChild2.startTimeMicros = 20000000;
    childChild2.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);

    Span child2 = Util.newChild("gandalf", "child2", root);
    child2.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    child2.startTimeMicros = 20000000;

    Span child3 = Util.newChild("gandalf", "child3", root);
    child3.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    child3.startTimeMicros = 25000000;

    // simulates another server span
    Span childChild3 = Util.newChild("faramir", "childChild3", child3);
    childChild3.startTimeMicros = 50000000;
    childChild3.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);

    Trace trace = new Trace();
    trace.spans = Arrays.asList(root, child, child2, child3, childChild, childChild2 ,childChild3);

    Graph graph = GraphCreator.create(trace);
    Map<Name, Set<Double>> results = NetworkLatency.calculate(graph);
    Assert.assertEquals(3, results.size());
    Set<Double> latencies = results.get(new Name("gandalf", "frodo"));
    Assert.assertEquals(1, latencies.size());
    Assert.assertEquals(5, latencies.iterator().next(), 0.0);

    latencies = results.get(new Name("gandalf", "frodo2"));
    Assert.assertEquals(1, latencies.size());
    Assert.assertEquals(10, latencies.iterator().next(), 0.0);

    latencies = results.get(new Name ("gandalf", "faramir"));
    Assert.assertEquals(1, latencies.size());
    Assert.assertEquals(25, latencies.iterator().next(), 0.0);
  }

  @Test
  public void nameEqualsAndHashCode() {
    Name fooBar = new Name("foo", "bar");
    Name fooBar2 = new Name("foo", "bar");
    Assert.assertEquals(fooBar, fooBar2);
    Assert.assertEquals(fooBar.hashCode(), fooBar2.hashCode());
    Name barFoo = new Name("bar", "foo");
    Assert.assertNotEquals(fooBar.hashCode(), barFoo.hashCode());
  }
}
