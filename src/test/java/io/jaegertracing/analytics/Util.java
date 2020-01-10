package io.jaegertracing.analytics;

import io.jaegertracing.analytics.model.Span;
import java.util.UUID;

/**
 * @author Pavol Loffay
 */
public class Util {

  private Util() {}

  public static Span newTrace(String operationName) {
    Span span = new Span();
    span.operationName = operationName;
    span.spanId = UUID.randomUUID().toString();
    span.traceId = UUID.randomUUID().toString();
    return span;
  }

  public static Span newChild(String operationName, Span parent) {
    Span span = new Span();
    span.operationName = operationName;
    span.spanId = UUID.randomUUID().toString();
    span.traceId = parent.traceId;
    span.parentId = parent.spanId;
    return span;
  }
}
