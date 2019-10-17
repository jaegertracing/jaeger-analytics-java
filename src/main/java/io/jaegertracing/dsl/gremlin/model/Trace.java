package io.jaegertracing.dsl.gremlin.model;

import java.io.Serializable;
import java.util.Collection;

public class Trace implements Serializable {
    public String traceId;
    public Collection<Span> spans;
}
