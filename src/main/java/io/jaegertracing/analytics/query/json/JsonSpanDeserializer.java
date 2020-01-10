package io.jaegertracing.analytics.query.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jaegertracing.analytics.model.Trace;
import io.jaegertracing.analytics.query.RestResult;
import java.io.IOException;

/**
 * @author Pavol Loffay
 */
public class JsonSpanDeserializer {
  private static final ObjectMapper objectMapper = JsonHelper.configure(new ObjectMapper());

  public static Trace deserialize(byte[] json) throws IOException {
    RestResult<Trace> restResult = objectMapper.readValue(json, new TypeReference<RestResult<Trace>>() {});
    if (restResult.getData().size() > 0) {
      return restResult.getData().get(0);
    }
    return null;
  }
}
