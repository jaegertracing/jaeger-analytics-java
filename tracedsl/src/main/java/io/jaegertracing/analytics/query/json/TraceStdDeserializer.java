package io.jaegertracing.analytics.query.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.Trace;
import io.jaegertracing.analytics.query.JsonSpan;
import io.jaegertracing.analytics.query.Process;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Pavol Loffay
 */
public class TraceStdDeserializer extends StdDeserializer<Trace> {

  private ObjectMapper objectMapper = JsonHelper.configure(new ObjectMapper());

  protected TraceStdDeserializer() {
    super(Trace.class);
  }

  @Override
  public Trace deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = objectMapper.getFactory().setCodec(objectMapper).getCodec().readTree(jp);
    Trace trace = new Trace();

    JsonNode spansNode = node.get("spans");
    trace.spans = Arrays.asList(objectMapper.treeToValue(spansNode, JsonSpan[].class));

    JsonNode processNode = node.get("processes");
    Map<String, Process> processMap = objectMapper.convertValue(processNode, new TypeReference<Map<String, Process>>() {});
    for (Span span: trace.spans) {
      JsonSpan jsonSpan = (JsonSpan) span;
      Process process = processMap.get(jsonSpan.processId);
      span.serviceName = process.getServiceName();
    }

    return trace;
  }
}
