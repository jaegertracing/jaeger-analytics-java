package io.jaegertracing.dsl.gremlin;

import io.jaegertracing.dsl.gremlin.model.Trace;
import io.jaegertracing.dsl.gremlin.ui.JaegerQueryService;
import java.io.IOException;

/**
 * @author Pavol Loffay
 */
public class JaegerUITest {

//  @Test
  public void load() throws IOException {
    Trace load = JaegerQueryService.load("2db17286b3e6b148",
        "https://simple-streaming-default.apps.ploffay-cluster1.devcluster.openshift.com/api/traces/");
  }
}
