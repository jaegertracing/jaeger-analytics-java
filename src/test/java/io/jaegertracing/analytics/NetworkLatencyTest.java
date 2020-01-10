package io.jaegertracing.analytics;

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
    Span root = Util.newTrace("root");
    root.serviceName = "root";

    Span child = Util.newChild("child", root);
    child.startTimeMicros = 10;
    child.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    child.serviceName = "gandalf";

    Span childChild = Util.newChild("childChild", child);
    childChild.startTimeMicros = 15;
    childChild.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
    childChild.serviceName = "frodo";

    // simulates another server span
    Span childChild2 = Util.newChild("childChild2", child);
    childChild2.startTimeMicros = 20;
    childChild2.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
    childChild2.serviceName = "frodo2";

    Span child2 = Util.newChild("child2", root);
    child2.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    child2.serviceName = "gandalf";
    child2.startTimeMicros = 20;

    Span child3 = Util.newChild("child3", root);
    child3.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    child3.serviceName = "gandalf";
    child3.startTimeMicros = 25;

    // simulates another server span
    Span childChild3 = Util.newChild("childChild3", child3);
    childChild3.startTimeMicros = 50;
    childChild3.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
    childChild3.serviceName = "faramir";

    Trace trace = new Trace();
    trace.spans = Arrays.asList(root, child, child2, child3, childChild, childChild2 ,childChild3);

    Graph graph = GraphCreator.create(trace);
    Map<String, Set<Long>> results = NetworkLatency.calculate(graph);
    Assert.assertEquals(3, results.size());
    Set<Long> latencies = results.get(NetworkLatency.getName("gandalf", "frodo"));
    Assert.assertEquals(1, latencies.size());
    Assert.assertEquals(5, (long)latencies.iterator().next());

    latencies = results.get(NetworkLatency.getName("gandalf", "frodo2"));
    Assert.assertEquals(1, latencies.size());
    Assert.assertEquals(10, (long)latencies.iterator().next());

    latencies = results.get(NetworkLatency.getName("gandalf", "faramir"));
    Assert.assertEquals(1, latencies.size());
    Assert.assertEquals(25, (long)latencies.iterator().next());
  }
}
