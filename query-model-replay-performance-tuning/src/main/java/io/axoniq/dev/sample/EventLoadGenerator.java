package io.axoniq.dev.sample;

import io.axoniq.dev.sample.api.AccountCancelledEvent;
import io.axoniq.dev.sample.api.AccountCreatedEvent;
import io.axoniq.dev.sample.api.AccountCreditedEvent;
import io.axoniq.dev.sample.api.AccountDebitedEvent;
import io.axoniq.dev.sample.api.AccountEvent;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Introduce a service dedicated to generating an event load. This replaces the requirement of creating an Aggregate
 * which publishes account events in favor of staging it. Based on a similar component called {@code Loader.kt} Frans
 * van Buul created in this repository: https://github.com/fransvanbuul/cqrs-projection-performance
 *
 * @author Steven van Beelen
 * @author Frans van Buul
 */
@Service
@Profile("loader")
public class EventLoadGenerator implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EventGateway eventGateway;
    private final ExecutorService executorService;
    private final int eventsPerThread;
    private final int threadCount;
    private final int idsPerThread;

    public EventLoadGenerator(EventGateway eventGateway,
                              @Value("${loader.events-per-thread:125_000}") int eventsPerThread,
                              @Value("${loader.thread-count:16}") int threadCount,
                              @Value("${loader.ids-per-thread:5000}") int idsPerThread) {
        executorService = Executors.newFixedThreadPool(threadCount);
        this.eventGateway = eventGateway;
        this.eventsPerThread = eventsPerThread;
        this.threadCount = threadCount;
        this.idsPerThread = idsPerThread;
    }

    @Override
    public void run(String... args) {
        IntStream.range(0, threadCount)
                 .mapToObj(EventPublisher::new)
                 .map(eventPublisher -> CompletableFuture.runAsync(eventPublisher, executorService))
                 .reduce(CompletableFuture::allOf)
                 .ifPresent(result -> result.whenComplete((r, e) -> logger.info("All EventLoadGenerators are done."))
                                            .join());
        System.exit(1);
    }

    private class EventPublisher implements Runnable {

        private final int threadNumber;

        public EventPublisher(int threadNumber) {
            this.threadNumber = threadNumber;
            logger.info("Starting EventLoadGenerator-#{}.", this.threadNumber);
        }

        @Override
        public void run() {
            UUID[] accountIds = new UUID[idsPerThread];
            Random rng = new SecureRandom();
            for (int i = 0; i < eventsPerThread; i++) {
                if (i % 1000 == 0) {
                    logger.info("EventLoadGenerator-#{} published {} events...", threadNumber, i);
                }
                AccountEvent randomEvent = generateAccountEvent(accountIds, rng);
                eventGateway.publish(randomEvent);
            }
            logger.info("EventLoadGenerator-#{} is done.", threadNumber);
        }

        private AccountEvent generateAccountEvent(UUID[] accountIds, Random rng) {
            int index = rng.nextInt(accountIds.length);
            UUID accountId = accountIds[index];
            if (Objects.isNull(accountId)) {
                UUID newAccountI = UUID.randomUUID();
                accountIds[index] = newAccountI;
                return new AccountCreatedEvent(newAccountI);
            } else {
                float dice = rng.nextFloat();
                if (dice < 0.5f) {
                    return new AccountCreditedEvent(accountId, generateAmount(rng.nextDouble()));
                } else if (dice < 0.8f) {
                    return new AccountDebitedEvent(accountId, generateAmount(rng.nextDouble()));
                } else {
                    accountIds[index] = null;
                    return new AccountCancelledEvent(accountId);
                }
            }
        }

        private BigDecimal generateAmount(double randomDouble) {
            return BigDecimal.valueOf(randomDouble * 100).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
