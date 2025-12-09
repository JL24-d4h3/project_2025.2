package org.project.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono de operaciones de archivos
 * Usado para jobs pesados como comprimir, descargar múltiples archivos, etc.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Executor para operaciones de archivos pesadas
     * Pool de threads dedicado para no bloquear requests HTTP
     */
    @Bean(name = "fileOperationExecutor")
    public Executor fileOperationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core threads siempre activos
        executor.setCorePoolSize(2);
        
        // Máximo de threads simultáneos
        executor.setMaxPoolSize(5);
        
        // Cola de tareas pendientes
        executor.setQueueCapacity(100);
        
        // Prefijo para identificar threads en logs
        executor.setThreadNamePrefix("FileOp-");
        
        // Esperar a que terminen las tareas al shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

    /**
     * Executor para sincronización con GitHub
     * Pool separado para no competir con operaciones de archivos locales
     */
    @Bean(name = "githubSyncExecutor")
    public Executor githubSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("GitHubSync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120); // Más tiempo para syncs largos
        
        executor.initialize();
        return executor;
    }
}
