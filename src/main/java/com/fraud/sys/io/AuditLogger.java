package com.fraud.sys.io;

import com.fraud.sys.model.CardTransaction;
import com.fraud.sys.model.Transaction;
import com.fraud.sys.model.UpiTransaction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Audit logger that writes fraudulent transaction alerts to a persistent log file
 * using Java character streams with try-with-resources.
 */
public class AuditLogger {

    public static final String DEFAULT_LOG_FILE = "audit_alerts.log";
    private final File logFile;

    public AuditLogger() {
        this(DEFAULT_LOG_FILE);
    }

    public AuditLogger(String filePath) {
        this.logFile = new File(filePath);
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create audit log file: " + e.getMessage());
        }
    }

    /**
     * Appends a blocked transaction record to the audit log file.
     * Uses try-with-resources over BufferedWriter and FileWriter in append mode.
     *
     * @param txn    the blocked transaction
     * @param reason the reason or fraud rule that caused rejection
     */
    public synchronized void logBlockedTransaction(Transaction txn, String reason) {
        if (txn == null) {
            return;
        }

        String txType = switch (txn) {
            case CardTransaction c -> "CARD";
            case UpiTransaction u -> "UPI";
        };

        String logEntry = String.format(
            "[%s] [BLOCKED] TxId=%s | Type=%s | Account=%s | Amount=%.2f | Location=%s,%s | Reason=%s%n",
            Instant.now(),
            txn.txId(),
            txType,
            txn.accountId(),
            txn.amount(),
            txn.location().city(),
            txn.location().country(),
            reason
        );

        // Try-with-resources ensures BufferedWriter and FileWriter are properly flushed and closed
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, StandardCharsets.UTF_8, true))) {
            writer.write(logEntry);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to audit log file: " + e.getMessage());
        }
    }

    public File getLogFile() {
        return logFile;
    }
}
