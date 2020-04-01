package io.jaegertracing.analytics;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.jaegertracing.analytics.NetworkLatency.Name;
import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.model.Converter;
import io.jaegertracing.analytics.model.Trace;
import io.jaegertracing.api_v2.Model.Span;
import io.jaegertracing.api_v2.Query.FindTracesRequest;
import io.jaegertracing.api_v2.Query.GetTraceRequest;
import io.jaegertracing.api_v2.Query.SpansResponseChunk;
import io.jaegertracing.api_v2.Query.TraceQueryParameters;
import io.jaegertracing.api_v2.QueryServiceGrpc;
import io.jaegertracing.api_v2.QueryServiceGrpc.QueryServiceBlockingStub;
import io.opentracing.tag.Tags;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test class is used to test code from jupyter notebook jaeger-query.
 * The tests are not executed as part of the CI. They are run manually to verify
 * that the code works.
 *
 * @author Pavol Loffay
 */
@Ignore
public class JaegerQueryNotebookTest {

  @Test
  public void runFindByTraceId() {
    String queryHostPort = "192.168.0.31:16686";
    String traceIdStr = "5a8c9a8c69b0fd4f";

    ManagedChannel channel;
    channel = ManagedChannelBuilder.forTarget(queryHostPort).usePlaintext().build();
    QueryServiceBlockingStub queryService = QueryServiceGrpc.newBlockingStub(channel);

    ByteString traceId = Converter.toProtoId(traceIdStr);
    Iterator<SpansResponseChunk> traceProto = queryService.getTrace(
        GetTraceRequest.newBuilder().setTraceId(traceId).build());

    List<Span> spans = new ArrayList<>();
    while (traceProto.hasNext()) {
      spans.addAll(traceProto.next().getSpansList());
    }
    Trace trace = Converter.toModel(spans);
    Graph graph = GraphCreator.create(trace);

    Set<io.jaegertracing.analytics.model.Span> errorSpans = NumberOfErrors.calculate(graph);
    Map<String, Integer> errorTypeAndCount = new LinkedHashMap<>();
    for (io.jaegertracing.analytics.model.Span errorSpan: errorSpans) {
      for (io.jaegertracing.analytics.model.Span.Log log: errorSpan.logs) {
        String err = log.fields.get(Tags.ERROR.getKey());
        if (err != null) {
          Integer count = errorTypeAndCount.get(err);
          if (count == null) {
            count = 1;
          }
          errorTypeAndCount.put(err, ++count);
        }
      }
    }
    System.out.printf("Error and count: %s\n", errorTypeAndCount);

    int height = TraceHeight.calculate(graph);
    Map<Name, Set<Double>> networkLatencies = NetworkLatency.calculate(graph);
    System.out.printf("Trace height = %d\n", height);
    System.out.printf("Network latencies = %s\n", networkLatencies);
  }

  @Test
  public void runFindTraces() {
String queryHostPort = "192.168.0.31:16686";

ManagedChannel channel;
channel = ManagedChannelBuilder.forTarget(queryHostPort).usePlaintext().build();
QueryServiceBlockingStub queryService = QueryServiceGrpc.newBlockingStub(channel);

TraceQueryParameters query = TraceQueryParameters.newBuilder().setServiceName("frontend").build();
Iterator<SpansResponseChunk> traceProto = queryService.findTraces(
    FindTracesRequest.newBuilder().setQuery(query).build());

List<Span> spans = new ArrayList<>();
while (traceProto.hasNext()) {
  spans.addAll(traceProto.next().getSpansList());
}
Trace trace = Converter.toModel(spans);
Graph graph = GraphCreator.create(trace);

Set<io.jaegertracing.analytics.model.Span> errorSpans = NumberOfErrors.calculate(graph);
Map<String, Integer> errorTypeAndCount = new LinkedHashMap<>();
for (io.jaegertracing.analytics.model.Span errorSpan: errorSpans) {
  for (io.jaegertracing.analytics.model.Span.Log log: errorSpan.logs) {
    String err = log.fields.get(Tags.ERROR.getKey());
    if (err != null) {
      Integer count = errorTypeAndCount.get(err);
      if (count == null) {
        count = 0;
      }
      errorTypeAndCount.put(err, ++count);
    }
  }
}
System.out.printf("Error and count: %s\n", errorTypeAndCount);

int height = TraceHeight.calculate(graph);
Map<Name, Set<Double>> networkLatencies = NetworkLatency.calculate(graph);
System.out.printf("Trace height = %d\n", height);
System.out.printf("Network latencies = %s\n", networkLatencies);
  }
}
