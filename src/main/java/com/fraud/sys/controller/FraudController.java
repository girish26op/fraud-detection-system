package com.fraud.sys.controller;

import com.fraud.sys.engine.TransactionProcessor;
import com.fraud.sys.exceptions.FraudDetectedException;
import com.fraud.sys.io.AuditLogger;
import com.fraud.sys.model.Transaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot REST Controller exposing fraud evaluation endpoints.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/fraud")
public class FraudController {

    private final TransactionProcessor processor;
    private final AuditLogger auditLogger;

    public FraudController() {
        this(new TransactionProcessor(), new AuditLogger());
    }

    public FraudController(TransactionProcessor processor, AuditLogger auditLogger) {
        this.processor = processor;
        this.auditLogger = auditLogger;
    }

    /**
     * Evaluates an incoming transaction against fraud rules.
     *
     * @param request the transaction payload
     * @return EvaluationResponse with APPROVED or BLOCKED status and timestamp
     */
    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationResponse> evaluateTransaction(@RequestBody TransactionRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(
                EvaluationResponse.blocked("UNKNOWN", "Request payload cannot be null")
            );
        }

        Transaction transaction = request.toDomainModel();

        try {
            // Evaluate rules through core engine
            processor.processLiveTransaction(transaction);

            // Successfully passed rules
            return ResponseEntity.ok(EvaluationResponse.approved(transaction.txId()));

        } catch (FraudDetectedException ex) {
            // Transaction blocked by rule violation - log to audit file
            auditLogger.logBlockedTransaction(transaction, ex.getMessage());

            // Return BLOCKED response with timestamp
            return ResponseEntity.ok(EvaluationResponse.blocked(transaction.txId(), ex.getMessage()));
        }
    }

    /**
     * Retrieves account historical metrics from the in-memory cache.
     */
    @GetMapping("/history/{accountId}")
    public ResponseEntity<Map<String, Object>> getUserHistory(@PathVariable String accountId) {
        double avg = processor.getTransactionCache().getHistoricalAverage(accountId);
        var hist = processor.getTransactionCache().getHistory(accountId);
        int count = (hist != null) ? hist.getTransactionCount() : 0;
        return ResponseEntity.ok(Map.of(
            "accountId", accountId,
            "transactionCount", count,
            "averageAmount", avg
        ));
    }

    /**
     * Retrieves recent alerts from the audit log file.
     */
    @GetMapping("/audit-log")
    public ResponseEntity<List<String>> getAuditLogs() {
        File file = auditLogger.getLogFile();
        if (!file.exists()) {
            return ResponseEntity.ok(List.of());
        }
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            return ResponseEntity.ok(lines);
        } catch (IOException e) {
            return ResponseEntity.ok(List.of("Error reading audit log: " + e.getMessage()));
        }
    }
}
