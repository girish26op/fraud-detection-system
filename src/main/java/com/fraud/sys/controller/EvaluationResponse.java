package com.fraud.sys.controller;

import java.time.Instant;

/**
 * REST response representing transaction fraud evaluation outcome.
 */
public record EvaluationResponse(
    String txId,
    String status,      // "APPROVED" or "BLOCKED"
    String message,
    Instant timestamp
) {
    public static EvaluationResponse approved(String txId) {
        return new EvaluationResponse(txId, "APPROVED", "Transaction passed all fraud checks", Instant.now());
    }

    public static EvaluationResponse blocked(String txId, String reason) {
        return new EvaluationResponse(txId, "BLOCKED", reason, Instant.now());
    }
}
