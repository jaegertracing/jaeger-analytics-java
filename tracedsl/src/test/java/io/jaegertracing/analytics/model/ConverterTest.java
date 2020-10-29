package io.jaegertracing.analytics.model;

import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import io.jaegertracing.api_v2.Model;
import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

/**
 * @author Pavol Loffay
 */
public class ConverterTest {

  @Test
  public void testId() {
    String id = "2cb1f0c274c3b4a5";
    ByteString protoId = Converter.toProtoId(id);
    Assert.assertEquals(id, Converter.toStringId(protoId));
  }

  @Test
  public void testConvertDuration() {
    Model.Span protoSpan = Model.Span.newBuilder()//
      .setDuration(Duration.newBuilder().setSeconds(2).setNanos((int) TimeUnit.MILLISECONDS.toNanos(154)).build())//
      .build();

    Span convertedSpan = Converter.toModel(protoSpan);

    Assert.assertEquals(2154000L, convertedSpan.durationMicros);
  }
  
  @Test
  public void testConvertStarttime() {
    Model.Span protoSpan = Model.Span.newBuilder()//
      .setStartTime(Timestamp.newBuilder().setSeconds(1_350).setNanos(1_391_000)) //
      .build();

    Span convertedSpan = Converter.toModel(protoSpan);

    Assert.assertEquals(1_350_001_391L, convertedSpan.startTimeMicros);
  }
  
  @Test
  public void testConvertLogTimestamp() {
    Model.Span protoSpan = Model.Span.newBuilder()//
      .addLogs(Model.Log.newBuilder().setTimestamp(Timestamp.newBuilder().setSeconds(3_512).setNanos(2_913_000))) //
      .build();

    Span convertedSpan = Converter.toModel(protoSpan);

    Assert.assertEquals(3_512_002_913L, convertedSpan.logs.get(0).timestamp);
  }
}
