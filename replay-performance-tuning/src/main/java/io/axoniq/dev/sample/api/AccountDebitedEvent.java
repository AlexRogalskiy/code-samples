package io.axoniq.dev.sample.api;

import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * {@link AccountEvent} dedicated towards debiting an account.
 *
 * @author Steven van Beelen
 */
public class AccountDebitedEvent implements AccountEvent {

    private final UUID accountId;
    private final BigDecimal amount;

    @ConstructorProperties({"accountId", "amount"})
    public AccountDebitedEvent(UUID accountId, BigDecimal amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    @Override
    public UUID getAccountId() {
        return accountId;
    }

    @Override
    public String accountIdString() {
        return accountId.toString();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountDebitedEvent that = (AccountDebitedEvent) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, amount);
    }

    @Override
    public String toString() {
        return "AccountDebitedEvent{" +
                "accountId=" + accountId +
                ", amount=" + amount +
                '}';
    }
}
