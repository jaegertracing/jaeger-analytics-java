package io.jaegertracing.analytics.spark;

import com.google.protobuf.ByteString;
import com.google.protobuf.ByteString.ByteIterator;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.api_v2.Model;
import io.jaegertracing.api_v2.Model.KeyValue;
import io.jaegertracing.api_v2.Model.SpanRef;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;

public class ProtoSpanDeserializer implements Deserializer<Span>, Serializable {

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
  }

  @Override
  public Span deserialize(String topic, byte[] data) {
   try {
      Model.Span protoSpan = Model.Span.parseFrom(data);
      return fromProto(protoSpan);
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalArgumentException("could not deserialize to span", e);
    }
  }

  @Override
  public void close() {
  }

  private Span fromProto(Model.Span protoSpan) {
    Span span = new Span();
    span.traceId = asHexString(protoSpan.getTraceId());
    span.spanId = asHexString(protoSpan.getSpanId());
    span.operationName = protoSpan.getOperationName();
    span.serviceName = protoSpan.getProcess().getServiceName();
    span.startTimeMicros = Timestamps.toMicros(protoSpan.getStartTime());
    span.durationMicros = Durations.toMicros(protoSpan.getDuration());
    addTags(span, protoSpan.getTagsList());
    addTags(span, protoSpan.getProcess().getTagsList());
    if (protoSpan.getReferencesList().size() > 0) {
      SpanRef reference = protoSpan.getReferences(0);
      if (asHexString(reference.getTraceId()).equals(span.traceId)) {
        span.parentId = asHexString(protoSpan.getReferences(0).getSpanId());
      }
    }
    return span;
  }

  private static final String HEXES = "0123456789ABCDEF";

  private String asHexString(ByteString id) {
    ByteIterator iterator = id.iterator();
    StringBuilder out = new StringBuilder();
    while (iterator.hasNext()) {
      byte b = iterator.nextByte();
      out.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
    }
    return out.toString();
  }

  private void addTags(Span span, List<KeyValue> tags) {
    for (KeyValue kv : tags) {
      if (!Model.ValueType.STRING.equals(kv.getVType())) {
        continue;
      }
      String value = kv.getVStr();
      if (value != null) {
        span.tags.put(kv.getKey(), value);
      }
    }
  }
}
