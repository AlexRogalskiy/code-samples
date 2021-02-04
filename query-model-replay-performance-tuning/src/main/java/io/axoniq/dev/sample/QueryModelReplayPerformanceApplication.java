package io.axoniq.dev.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application to show case how to enhance the performance of query model replays.
 *
 * @author Steven van Beelen
 */
@SpringBootApplication
public class QueryModelReplayPerformanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryModelReplayPerformanceApplication.class, args);
    }
    // TODO: 28-12-20 look for message monitors to measure event throughput
}
