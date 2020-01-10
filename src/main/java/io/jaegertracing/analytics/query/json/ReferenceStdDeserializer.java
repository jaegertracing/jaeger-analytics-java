package io.jaegertracing.analytics.query.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.jaegertracing.analytics.query.Reference;
import java.io.IOException;

/**
 * @author Pavol Loffay
 */
public class ReferenceStdDeserializer extends StdDeserializer<Reference> {

  private ObjectMapper objectMapper = JsonHelper.configure(new ObjectMapper());

  protected ReferenceStdDeserializer() {
    super(Reference.class);
  }

  @Override
  public Reference deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode node = objectMapper.getFactory().setCodec(objectMapper).getCodec().readTree(jp);

    String spanIdHex = node.get("spanID").asText();

    Reference reference = new Reference();
    reference.setSpanId(spanIdHex);
    return reference;
  }
}
