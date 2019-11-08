package io.jaegertracing.dsl.gremlin;

import io.jaegertracing.dsl.gremlin.model.Trace;
import io.jaegertracing.dsl.gremlin.ui.JaegerQueryService;
import java.io.IOException;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class JaegerUITest {

  @Test
  public void load() throws IOException {
//    Trace trace2 = JaegerQueryService.load("54e26ad4bbea6606", "http://192.168.122.1:16686/api/traces");
    Trace tracae = JaegerQueryService.load("54e26ad4bbea6606",
        "http://192.168.122.1:16686/api/traces/");
    System.out.println(tracae);
  }
}
