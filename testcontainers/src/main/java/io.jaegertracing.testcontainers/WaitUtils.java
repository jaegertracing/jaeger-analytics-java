package io.jaegertracing.testcontainers;

import io.jaegertracing.api_v2.Model.KeyValue;
import io.jaegertracing.api_v2.Model.Span;
import io.jaegertracing.api_v2.Query.FindTracesRequest;
import io.jaegertracing.api_v2.Query.SpansResponseChunk;
import io.jaegertracing.api_v2.Query.TraceQueryParameters;
import io.jaegertracing.api_v2.QueryServiceGrpc.QueryServiceBlockingStub;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;

/**
 * @author Pavol Loffay
 */
public class WaitUtils {

  private WaitUtils() {}

  public static void untilQueryHasTag(QueryServiceBlockingStub queryService, String service, String tagKey, String tagValue) {
    TraceQueryParameters queryParameters = TraceQueryParameters.newBuilder().setServiceName(service).build();

    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
      Iterator<SpansResponseChunk> traces = queryService
          .findTraces(FindTracesRequest.newBuilder().setQuery(queryParameters).build());

      while (traces.hasNext()) {
        SpansResponseChunk trace = traces.next();
        for (Span span: trace.getSpansList()) {
          for (KeyValue tag: span.getTagsList()) {
            if (tagKey.equals(tag.getKey()) && tagValue.equals(tag.getVStr())) {
              return true;
            }
          }
        }
      }
      return false;
    });
  }

}
