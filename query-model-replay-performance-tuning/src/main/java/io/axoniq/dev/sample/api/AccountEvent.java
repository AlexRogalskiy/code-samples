package io.axoniq.dev.sample.api;

import java.util.UUID;

/**
 * Base account event.
 *
 * @author Steven van Beelen
 */
public interface AccountEvent {

    UUID getAccountId();
}
