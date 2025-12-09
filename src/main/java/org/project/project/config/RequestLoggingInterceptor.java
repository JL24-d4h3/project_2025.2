package org.project.project.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor para loggear todas las peticiones que llegan a los controladores
 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // Solo loggear peticiones al dashboard
        if (uri.contains("/dashboard")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            logger.info("╔═══════════════════════════════════════════════════════════════");
            logger.info("║ 🎯 [INTERCEPTOR] REQUEST ALCANZÓ SPRING MVC");
            logger.info("║ 📍 URI: {} {}", method, uri);
            logger.info("║ 🎭 Handler: {}", handler.getClass().getName());
            logger.info("║ 👤 Authentication: {}", auth != null ? auth.getName() : "NULL");
            logger.info("║ 🔑 Authenticated: {}", auth != null ? auth.isAuthenticated() : "N/A");
            logger.info("║ ⚡ Authorities: {}", auth != null ? auth.getAuthorities() : "N/A");
            logger.info("╚═══════════════════════════════════════════════════════════════");
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String uri = request.getRequestURI();
        
        if (uri.contains("/dashboard")) {
            logger.info("╔═══════════════════════════════════════════════════════════════");
            logger.info("║ ✅ [INTERCEPTOR] POST-HANDLE - Controlador ejecutado");
            logger.info("║ 📍 URI: {}", uri);
            logger.info("║ 📄 View: {}", modelAndView != null ? modelAndView.getViewName() : "NULL");
            logger.info("║ 📊 Model: {}", modelAndView != null ? modelAndView.getModel().keySet() : "NULL");
            logger.info("║ 🔢 Status: {}", response.getStatus());
            logger.info("╚═══════════════════════════════════════════════════════════════");
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String uri = request.getRequestURI();
        
        if (uri.contains("/dashboard")) {
            if (ex != null) {
                logger.error("╔═══════════════════════════════════════════════════════════════");
                logger.error("║ ❌ [INTERCEPTOR] EXCEPCIÓN EN CONTROLADOR");
                logger.error("║ 📍 URI: {}", uri);
                logger.error("║ 💥 Exception: {}", ex.getClass().getName());
                logger.error("║ 📝 Message: {}", ex.getMessage());
                logger.error("╚═══════════════════════════════════════════════════════════════", ex);
            } else {
                logger.info("╔═══════════════════════════════════════════════════════════════");
                logger.info("║ 🏁 [INTERCEPTOR] REQUEST COMPLETADO");
                logger.info("║ 📍 URI: {}", uri);
                logger.info("║ 🔢 Final Status: {}", response.getStatus());
                logger.info("╚═══════════════════════════════════════════════════════════════");
            }
        }
    }
}
