package rb.ebooklib.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import rb.ebooklib.security.jwt.AuthTokenFilter;
import rb.ebooklib.security.RestAuthenticationEntryPoint;
import rb.ebooklib.security.jwt.AuthTokenFilter;
import rb.ebooklib.security.service.CustomUserDetailsService;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }




//
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                //.httpBasic().disable()
                .headers(h -> h.contentSecurityPolicy("img-src '*'"))
                .exceptionHandling(eh -> eh.authenticationEntryPoint(new RestAuthenticationEntryPoint()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .authorizeHttpRequests(requests ->
                        requests.requestMatchers(HttpMethod.GET).permitAll()
                                .requestMatchers(HttpMethod.POST,"/**").permitAll()
                                .requestMatchers(HttpMethod.PUT,"/**").permitAll()
                                .anyRequest()
                                .authenticated());

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain_KANWEG(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().httpBasic().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin()
                //.loginPage("/auth/signin")
                .permitAll()
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/**").permitAll()
                .anyRequest().authenticated();

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.cors().and().csrf().disable()
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//                .authorizeHttpRequests().requestMatchers("/auth/**", "/books/**", "/**").permitAll()
//                .anyRequest().authenticated();
//
//        http.authenticationProvider(authenticationProvider());
//
//        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

}
