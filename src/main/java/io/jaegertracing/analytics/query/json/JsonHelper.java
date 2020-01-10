package io.jaegertracing.analytics.query.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jaegertracing.analytics.model.Trace;
import io.jaegertracing.analytics.query.JsonSpan;
import io.jaegertracing.analytics.query.KeyValue;
import io.jaegertracing.analytics.query.Reference;

/**
 * @author Pavol Loffay
 */
public class JsonHelper {

  public static ObjectMapper configure(ObjectMapper objectMapper) {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.addMixIn(Trace.class, TraceMixin.class);
    objectMapper.addMixIn(JsonSpan.class, JsonSpanMixin.class);
    objectMapper.addMixIn(KeyValue.class, KeyValueMixin.class);
    objectMapper.addMixIn(Reference.class, ReferenceMixin.class);
    return objectMapper;
  }
}
