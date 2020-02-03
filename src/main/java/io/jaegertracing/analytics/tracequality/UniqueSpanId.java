package io.jaegertracing.analytics.tracequality;

import io.jaegertracing.analytics.ModelRunner;
import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.Keys;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import io.prometheus.client.Counter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Pavol Loffay
 */
public class UniqueSpanId implements ModelRunner {

  private static final Counter counter = Counter.build()
      .name("trace_quality_unique_span_id_total")
      .help("The service emitted spans with unique span IDs")
      .labelNames("pass", "service")
      .create()
      .register();

  @Override
  public void runWithMetrics(Graph graph) {
    Map<String, List<Span>> result = computeScore(graph);
    for (Map.Entry<String, List<Span>> entry: result.entrySet()) {
      List<Span> spans = entry.getValue();
      boolean uniqueId = spans.size() == 1;
      for (Span span: entry.getValue()) {
        counter.labels(String.valueOf(uniqueId), span.serviceName).inc();
      }
    }
  }

  /**
   * @return List of spans per spanID
   */
  public static Map<String, List<Span>> computeScore(Graph graph) {
    TraceTraversal<Vertex, Map<Object, Object>> by = Util.traceTraversal(graph).V()
        .group().by(Keys.SPAN_ID);

    Map<String, List<Span>> result = new LinkedHashMap<>();
    while (by.hasNext()) {
      Map<Object, Object> map = by.next();
      for (Map.Entry<Object, Object> entry: map.entrySet()) {
          Collection<Vertex> vertices = (Collection<Vertex>) entry.getValue();
          String id = (String) entry.getKey();
          List<Span> spans = result.get(id);
          if (spans == null) {
            spans = new ArrayList<>(vertices.size());
            result.put(id, spans);
          }
          for (Vertex vertex: vertices) {
            Span span = GraphCreator.toSpan(vertex);
            spans.add(span);
          }
      }
    }
    return result;
  }
}
