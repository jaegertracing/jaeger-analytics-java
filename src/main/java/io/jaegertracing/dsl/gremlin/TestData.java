package io.jaegertracing.dsl.gremlin;

import io.opentracing.References;
import io.opentracing.tag.Tags;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class TestData {
  private TestData() {}

  public static void initData(Graph graph) {
    Vertex rootSpan = graph.addVertex(Keys.SPAN_TYPE);
    rootSpan.property(Keys.SPAN_ID, 0);
    rootSpan.property(Keys.TRACE_ID, 1);
    rootSpan.property(Keys.OPERATION_NAME, "root_span");
    rootSpan.property(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    rootSpan.property(Keys.START_TIME, 22);
    rootSpan.property(Keys.DURATION, 15);

    Vertex childSpan = graph.addVertex(Keys.SPAN_TYPE);
    childSpan.property(Keys.SPAN_ID, 1);
    childSpan.property(Keys.TRACE_ID, 1);
    childSpan.property(Keys.OPERATION_NAME, "child");
    childSpan.property(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
    childSpan.property(Keys.START_TIME, 33);
    childSpan.property(Keys.DURATION, 22);
    childSpan.property("foo", "bar");
    rootSpan.addEdge(References.CHILD_OF, childSpan);

    Vertex childSpan2 = graph.addVertex(Keys.SPAN_TYPE);
    childSpan2.property(Keys.SPAN_ID, 2);
    childSpan2.property(Keys.TRACE_ID, 1);
    childSpan2.property(Keys.OPERATION_NAME, "child2");
    childSpan2.property(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
    childSpan2.property(Keys.DURATION, 255);
    childSpan2.property("baz", "kar");
    childSpan.addEdge(References.FOLLOWS_FROM, childSpan2);

    Vertex orphan = graph.addVertex(Keys.SPAN_TYPE);
    orphan.property(Keys.SPAN_ID, 3);
    orphan.property(Keys.TRACE_ID, 2);
    orphan.property(Keys.OPERATION_NAME, "orphan");
    orphan.property("foo", "server");
  }
}
