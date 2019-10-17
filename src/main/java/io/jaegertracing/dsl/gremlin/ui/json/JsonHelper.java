package io.jaegertracing.dsl.gremlin.ui.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jaegertracing.dsl.gremlin.model.Trace;
import io.jaegertracing.dsl.gremlin.ui.JsonSpan;
import io.jaegertracing.dsl.gremlin.ui.KeyValue;
import io.jaegertracing.dsl.gremlin.ui.Reference;

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
