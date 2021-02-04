package io.axoniq.dev.sample.query;

import io.axoniq.dev.sample.api.AccountCancelledEvent;
import io.axoniq.dev.sample.api.AccountCreatedEvent;
import io.axoniq.dev.sample.api.AccountCreditedEvent;
import io.axoniq.dev.sample.api.AccountDebitedEvent;
import io.axoniq.dev.sample.api.AccountEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Projector for the {@link AccountSummary}. Uses the {@link UnitOfWork} to stage the storage of the model once the
 * {@code UnitOfWork} is committed. This approach is only feasible if the batch size is greater than 1, as this enhance
 * the number of events dealt with in a {@code UnitOfWork}.
 * <p>
 * It does so by storing the {@code AccountSummary} on the {@link org.axonframework.messaging.unitofwork.UnitOfWork.Phase#PREPARE_COMMIT}
 * phase, by attaching an operation {@link UnitOfWork#onPrepareCommit(Consumer)}. This requires to store the model
 * within the {@link UnitOfWork#resources()}.
 * <p>
 * Certain caution should be taken with this approach, as from the handling perspective we cannot be certain whether a
 * the model is present in the resources for the given event batch.
 *
 * @author Steven van Beelen
 */
@Component
@Profile("uow")
@ProcessingGroup("account-summary")
class OptimizedAccountSummaryProjector {

    private final AccountSummaryRepository repository;

    OptimizedAccountSummaryProjector(AccountSummaryRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(AccountCreatedEvent event, UnitOfWork<EventMessage<AccountCreatedEvent>> unitOfWork) {
        unitOfWork.resources()
                  .put(event.accountIdString(), new AccountSummary(event.getAccountId()));
        unitOfWork.onPrepareCommit(saveModel(event.accountIdString()));
    }

    @EventHandler
    public void on(AccountCreditedEvent event, UnitOfWork<EventMessage<AccountCreditedEvent>> unitOfWork) {
        String accountIdString = event.accountIdString();
        AccountSummary accountSummary = unitOfWork.getOrComputeResource(
                accountIdString,
                accountId -> {
                    unitOfWork.onPrepareCommit(saveModel(event.accountIdString()));
                    return getAccountSummaryFor(event.getAccountId());
                }
        );

        accountSummary.creditAccount(event.getAmount());
        unitOfWork.resources().put(accountIdString, accountSummary);
    }

    @EventHandler
    public void on(AccountDebitedEvent event, UnitOfWork<EventMessage<AccountDebitedEvent>> unitOfWork) {
        String accountIdString = event.accountIdString();
        AccountSummary accountSummary = unitOfWork.getOrComputeResource(
                accountIdString,
                accountId -> {
                    unitOfWork.onPrepareCommit(saveModel(event.accountIdString()));
                    return getAccountSummaryFor(event.getAccountId());
                }
        );

        accountSummary.debitAccount(event.getAmount());
        unitOfWork.resources().put(accountIdString, accountSummary);
    }

    @EventHandler
    public void on(AccountCancelledEvent event, UnitOfWork<EventMessage<AccountDebitedEvent>> unitOfWork) {
        AccountSummary accountSummary = (AccountSummary) unitOfWork.resources().remove(event.accountIdString());
        if (Objects.isNull(accountSummary)) {
            unitOfWork.onPrepareCommit(uow -> repository.deleteById(event.getAccountId()));
        }
    }

    /**
     * Saves the {@link AccountSummary} corresponding to the given {@code accountId} in the repository. Does so in a
     * {@link Consumer} of a {@link UnitOfWork}. This allows this method to be invoked inside any of the {@code
     * UnitOfWork}s phases.
     * <p>
     * The corresponding {@code AccountSummary} will only be saved if it is present in the {@link
     * UnitOfWork#resources()}. If it is not present in the resources, that means the {@code AccountSummary} has been
     * removed through handling an {@link AccountCancelledEvent}. The model should thus only be stored in the {@code
     * UnitOfWork}'s phase if it is present in the resources.
     *
     * @param accountId the identifier of the {@link AccountSummary} to store
     * @param <E>       the type of {@link AccountEvent} part of the {@link UnitOfWork}
     * @return a {@link Consumer} of the {@link UnitOfWork} which can be used on any of the {@code UnitOfWork}'s phases
     */
    private <E extends AccountEvent> Consumer<UnitOfWork<EventMessage<E>>> saveModel(String accountId) {
        return unitOfWork -> {
            AccountSummary accountSummary = unitOfWork.getResource(accountId);
            if (Objects.nonNull(accountSummary)) {
                repository.save(accountSummary);
            }
        };
    }

    private AccountSummary getAccountSummaryFor(UUID accountId) {
        return repository.findById(accountId)
                         .orElseThrow(() -> new IllegalStateException(
                                 "No account summary found to match id [" + accountId + "]"
                         ));
    }
}
