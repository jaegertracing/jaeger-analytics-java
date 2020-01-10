package io.jaegertracing.analytics.query.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Pavol Loffay
 */
@JsonDeserialize(using = JsonSpanStdDeserializer.class)
public class JsonSpanMixin {
}
