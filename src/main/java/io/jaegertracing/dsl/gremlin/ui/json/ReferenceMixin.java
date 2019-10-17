package io.jaegertracing.dsl.gremlin.ui.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Pavol Loffay
 */
@JsonDeserialize(using = ReferenceStdDeserializer.class)
public class ReferenceMixin {
}
