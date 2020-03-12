package io.jaegertracing.analytics.ui;

import io.jaegertracing.analytics.model.Trace;
import io.jaegertracing.analytics.query.JaegerQueryService;
import java.io.IOException;

/**
 * @author Pavol Loffay
 */
public class JaegerUITest {

//  @Test
  // TODO use test containers
  public void load() throws IOException {
//    Trace trace2 = JaegerQueryService.load("54e26ad4bbea6606", "http://192.168.122.1:16686/api/traces");
    Trace tracae = JaegerQueryService.load("54e26ad4bbea6606",
        "http://192.168.122.1:16686/api/traces/");
    System.out.println(tracae);
  }
}
