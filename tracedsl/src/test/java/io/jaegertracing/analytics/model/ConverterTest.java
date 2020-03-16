package io.jaegertracing.analytics.model;

import com.google.protobuf.ByteString;
import org.junit.Assert;
import org.junit.Test;

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
}
