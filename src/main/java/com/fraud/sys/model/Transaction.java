package com.fraud.sys.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;

/**
 * Sealed interface representing a financial transaction.
 * Permitted implementations are restricted to CardTransaction and UpiTransaction.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CardTransaction.class, name = "CARD"),
    @JsonSubTypes.Type(value = UpiTransaction.class, name = "UPI")
})
public sealed interface Transaction permits CardTransaction, UpiTransaction {
    String txId();
    String accountId();
    double amount();
    Instant timestamp();
    Location location();
}
