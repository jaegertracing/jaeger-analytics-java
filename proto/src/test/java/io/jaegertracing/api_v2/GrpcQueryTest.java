package io.jaegertracing.api_v2;

import grpc.gateway.protoc_gen_swagger.options.Openapiv2.SecurityScheme.In;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.jaegertracing.api_v2.Query.GetOperationsRequest;
import io.jaegertracing.api_v2.Query.GetOperationsResponse;
import io.jaegertracing.api_v2.QueryServiceGrpc.QueryServiceBlockingStub;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.JaegerTracer.Builder;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.spi.Reporter;
import io.jaegertracing.spi.Sender;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import io.opentracing.Tracer;
import java.util.Collections;
import java.util.Set;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

/**
 * @author Pavol Loffay
 */
public class GrpcQueryTest {

  private static final String SERVICE_NAME = "query-test";
  private static final String OPERATION_NAME = "someoperation";

  private static final int JAEGER_QUERY_PORT = 16686;
  private static final int JAEGER_COLLECTOR_THRIFT = 14268;

  private JaegerAllInOne jaeger = new JaegerAllInOne("jaegertracing/all-in-one:latest");
  private Tracer tracer;

  @Before
  public void before() {
    jaeger.start();
    tracer = jaeger.getTracer(SERVICE_NAME);
  }

  @After
  public void after() {
    jaeger.stop();
    tracer.close();
  }

  @Test
  public void testGetService() {
    tracer.buildSpan(OPERATION_NAME).start().finish();
    tracer.close();

    ManagedChannel channel = ManagedChannelBuilder.forTarget(String.format("localhost:%d", jaeger.getQueryPort())).usePlaintext().build();
    QueryServiceBlockingStub blockingStub = QueryServiceGrpc.newBlockingStub(channel);
    GetOperationsResponse operations = blockingStub
        .getOperations(GetOperationsRequest.newBuilder().setService(SERVICE_NAME).build());

    Assert.assertEquals(1, operations.getOperationNamesList().size());
    Assert.assertEquals(OPERATION_NAME, operations.getOperationNamesList().get(0));
    jaeger.close();
  }
}
