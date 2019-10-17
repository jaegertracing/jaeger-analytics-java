package io.jaegertracing.dsl.gremlin.ui.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.jaegertracing.dsl.gremlin.ui.KeyValue;
import java.io.IOException;

/**
 * @author Pavol Loffay
 */
public class KeyValueStdDeserializer extends StdDeserializer<KeyValue> {

  // TODO Spark incorrectly serializes object mapper, therefore reinitializing here
  private ObjectMapper objectMapper = JsonHelper.configure(new ObjectMapper());

  public KeyValueStdDeserializer() {
    super(KeyValue.class);
  }

  @Override
  public KeyValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode node = objectMapper.getFactory().setCodec(objectMapper).getCodec().readTree(jp);

    String key = node.get("key").asText();
    String type = node.get("type").asText();

    KeyValue keyValue = new KeyValue();
    keyValue.setKey(key);
    keyValue.setValueType(type);

    JsonNode value = node.get("value");
    keyValue.setValueString(value.asText());

    return keyValue;
  }
}
