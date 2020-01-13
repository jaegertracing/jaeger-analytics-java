package io.jaegertracing.analytics;

import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * @author Pavol Loffay
 */
public interface ModelRunner {

  void runWithMetrics(Graph graph);
}
