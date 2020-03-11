package io.jaegertracing.analytics.ui;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.jaegertracing.analytics.model.Trace;
import io.jaegertracing.analytics.query.JaegerQueryService;
import io.jaegertracing.api_v2.Query.GetOperationsRequest;
import io.jaegertracing.api_v2.Query.GetOperationsResponse;
import io.jaegertracing.api_v2.QueryServiceGrpc;
import io.jaegertracing.api_v2.QueryServiceGrpc.QueryServiceBlockingStub;
import java.io.IOException;
import org.junit.Test;

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

//  @Test
  public void loadgRpc() throws InterruptedException {
    ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:16686").usePlaintext().build();
    QueryServiceBlockingStub blockingStub = QueryServiceGrpc.newBlockingStub(channel);
    GetOperationsResponse operations = blockingStub
        .getOperations(GetOperationsRequest.newBuilder().setService("jaeger-query").build());
    System.out.println(operations.getOperationsList());
  }
}
