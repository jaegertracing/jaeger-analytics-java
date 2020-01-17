package io.jaegertracing.analytics.tracequality;

import io.jaegertracing.analytics.ModelRunner;
import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.model.Span;
import io.prometheus.client.Counter;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Emits a passing score if the version number of a client is higher than the ones specified and a failing score
 * otherwise.
 */
public class MinimumClientVersion implements ModelRunner {

    static final String VERSION_TAG = "jaeger.version";
    static final String MISSING_VERSION = "none";

    public static class Builder implements Serializable {
        private String javaVersion = "1.0.0";
        private String nodeVersion = "3.17.1";
        private String goVersion = "2.22.0";
        private String pythonVersion = "4.0.0";

        private Builder() {}

        public  MinimumClientVersion build() {
            return new MinimumClientVersion(this);
        }

        public Builder withJavaVersion(String javaVersion) {
            this.javaVersion = javaVersion;
            return this;
        }

        public Builder withNodeVersion(String nodeVersion) {
            this.nodeVersion = nodeVersion;
            return this;
        }

        public Builder withGoVersion(String goVersion) {
            this.goVersion = goVersion;
            return this;
        }

        public Builder withPythonVersion(String pythonVersion) {
            this.pythonVersion = pythonVersion;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private MinimumClientVersion(Builder builder) {
        languageToMinVersion = Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("go", builder.goVersion);
            put("python", builder.pythonVersion);
            put("node", builder.nodeVersion);
            put("java", builder.javaVersion);
        }});

    }

    private final Map<String, String> languageToMinVersion;

    private static final Counter counter = Counter.build()
        .name("trace_quality_minimum_client_version_total")
        .help("The service emitted spans with Jaeger client version")
        .labelNames("pass", "service", "version")
        .create()
        .register();

    @Override
    public void runWithMetrics(Graph graph) {
        Iterator<Vertex> vertices = graph.vertices();
        while (vertices.hasNext()) {
            Vertex vertex = vertices.next();
            Span span = GraphCreator.toSpan(vertex);
            String jaegerVersion = span.tags.get(VERSION_TAG);
            if (jaegerVersion == null || jaegerVersion.isEmpty()) {
                jaegerVersion = MISSING_VERSION;
            }
            boolean result = computeScore(span);
            counter.labels(Boolean.toString(result), span.serviceName, jaegerVersion)
                .inc();
        }
    }

    public boolean computeScore(Span span) {
        String version = span.tags.get(VERSION_TAG);
        if (version == null || version.isEmpty()) {
            return false;
        }

        String[] languageVersion = version.toLowerCase().split("-");
        if (languageVersion.length != 2
                || languageToMinVersion.get(languageVersion[0]) == null
                || !languageVersion[1].matches("[0-9]+(\\.[0-9]+)*")) {
            return false;
        }

        if (isGreaterThanOrEqualTo(languageToMinVersion.get(languageVersion[0]), languageVersion[1])) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Compares two semantic version strings
     *
     * @param left  semantic version string
     * @param right semantic version string
     * @return true if right is greater than or equal to left
     * false for all other cases
     */
    boolean isGreaterThanOrEqualTo(String left, String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");

        int length = Math.min(leftParts.length, rightParts.length);
        try {
            for (int i = 0; i < length; i++) {
                int leftPart = Integer.parseInt(leftParts[i]);
                int rightPart = Integer.parseInt(rightParts[i]);
                if (leftPart < rightPart) {
                    return true;
                }
                if (leftPart > rightPart) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
