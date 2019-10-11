package io.jaegertracing.dsl.gremlin;

import io.jaegertracing.dsl.gremlin.model.Span;
import io.jaegertracing.dsl.gremlin.model.Trace;
import io.opentracing.References;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

public class GraphCreator {

  public static Graph create(Trace trace) {
    TinkerGraph graph = TinkerGraph.open();

    // create vertices
    Map<String, Vertex> vertexMap = new LinkedHashMap<>();
    for (Span span: trace.spans) {
      Vertex vertex = graph.addVertex(Keys.SPAN_TYPE);
      vertexMap.put(span.spanId, vertex);

      vertex.property(Keys.TRACE_ID, span.traceId);
      vertex.property(Keys.SPAN_ID, span.spanId);
      if (span.parentId != null) {
        vertex.property(Keys.PARENT_ID, span.parentId);
      }
      vertex.property(Keys.START_TIME, span.startTimeMicros);
      vertex.property(Keys.DURATION, span.durationMicros);
      vertex.property(Keys.SERVICE_NAME, span.serviceName);
      vertex.property(Keys.OPERATION_NAME, span.operationName);
      span.tags.entrySet().forEach(stringStringEntry -> {
        vertex.property(stringStringEntry.getKey(), stringStringEntry.getValue());
      });
    }

    for (Span span: trace.spans) {
      Vertex vertex = vertexMap.get(span.spanId);
      if (span.parentId != null) {
        Vertex parent = vertexMap.get(span.parentId);
        if (parent != null) {
          parent.addEdge(References.CHILD_OF, vertex);
        }
      }
    }

    return graph;
  }
}
