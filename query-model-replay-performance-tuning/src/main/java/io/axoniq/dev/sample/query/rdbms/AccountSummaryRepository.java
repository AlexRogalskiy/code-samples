package io.axoniq.dev.sample.query.rdbms;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository solution for an {@link AccountSummary}.
 *
 * @author Steven van Beelen
 */
interface AccountSummaryRepository extends JpaRepository<AccountSummary, UUID> {

}
