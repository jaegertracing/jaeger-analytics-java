package io.jaegertracing.analytics.model;

import com.google.protobuf.ByteString;
import io.jaegertracing.api_v2.Model;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Pavol Loffay
 */
public class Converter {
  private Converter() {}

  public static Trace toModel(List<Model.Span> spanList) {
    // gRPC query service returns list of spans not the trace object
    Trace trace = new Trace();
    for (Model.Span protoSpan: spanList) {
      if (trace.traceId == null || trace.traceId.isEmpty()) {
        trace.traceId = toStringId(protoSpan.getTraceId());
      }
      trace.spans.add(toModel(protoSpan));
    }
    return trace;
  }

  public static Span toModel(Model.Span protoSpan) {
    Span span = new Span();
    span.spanId = toStringId(protoSpan.getSpanId());
    span.traceId = toStringId(protoSpan.getTraceId());
    if (protoSpan.getReferencesList().size() > 0) {
      span.parentId = toStringId(protoSpan.getReferencesList().get(0).getSpanId());
    }

    span.serviceName = protoSpan.getProcess().getServiceName();
    span.operationName = protoSpan.getOperationName();
    span.startTimeMicros = protoSpan.getStartTime().getNanos() / 1000;
    span.durationMicros = protoSpan.getDuration().getNanos() / 1000;

    span.tags = new LinkedHashMap<>();
    for (Model.KeyValue keyValue: protoSpan.getTagsList()) {
      switch (keyValue.getVType()) {
        case STRING:
      }
      span.tags.put(keyValue.getKey(), toStringValue(keyValue));
    }
    return span;
  }

  private static String toStringValue(Model.KeyValue keyValue) {
    switch (keyValue.getVType()) {
      case STRING:
        return keyValue.getVStr();
      case BOOL:
        return Boolean.toString(keyValue.getVBool());
      case INT64:
        return Long.toString(keyValue.getVInt64());
      case FLOAT64:
        return Double.toString(keyValue.getVFloat64());
      case BINARY:
        return keyValue.getVBinary().toStringUtf8();
      case UNRECOGNIZED:
      default:
        return "unrecognized";
    }
  }

  public static String toStringId(ByteString id) {
    return new BigInteger(1, id.toByteArray()).toString(16);
  }

  public static ByteString toProtoId(String id) {
    return ByteString.copyFrom(new BigInteger(id, 16).toByteArray());
  }
}
