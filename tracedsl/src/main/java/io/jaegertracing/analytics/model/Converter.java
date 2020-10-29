package io.jaegertracing.analytics.model;

import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import io.jaegertracing.api_v2.Model;
import io.jaegertracing.api_v2.Model.KeyValue;
import io.jaegertracing.api_v2.Model.Log;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

  public static Collection<Trace> toModelTraces(List<Model.Span> spanList) {
    Map<String, Trace> traces = new LinkedHashMap<>();
    for (Model.Span protoSpan: spanList) {
      Span span = toModel(protoSpan);
      Trace trace = traces.get(span.traceId);
      if (trace == null) {
        trace = new Trace();
        trace.traceId = span.traceId;
        traces.put(span.traceId, trace);
      }
      trace.spans.add(span);
    }
    return traces.values();
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
    span.startTimeMicros = timestampToMicros(protoSpan.getStartTime());
    span.durationMicros = durationToMicros(protoSpan.getDuration());

    span.tags = toMap(protoSpan.getTagsList());
    span.logs = new ArrayList<>();
    for (Log protoLog: protoSpan.getLogsList()) {
      Span.Log log = new Span.Log();
      log.timestamp = timestampToMicros(protoLog.getTimestamp());
      log.fields = toMap(protoLog.getFieldsList());
      span.logs.add(log);
    }

    return span;
  }

  private static long durationToMicros(Duration duration) {
    long nanos = TimeUnit.SECONDS.toNanos(duration.getSeconds());
    nanos += duration.getNanos();
    return TimeUnit.NANOSECONDS.toMicros(nanos);
  }

  private static long timestampToMicros(Timestamp timestamp) {
    long nanos = TimeUnit.SECONDS.toNanos(timestamp.getSeconds());
    nanos += timestamp.getNanos();
    return TimeUnit.NANOSECONDS.toMicros(nanos);
  }

  private static Map<String, String> toMap(List<KeyValue> tags) {
    Map<String, String> tagMap = new LinkedHashMap<>();
    for (Model.KeyValue keyValue: tags) {
      tagMap.put(keyValue.getKey(), toStringValue(keyValue));
    }
    return tagMap;
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
