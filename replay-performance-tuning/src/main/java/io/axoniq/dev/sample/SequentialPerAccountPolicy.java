package io.axoniq.dev.sample;

import io.axoniq.dev.sample.api.AccountEvent;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.async.SequencingPolicy;

/**
 * Dedicated {@link SequencingPolicy} implementation. Used to ensure {@link AccountEvent}s based on the same account
 * identifier are handled in sequence. Thus without this policy in place, multi-threading the event processor would
 * result in numerous exceptions being thrown because the query model could not be found.
 * <p>
 * Normally the {@link org.axonframework.eventhandling.async.SequentialPerAggregatePolicy} would take care of this, but
 * we are not publishing domain events in the application. Due to this, the events do not carry identical {@link
 * DomainEventMessage#getAggregateIdentifier()} results at all and hence are not handled in sequence.
 *
 * @author Steven van Beelen
 */
public class SequentialPerAccountPolicy implements SequencingPolicy<EventMessage<?>> {

    @Override
    public Object getSequenceIdentifierFor(EventMessage<?> event) {
        return ((AccountEvent) event.getPayload()).accountIdString();
    }
}
