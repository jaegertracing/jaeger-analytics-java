package io.jaegertracing.analytics;

import java.io.Serializable;
import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * @author Pavol Loffay
 */
public interface ModelRunner extends Serializable {

  void runWithMetrics(Graph graph);
}
