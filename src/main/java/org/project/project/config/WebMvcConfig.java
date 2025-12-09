package org.project.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Spring MVC para registrar interceptores y recursos estáticos
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;
    
    @Autowired
    private org.project.project.interceptor.RouteValidationInterceptor routeValidationInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        System.out.println("🔧 [WebMvcConfig] Registrando interceptores...");
        
        // 1. RouteValidationInterceptor - Validar rutas primero
        registry.addInterceptor(routeValidationInterceptor)
                .addPathPatterns("/devportal/**")
                .order(1);
        
        // 2. RequestLoggingInterceptor - Logging después
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .order(2);
                
        System.out.println("✅ [WebMvcConfig] Interceptores registrados correctamente");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("🔧 [WebMvcConfig] Configurando manejadores de recursos estáticos...");
        
        // Configurar favicon.ico para que se sirva desde static/
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico")
                .setCachePeriod(604800); // Cache por 7 días
        
        System.out.println("✅ [WebMvcConfig] favicon.ico configurado correctamente");
    }
}
