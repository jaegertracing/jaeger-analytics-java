package io.jaegertracing.dsl.gremlin.ui.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = TraceStdDeserializer.class)
public class TraceMixin {
}
