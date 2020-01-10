package io.jaegertracing.analytics.query.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = TraceStdDeserializer.class)
public class TraceMixin {
}
