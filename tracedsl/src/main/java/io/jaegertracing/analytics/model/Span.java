package io.jaegertracing.analytics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
    public List<Log> logs = new ArrayList<>();

    public static class Log {
        public long timestamp;
        public Map<String, String> fields = new LinkedHashMap<>();
    }
}
