package org.project.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.project.project.interceptor.RouteValidationInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private OidcUserInterceptor oidcUserInterceptor;
    
    @Autowired
    private PathValidationInterceptor pathValidationInterceptor;
    
    @Autowired
    private RouteValidationInterceptor routeValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor de validación de rutas (PRIMERO - limpia URLs malformadas)
        registry.addInterceptor(routeValidationInterceptor)
                .addPathPatterns("/devportal/**")
                .excludePathPatterns("/api/**", "/assets/**", "/css/**", "/js/**", "/images/**", "/favicon.ico");
        
        // Interceptor de validación de path variables (SEGUNDO - para seguridad)
        registry.addInterceptor(pathValidationInterceptor)
                .addPathPatterns("/devportal/**")
                .excludePathPatterns("/api/**", "/assets/**", "/css/**", "/js/**", "/images/**", "/favicon.ico");
        
        // Interceptor OIDC (TERCERO)
        registry.addInterceptor(oidcUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/**", "/assets/**", "/css/**", "/js/**", "/images/**", "/favicon.ico");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}