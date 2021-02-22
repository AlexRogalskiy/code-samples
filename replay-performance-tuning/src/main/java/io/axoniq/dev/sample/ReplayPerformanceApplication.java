package io.axoniq.dev.sample;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.async.SequencingPolicy;
import org.axonframework.metrics.MessageTimerMonitor;
import org.axonframework.monitoring.NoOpMessageMonitor;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

/**
 * Main application to show case how to enhance the performance of query model replays.
 *
 * @author Steven van Beelen
 */
@SpringBootApplication
public class ReplayPerformanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReplayPerformanceApplication.class, args);
    }

    @Bean
    @Profile("multithreading")
    public SequencingPolicy<EventMessage<?>> sequentialPerAccountPolicy() {
        return new SequentialPerAccountPolicy();
    }

    @Autowired
    public void configureMessageMonitors(Configurer configurer, MetricRegistry metricRegistry) {
        configurer.configureMessageMonitor(CommandBus.class, (c, t, n) -> NoOpMessageMonitor.INSTANCE);
        configurer.configureMessageMonitor(EventBus.class, (c, t, n) -> NoOpMessageMonitor.INSTANCE);
        configurer.configureMessageMonitor(QueryBus.class, (c, t, n) -> NoOpMessageMonitor.INSTANCE);
        configurer.configureMessageMonitor(QueryUpdateEmitter.class, (c, t, n) -> NoOpMessageMonitor.INSTANCE);

        configurer.configureMessageMonitor(EventProcessor.class, (config, type, name) -> {
            MessageTimerMonitor messageTimerMonitor = new MessageTimerMonitor();
            MetricRegistry eventProcessingRegistry = new MetricRegistry();
            eventProcessingRegistry.register("messageTimer", messageTimerMonitor);
            metricRegistry.register(name, eventProcessingRegistry);
            return messageTimerMonitor;
        });
    }

    @Autowired
    public void configureMetricLogger(MetricRegistry metricRegistry) {
        Slf4jReporter.forRegistry(metricRegistry)
                     .outputTo(LoggerFactory.getLogger("io.axoniq.dev.sample.replay-performance"))
                     .convertRatesTo(TimeUnit.SECONDS)
                     .convertDurationsTo(TimeUnit.MILLISECONDS)
                     .build()
                     .start(10, TimeUnit.SECONDS);
    }
}
