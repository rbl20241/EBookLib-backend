package rb.ebooklib.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class CorsConfiguration implements WebMvcConfigurer {

    @Value( "${access.control.allow.origin}" )
    private String accessControlAllowOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        long maxAgeSecs = 3600;
        log.info(String.format("CORS config Allowed origins %s", accessControlAllowOrigin             ));
        registry.addMapping("/**")
                .allowedOrigins(accessControlAllowOrigin)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(maxAgeSecs);
    }
}