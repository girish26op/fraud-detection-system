package com.fraud.sys.controller;

import com.fraud.sys.model.CardTransaction;
import com.fraud.sys.model.Location;
import com.fraud.sys.model.Transaction;
import com.fraud.sys.model.UpiTransaction;

import java.time.Instant;

/**
 * Incoming REST payload representing a transaction evaluation request.
 */
public record TransactionRequest(
    String txId,
    String accountId,
    double amount,
    String city,
    String country,
    String type,               // "CARD" or "UPI"
    String cardNumber,         // Card specific
    Boolean isInternational,   // Card specific
    String vpaId               // UPI specific
) {
    /**
     * Converts the DTO request into a sealed Transaction domain model.
     */
    public Transaction toDomainModel() {
        Location loc = new Location(city != null ? city : "UNKNOWN", country != null ? country : "INDIA");
        Instant now = Instant.now();

        if ("UPI".equalsIgnoreCase(type)) {
            return new UpiTransaction(txId, accountId, amount, now, loc, vpaId);
        } else {
            // Default to CardTransaction
            return new CardTransaction(
                txId,
                accountId,
                amount,
                now,
                loc,
                cardNumber != null ? cardNumber : "N/A",
                Boolean.TRUE.equals(isInternational)
            );
        }
    }
}
