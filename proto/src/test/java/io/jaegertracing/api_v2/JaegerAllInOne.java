package io.jaegertracing.api_v2;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.JaegerTracer.Builder;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.spi.Reporter;
import io.jaegertracing.spi.Sender;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import java.util.Collections;
import java.util.Set;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

/**
 * @author Pavol Loffay
 */
public class JaegerAllInOne extends GenericContainer<JaegerAllInOne> {

  public static final int JAEGER_QUERY_PORT = 16686;
  public static final int JAEGER_COLLECTOR_THRIFT_PORT = 14268;
  public static final int JAEGER_COLLECTOR_GRPC_PORT = 14250;
  public static final int JAEGER_ADMIN_PORT = 14269;
  public static final int JAEGER_ZIPKIN_PORT = 9411;

  public JaegerAllInOne(String dockerImageName) {
    super(dockerImageName);
    init();
  }

  protected void init() {
    waitingFor(new BoundPortHttpWaitStrategy(JAEGER_ADMIN_PORT));
    withEnv("COLLECTOR_ZIPKIN_HTTP_PORT", "9411");
    withExposedPorts(JAEGER_ADMIN_PORT, JAEGER_COLLECTOR_THRIFT_PORT, JAEGER_COLLECTOR_GRPC_PORT, JAEGER_QUERY_PORT, JAEGER_ZIPKIN_PORT);
  }

  public int getCollectorThriftPort() {
    return getMappedPort(JAEGER_COLLECTOR_THRIFT_PORT);
  }

  public int getQueryPort() {
    return getMappedPort(JAEGER_QUERY_PORT);
  }

  public JaegerTracer getTracer(String serviceName) {
    String endpoint = String.format("http://localhost:%d/api/traces", getCollectorThriftPort());
    Sender sender = new HttpSender.Builder(endpoint)
        .build();
    Reporter reporter = new RemoteReporter.Builder()
        .withSender(sender)
        .build();
    Builder tracerBuilder = new Builder(serviceName)
        .withSampler(new ConstSampler(true))
        .withReporter(reporter);
    return tracerBuilder.build();
  }

  public static class BoundPortHttpWaitStrategy extends HttpWaitStrategy {
    private final int port;

    public BoundPortHttpWaitStrategy(int port) {
      this.port = port;
    }

    @Override
    protected Set<Integer> getLivenessCheckPorts() {
      int mapptedPort = this.waitStrategyTarget.getMappedPort(port);
      return Collections.singleton(mapptedPort);
    }
  }
}
