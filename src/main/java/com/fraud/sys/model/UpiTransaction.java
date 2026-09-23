package com.fraud.sys.model;

import java.time.Instant;

/**
 * Record representing a Unified Payments Interface (UPI) transaction.
 *
 * @param txId      unique transaction identifier
 * @param accountId associated account identifier
 * @param amount    monetary transaction value
 * @param timestamp instant transaction was created
 * @param location  originating location
 * @param vpaId     Virtual Payment Address (e.g., username@bank)
 */
public record UpiTransaction(
    String txId,
    String accountId,
    double amount,
    Instant timestamp,
    Location location,
    String vpaId
) implements Transaction {
}
