package io.jaegertracing.api_v2;

import io.jaegertracing.api_v2.Query.GetOperationsRequest;
import io.jaegertracing.api_v2.Query.GetOperationsResponse;
import io.jaegertracing.api_v2.QueryServiceGrpc.QueryServiceBlockingStub;
import io.opentracing.Tracer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class GrpcQueryTest {

  private static final String SERVICE_NAME = "query-test";
  private static final String OPERATION_NAME = "someoperation";

  private JaegerAllInOne jaeger = new JaegerAllInOne("jaegertracing/all-in-one:latest");
  private Tracer tracer;

  @Before
  public void before() {
    jaeger.start();
    tracer = jaeger.createTracer(SERVICE_NAME);
  }

  @After
  public void after() {
    jaeger.stop();
    tracer.close();
  }

  @Test
  public void testGetService() {
    tracer.buildSpan(OPERATION_NAME)
        .withTag("foo", "bar")
        .start()
        .finish();

    QueryServiceBlockingStub queryService = jaeger.createBlockingQueryService();
    WaitUtils.untilQueryHasTag(queryService, SERVICE_NAME, "foo", "bar");

    GetOperationsResponse operations = queryService
        .getOperations(GetOperationsRequest.newBuilder().setService(SERVICE_NAME).build());
    Assert.assertEquals(1, operations.getOperationNamesList().size());
    Assert.assertEquals(OPERATION_NAME, operations.getOperationNamesList().get(0));
  }
}
