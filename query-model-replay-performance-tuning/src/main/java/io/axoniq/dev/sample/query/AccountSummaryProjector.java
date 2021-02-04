package io.axoniq.dev.sample.query;

import io.axoniq.dev.sample.api.AccountCancelledEvent;
import io.axoniq.dev.sample.api.AccountCreatedEvent;
import io.axoniq.dev.sample.api.AccountCreditedEvent;
import io.axoniq.dev.sample.api.AccountDebitedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Default projector for the {@link AccountSummary}. Does <em>not</em> employ the {@link
 * org.axonframework.messaging.unitofwork.UnitOfWork} to enhance event handling during replays. Instead it follows a
 * regular approach of saving and updating the entity in the event handler directly.
 *
 * @author Steven van Beelen
 */
@Component
@Profile("!uow")
@ProcessingGroup("account-summary")
class AccountSummaryProjector {

    private final AccountSummaryRepository repository;

    AccountSummaryProjector(AccountSummaryRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(AccountCreatedEvent event) {
        repository.save(new AccountSummary(event.getAccountId()));
    }

    @EventHandler
    public void on(AccountCreditedEvent event) {
        UUID accountId = event.getAccountId();
        repository.findById(accountId)
                  .map(account -> {
                      account.creditAccount(event.getAmount());
                      return account;
                  })
                  .orElseThrow(() -> new IllegalStateException(
                          "No account summary found to match id [" + accountId + "]"
                  ));
    }

    @EventHandler
    public void on(AccountDebitedEvent event) {
        UUID accountId = event.getAccountId();
        repository.findById(accountId)
                  .map(account -> {
                      account.debitAccount(event.getAmount());
                      return account;
                  })
                  .orElseThrow(() -> new IllegalStateException(
                          "No account summary found to match id [" + accountId + "]"
                  ));
    }

    @EventHandler
    public void on(AccountCancelledEvent event) {
        repository.deleteById(event.getAccountId());
    }
}
