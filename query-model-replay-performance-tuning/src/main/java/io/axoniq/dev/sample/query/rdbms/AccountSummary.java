package io.axoniq.dev.sample.query.rdbms;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Query Model reflecting an account as an Entity.
 *
 * @author Steven van Beelen
 */
@Entity
class AccountSummary {

    @Id
    private UUID accountId;
    private BigDecimal balance;

    public AccountSummary(UUID accountId) {
        this.accountId = accountId;
        this.balance = BigDecimal.ZERO;
    }

    public AccountSummary() {
        // Default constructor
    }

    public UUID getAccountId() {
        return accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void debitAccount(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void creditAccount(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountSummary that = (AccountSummary) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(balance, that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, balance);
    }

    @Override
    public String toString() {
        return "AccountSummary{" +
                "accountId=" + accountId +
                ", balance=" + balance +
                '}';
    }
}
