package io.jaegertracing.dsl.gremlin;

import static io.jaegertracing.dsl.gremlin.Util.newChild;
import static io.jaegertracing.dsl.gremlin.Util.newTrace;

import io.jaegertracing.dsl.gremlin.model.Span;
import io.jaegertracing.dsl.gremlin.model.Trace;
import io.opentracing.tag.Tags;
import java.util.Arrays;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class NetworkLatencyTest {

  @Test
  public void calculate() {
    Span root = newTrace("root");
    root.serviceName = "root";

    Span child = newChild("child", root);
    child.startTimeMicros = 10;
    child.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    child.serviceName = "gandalf";

    Span childChild = newChild("childChild", child);
    childChild.startTimeMicros = 15;
    childChild.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
    childChild.serviceName = "frodo";

    Span child2 = newChild("child2", root);
    child2.tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    child2.serviceName = "gandalf";
    child2.startTimeMicros = 20;

    Trace trace = new Trace();
    trace.spans = Arrays.asList(root, child, child2, childChild);

    Graph graph = GraphCreator.create(trace);
    NetworkLatency.calculate(graph);
  }
}
