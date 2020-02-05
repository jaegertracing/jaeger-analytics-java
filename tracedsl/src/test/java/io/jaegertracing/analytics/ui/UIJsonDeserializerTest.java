package io.jaegertracing.analytics.ui;

import static org.junit.Assert.assertEquals;

import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.Trace;
import io.jaegertracing.analytics.query.json.JsonSpanDeserializer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class UIJsonDeserializerTest {

  @Test
  public void deserialize() throws IOException {
    File file = new File(getClass().getClassLoader().getResource("ui-trace.json").getFile());
    String json = new Scanner(file, "UTF-8").useDelimiter("\\A").next();

    Trace trace = JsonSpanDeserializer.deserialize(json.getBytes());
    List<Span> spans = new ArrayList<>(trace.spans);
    assertEquals(2, spans.size());
    assertEquals("/api/services/{service}/operations", spans.get(0).operationName);
    assertEquals("jaeger-query", spans.get(0).serviceName);
    assertEquals(8, spans.get(0).tags.size());
  }
}
