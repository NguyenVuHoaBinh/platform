package viettel.dac.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${tool-template.execution.max-concurrent-executions:20}")
    private int maxConcurrentExecutions;

    /**
     * Configures the async task executor for template executions.
     *
     * @return The task executor
     */
    @Bean(name = "executionTaskExecutor")
    public Executor executionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(maxConcurrentExecutions);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("execution-");
        executor.initialize();
        return executor;
    }
}