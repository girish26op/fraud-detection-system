package com.fraud.sys.model;

import java.time.Instant;

/**
 * Record representing a credit or debit card transaction.
 *
 * @param txId            unique transaction identifier
 * @param accountId       associated account identifier
 * @param amount          monetary transaction value
 * @param timestamp       instant transaction was created
 * @param location        originating location
 * @param cardNumber      masked or tokenized card number
 * @param isInternational flag indicating cross-border transaction
 */
public record CardTransaction(
    String txId,
    String accountId,
    double amount,
    Instant timestamp,
    Location location,
    String cardNumber,
    boolean isInternational
) implements Transaction {
}
