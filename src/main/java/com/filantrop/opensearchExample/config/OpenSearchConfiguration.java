package com.filantrop.opensearchExample.config;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.spring.boot.autoconfigure.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class OpenSearchConfiguration {

    @Bean
    RestClientBuilderCustomizer customizer() {
        return new RestClientBuilderCustomizer() {
            @Override
            public void customize(HttpAsyncClientBuilder builder) {
                try {
                    builder.setSSLContext(new SSLContextBuilder()
                            .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                            .build());
                } catch (final KeyManagementException | NoSuchAlgorithmException | KeyStoreException ex) {
                    throw new RuntimeException("Failed to initialize SSL Context instance", ex);
                }
            }

            @Override
            public void customize(RestClientBuilder builder) {
                // No additional customizations needed
            }
        };
    }
/*    @Bean
    public OpenSearchClient openSearchClient() {
        final HttpHost host = new HttpHost("localhost", 9200, "https");
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(new AuthScope(host), new UsernamePasswordCredentials("admin", "Wpassword1!"));

        final RestClient restClient = RestClient.builder(host)
                .setHttpClientConfigCallback(
                        httpClientBuilder -> {
                            try {
                                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                                        .setSSLContext(new SSLContextBuilder()
                                                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                                                .build());
                            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .build();

        JacksonJsonpMapper jacksonJsonpMapper = new JacksonJsonpMapper();
        final OpenSearchTransport transport = new RestClientTransport(restClient, jacksonJsonpMapper);
        return new OpenSearchClient(transport);
    }*/

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules()
                // correct work with OffsetDateTime
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // dont lose accuracy on long
                .enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
    }

    @Bean
    public JacksonJsonpMapper jacksonJsonpMapper(ObjectMapper objectMapper) {
        return new JacksonJsonpMapper(objectMapper);
    }
}
