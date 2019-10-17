package io.jaegertracing.dsl.gremlin.ui.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Pavol Loffay
 */
@JsonDeserialize(using = KeyValueStdDeserializer.class)
public class KeyValueMixin {
}
