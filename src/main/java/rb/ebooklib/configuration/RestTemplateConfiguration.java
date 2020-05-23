package rb.ebooklib.configuration;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    CloseableHttpClient httpClient;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(() -> new BufferingClientHttpRequestFactory(clientHttpRequestFactory()))
                .interceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()))
                .build();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
