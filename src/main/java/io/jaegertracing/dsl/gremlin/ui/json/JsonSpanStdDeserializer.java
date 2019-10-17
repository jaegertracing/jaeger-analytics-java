package io.jaegertracing.dsl.gremlin.ui.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.jaegertracing.dsl.gremlin.ui.JsonSpan;
import io.jaegertracing.dsl.gremlin.ui.KeyValue;
import io.jaegertracing.dsl.gremlin.ui.Reference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Pavol Loffay
 */
public class JsonSpanStdDeserializer extends StdDeserializer<JsonSpan> {

  private ObjectMapper objectMapper = JsonHelper.configure(new ObjectMapper());

  protected JsonSpanStdDeserializer() {
    super(JsonSpan.class);
  }

  @Override
  public JsonSpan deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = objectMapper.getFactory().setCodec(objectMapper).getCodec().readTree(jp);
    JsonSpan span = new JsonSpan();

    JsonNode traceIdNode = node.get("traceID");
    span.traceId = objectMapper.treeToValue(traceIdNode, String.class);
    JsonNode spanIdNode = node.get("spanID");
    span.spanId = objectMapper.treeToValue(spanIdNode, String.class);
    List<Reference> references = deserializeReferences(node);
    if (references.size() > 0) {
      span.parentId = references.get(0).getSpanId();
    }

    JsonNode startTimeNode = node.get("startTime");
    span.startTimeMicros = objectMapper.treeToValue(startTimeNode, Long.class);
    JsonNode durationNode = node.get("duration");
    span.durationMicros = objectMapper.treeToValue(durationNode, Long.class);

    JsonNode tagsNode = node.get("tags");
    List<KeyValue> tags = Arrays.asList(objectMapper.treeToValue(tagsNode, KeyValue[].class));
    span.tags = new LinkedHashMap<>();
    for (KeyValue keyValue: tags) {
      span.tags.put(keyValue.getKey(), keyValue.getValueString());
    }

    JsonNode operationNameNode = node.get("operationName");
    span.operationName = objectMapper.treeToValue(operationNameNode, String.class);
    JsonNode processIdNode = node.get("processID");
    span.processId = objectMapper.treeToValue(processIdNode, String.class);

    return span;
  }

  private List<Reference> deserializeReferences(JsonNode node) throws JsonProcessingException {
    List<Reference> references = new ArrayList<>();
    JsonNode parentSpanID = node.get("parentSpanID");
    if (parentSpanID != null) {
      Reference reference = new Reference();
      reference.setSpanId(parentSpanID.asText());
      references.add(reference);
    }

    Reference[] referencesArr = objectMapper.treeToValue(node.get("references"), Reference[].class);
    references.addAll(Arrays.asList(referencesArr));
    return references;
  }

}
