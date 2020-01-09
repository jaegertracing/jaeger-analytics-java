package io.jaegertracing.dsl.gremlin.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Span implements Serializable {
    public String traceId;
    public String spanId;
    // TODO add references array
    public String parentId;
    public String serviceName;
    public String operationName;
    public long startTimeMicros;
    public long durationMicros;
    public Map<String, String> tags = new HashMap<>();
}
