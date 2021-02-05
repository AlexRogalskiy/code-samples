package io.axoniq.dev.sample.query.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Spring Data Mongo repository solution for an {@link AccountSummary}.
 *
 * @author Steven van Beelen
 */
interface AccountSummaryRepository extends MongoRepository<AccountSummary, UUID> {

}
