package io.jaegertracing.dsl.gremlin.ui;

import io.jaegertracing.dsl.gremlin.model.Trace;
import io.jaegertracing.dsl.gremlin.ui.json.JsonSpanDeserializer;
import java.io.IOException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.Response;

public class JaegerQueryService {

  private static String UI_ENDPOINT = System.getProperty("JAEGER_UI_ENDPOINT", System.getenv("JAEGER_UI_ENDPOINT"));
  private static OkHttpClient client = getUnsafeOkHttpClient();

  public static Trace load(String traceId, String queryEndpoint) throws IOException {
    Call call = client.newCall(new Builder().url(queryEndpoint + traceId)
        .build());
    Response response = call.execute();
    try {
      return JsonSpanDeserializer.deserialize(response.body().bytes());
    } finally {
      response.close();
    }
  }

  public static Trace load(String traceId) throws IOException {
    return load(traceId, UI_ENDPOINT);
  }

  private static OkHttpClient getUnsafeOkHttpClient() {
    try {
      // Create a trust manager that does not validate certificate chains
      final TrustManager[] trustAllCerts = new TrustManager[] {
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return new java.security.cert.X509Certificate[]{};
            }
          }
      };

      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

      OkHttpClient.Builder builder = new OkHttpClient.Builder();
      builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
      builder.hostnameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      });

      OkHttpClient okHttpClient = builder.build();
      return okHttpClient;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
