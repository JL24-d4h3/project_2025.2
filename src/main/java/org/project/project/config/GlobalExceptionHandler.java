package org.project.project.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * Manejador global de excepciones para capturar errores que no se loggean
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        logger.error("╔═══════════════════════════════════════════════════════════════");
        logger.error("║ 🚫 [EXCEPTION] ACCESS DENIED");
        logger.error("║ 📍 URI: {}", request.getRequestURI());
        logger.error("║ 👤 User: {}", auth != null ? auth.getName() : "NULL");
        logger.error("║ ⚡ Authorities: {}", auth != null ? auth.getAuthorities() : "N/A");
        logger.error("║ 💥 Message: {}", ex.getMessage());
        logger.error("╚═══════════════════════════════════════════════════════════════", ex);
        
        model.addAttribute("error", "Acceso denegado: " + ex.getMessage());
        return "error/403";
    }
    
    @ExceptionHandler(TemplateProcessingException.class)
    public ModelAndView handleTemplateProcessingException(TemplateProcessingException ex, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        logger.error("╔═══════════════════════════════════════════════════════════════");
        logger.error("║ 🔥 [THYMELEAF ERROR] TEMPLATE PROCESSING EXCEPTION");
        logger.error("║ 📍 URI: {}", request.getRequestURI());
        logger.error("║ 🎯 Method: {}", request.getMethod());
        logger.error("║ 👤 User: {}", auth != null ? auth.getName() : "NULL");
        logger.error("║ 📄 Template Name: {}", ex.getTemplateName());
        logger.error("║ 📍 Line: {}", ex.getLine());
        logger.error("║ 📍 Col: {}", ex.getCol());
        logger.error("║ 💥 Message: {}", ex.getMessage());
        logger.error("║ 🔗 Cause: {}", ex.getCause() != null ? ex.getCause().getMessage() : "N/A");
        logger.error("╚═══════════════════════════════════════════════════════════════");
        
        // Log stack trace completo
        logger.error("Stack Trace:", ex);
        
        ModelAndView mav = new ModelAndView("error/template-error");
        mav.addObject("templateName", ex.getTemplateName());
        mav.addObject("line", ex.getLine());
        mav.addObject("col", ex.getCol());
        mav.addObject("message", ex.getMessage());
        mav.addObject("cause", ex.getCause() != null ? ex.getCause().getMessage() : "No cause");
        return mav;
    }
    
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception ex, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        logger.error("╔═══════════════════════════════════════════════════════════════");
        logger.error("║ ❌ [EXCEPTION] UNHANDLED EXCEPTION");
        logger.error("║ 📍 URI: {}", request.getRequestURI());
        logger.error("║ 🎯 Method: {}", request.getMethod());
        logger.error("║ 👤 User: {}", auth != null ? auth.getName() : "NULL");
        logger.error("║ ⚡ Authorities: {}", auth != null ? auth.getAuthorities() : "N/A");
        logger.error("║ 💥 Exception Type: {}", ex.getClass().getName());
        logger.error("║ 📝 Message: {}", ex.getMessage());
        logger.error("╚═══════════════════════════════════════════════════════════════", ex);
        
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("error", ex.getMessage());
        mav.addObject("exception", ex.getClass().getName());
        return mav;
    }
}
