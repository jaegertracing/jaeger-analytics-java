package io.jaegertracing.analytics.spark;

import io.jaegertracing.analytics.NetworkLatency;
import io.jaegertracing.analytics.TraceDepth;
import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.model.Span;
import io.jaegertracing.analytics.model.ProtoSpanDeserializer;
import io.jaegertracing.analytics.model.Trace;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.tinkerpop.gremlin.structure.Graph;
import scala.Tuple2;

/**
 * @author Pavol Loffay
 */
public class SparkRunner {

  public static void main(String []args) throws InterruptedException, IOException {
    HTTPServer server = new HTTPServer(getPropOrEnv("PROMETHEUS_PORT", 9111));

    SparkConf sparkConf = new SparkConf()
        .setAppName("Trace DSL")
        .setMaster(getPropOrEnv("SPARK_MASTER","local[*]"));

    JavaSparkContext sc = new JavaSparkContext(sparkConf);
    JavaStreamingContext ssc = new JavaStreamingContext(sc, new Duration(5000));

    Set<String> topics = Collections.singleton(getPropOrEnv("KAFKA_JAEGER_TOPIC", "jaeger-spans"));
    Map<String, Object> kafkaParams = new HashMap<>();
    kafkaParams.put("bootstrap.servers", getPropOrEnv("KAFKA_BOOTSTRAP_SERVER", "localhost:9092"));
    kafkaParams.put("key.deserializer", StringDeserializer.class);
    kafkaParams.put("value.deserializer", ProtoSpanDeserializer.class);
    // hack to start always from beginning
    kafkaParams.put("group.id", "jaeger-trace-aggregation-" + System.currentTimeMillis());
    kafkaParams.put("auto.offset.reset", "earliest");
    kafkaParams.put("enable.auto.commit", false);
    kafkaParams.put("startingOffsets", "earliest");
    kafkaParams.put("endingOffsets", "latest");

    JavaInputDStream<ConsumerRecord<String, Span>> messages =
        KafkaUtils.createDirectStream(
            ssc,
            LocationStrategies.PreferConsistent(),
            ConsumerStrategies.Subscribe(topics, kafkaParams));

    JavaPairDStream<String, Span> traceIdSpanTuple = messages.mapToPair(record -> {
      return new Tuple2<>(record.value().traceId, record.value());
    });

    JavaDStream<Trace> tracesStream = traceIdSpanTuple.groupByKey().map(traceIdSpans -> {
      Iterable<Span> spans = traceIdSpans._2();
      Trace trace = new Trace();
      trace.traceId = traceIdSpans._1();
      trace.spans = StreamSupport.stream(spans.spliterator(), false)
          .collect(Collectors.toList());
      return trace;
    });

    tracesStream.foreachRDD((traceRDD, time) -> {
      traceRDD.foreach(trace -> {
        Graph graph = GraphCreator.create(trace);
        TraceDepth.calculateWithMetrics(graph);
        Map<String, Set<Long>> networkLatencies = NetworkLatency.calculate(graph);
        System.out.println(networkLatencies);
      });
    });

    ssc.start();
    ssc.awaitTermination();
  }

  private static  <T>  T getPropOrEnv(String key, T defaultValue) {
    String value = System.getProperty(key, System.getenv(key));
    return value != null ? (T) value : defaultValue;
  }
}
