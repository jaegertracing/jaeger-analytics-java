package io.jaegertracing.analytics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Trace implements Serializable {
    public String traceId;
    public Collection<Span> spans = new ArrayList<>();
}
