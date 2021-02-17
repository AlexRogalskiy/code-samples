package io.axoniq.dev.sample.query;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository solution for an {@link AccountSummary}.
 *
 * @author Steven van Beelen
 */
@Profile("!loader")
interface AccountSummaryRepository extends JpaRepository<AccountSummary, UUID> {

}
