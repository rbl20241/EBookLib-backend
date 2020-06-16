package rb.ebooklib.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import rb.ebooklib.security.CustomUserDetailsService;

import java.util.Objects;

@Configuration
class OAuth2ServerConfiguration {
    private static final String RESOURCE_ID = "restservice";
    private static final String CLIENT_ID_PROPERTY = "nl.librarysharing.oauth2.client_id";
    private static final String CLIENT_PASS_PROPERTY = "nl.librarysharing.oauth2.client_pass";

    private static final String[] PROTECTED_ENDPOINTS = {"/books/**", "/categories/**",
            "/authors/**",  "/users/*", "/search/**", "/settings/**", "/rename/**"};
    private static final String[] AUTHORIZED_GRANT_TYPES = {"password", "refresh_token"};
    private static final String[] AUTHORITIES = {"USER"};
    private static final String[] SCOPES = {"read", "write"};

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * The configuration for the Resource Server. All endpoints except OAuth context are currently defined
     * as resource
     */
    @Configuration
    @EnableResourceServer
    class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(final ResourceServerSecurityConfigurer resources) {
            resources.resourceId(RESOURCE_ID);
        }

        @Override
        public void configure(final HttpSecurity http) throws Exception {
            // Require authorization on all endpoints except oauth context
            http.csrf().disable().authorizeRequests()
                    .antMatchers(PROTECTED_ENDPOINTS).authenticated()
                    .antMatchers(HttpMethod.OPTIONS, "/oauth/token").permitAll();
        }
    }

    /**
     * Configuration for the Authorization Server, the OAuth context
     */
    @Configuration
    @EnableAuthorizationServer
    class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Autowired
        Environment env;

        @Autowired
        BCryptPasswordEncoder passwordEncoder;

        @Autowired
        @Qualifier("authenticationManagerBean")
        private AuthenticationManager authenticationManager;

        @Autowired
        private CustomUserDetailsService userDetailsService;

        private final TokenStore tokenStore = new InMemoryTokenStore();

        @Override
        public void configure(final ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory()
                    .withClient(this.env.getProperty(CLIENT_ID_PROPERTY))
                    .authorizedGrantTypes(AUTHORIZED_GRANT_TYPES)
                    .authorities(AUTHORITIES)
                    .scopes(SCOPES)
                    .resourceIds(RESOURCE_ID)
                    // encode the secret with BCrypt
                    .secret(this.passwordEncoder.encode(Objects.requireNonNull(this.env.getProperty(CLIENT_PASS_PROPERTY))))
                    .accessTokenValiditySeconds(Integer.MAX_VALUE);
        }

        @Override
        public void configure(final AuthorizationServerEndpointsConfigurer endpoints) {
            endpoints.tokenStore(this.tokenStore)
                    .authenticationManager(this.authenticationManager)
                    .userDetailsService(this.userDetailsService);
        }
    }
}
