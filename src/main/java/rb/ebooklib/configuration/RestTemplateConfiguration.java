package rb.ebooklib.configuration;

import org.apache.hc.client5.http.classic.HttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import rb.ebooklib.util.RequestResponseLoggingInterceptor;

import java.util.Collections;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, HttpClient httpClient) {
        return builder
                .requestFactory(() -> new BufferingClientHttpRequestFactory(clientHttpRequestFactory(httpClient)))
                .interceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()))
                .build();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory(HttpClient httpClient) {
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
