package io.jaegertracing.dsl.gremlin.model;

import java.util.Collection;

public class Trace {
    public String traceId;
    public Collection<Span> spans;
}
