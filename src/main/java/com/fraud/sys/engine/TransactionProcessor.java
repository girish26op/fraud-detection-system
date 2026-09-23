package com.fraud.sys.engine;

import com.fraud.sys.cache.TransactionCache;
import com.fraud.sys.exceptions.FraudDetectedException;
import com.fraud.sys.model.CardTransaction;
import com.fraud.sys.model.Location;
import com.fraud.sys.model.Transaction;
import com.fraud.sys.model.UpiTransaction;
import com.fraud.sys.rules.FraudRule;
import com.fraud.sys.rules.GeoJumpingRule;
import com.fraud.sys.rules.VelocityRule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Core fraud detection engine that evaluates transactions using Java 21 pattern matching for switch.
 */
public class TransactionProcessor {

    private final FraudRule velocityRule;
    private final FraudRule geoJumpingRule;
    private final TransactionCache transactionCache;

    public TransactionProcessor() {
        this(new VelocityRule(), new GeoJumpingRule(), new TransactionCache());
    }

    public TransactionProcessor(FraudRule velocityRule, FraudRule geoJumpingRule, TransactionCache transactionCache) {
        this.velocityRule = velocityRule;
        this.geoJumpingRule = geoJumpingRule;
        this.transactionCache = transactionCache;
    }

    /**
     * Evaluates a live transaction using Java 21 switch pattern matching.
     *
     * @param txn the incoming transaction to inspect
     * @throws FraudDetectedException if any fraud validation rule fails
     */
    public void processLiveTransaction(Transaction txn) throws FraudDetectedException {
        if (txn == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        // Java 21 Pattern Matching for switch over sealed interface Transaction
        switch (txn) {
            case CardTransaction cardTxn -> {
                // Check velocity rule (max amount)
                if (!velocityRule.evaluate(cardTxn)) {
                    throw new FraudDetectedException(
                        "Card transaction [" + cardTxn.txId() + "] failed VelocityRule: Amount " 
                        + cardTxn.amount() + " exceeds limit of 100,000"
                    );
                }
                // Check geo-jumping rule (international on domestic account)
                if (!geoJumpingRule.evaluate(cardTxn)) {
                    throw new FraudDetectedException(
                        "Card transaction [" + cardTxn.txId() + "] failed GeoJumpingRule: International transaction detected at "
                        + cardTxn.location().city() + ", " + cardTxn.location().country()
                    );
                }
            }
            case UpiTransaction upiTxn -> {
                // Check velocity rule
                if (!velocityRule.evaluate(upiTxn)) {
                    throw new FraudDetectedException(
                        "UPI transaction [" + upiTxn.txId() + "] failed VelocityRule: Amount " 
                        + upiTxn.amount() + " exceeds limit of 100,000"
                    );
                }
                // Check VPA validity format
                if (upiTxn.vpaId() == null || !upiTxn.vpaId().contains("@")) {
                    throw new FraudDetectedException(
                        "UPI transaction [" + upiTxn.txId() + "] failed VpaRule: Invalid VPA address [" 
                        + upiTxn.vpaId() + "]"
                    );
                }
            }
        }

        // If all rules pass, update transaction cache
        if (transactionCache != null) {
            transactionCache.recordTransaction(txn);
        }
    }

    public TransactionCache getTransactionCache() {
        return transactionCache;
    }

    /**
     * Main entry point demonstrating Java 21 Virtual Threads concurrency.
     */
    public static void main(String[] args) {
        TransactionProcessor processor = new TransactionProcessor();

        // Generate 10 mock transactions: mix of valid and fraudulent transactions
        List<Transaction> mockTransactions = generateMockTransactions();

        System.out.println("======================================================================");
        System.out.println("Processing " + mockTransactions.size() + " transactions using Java 21 Virtual Threads");
        System.out.println("======================================================================");

        // Java 21 Virtual Thread Per Task Executor
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Transaction txn : mockTransactions) {
                executor.submit(() -> {
                    String threadName = Thread.currentThread().toString();
                    try {
                        processor.processLiveTransaction(txn);
                        System.out.printf("[APPROVED] TxId: %-10s | Account: %-10s | Amount: %10.2f | Thread: %s%n",
                                txn.txId(), txn.accountId(), txn.amount(), threadName);
                    } catch (FraudDetectedException e) {
                        System.err.printf("[BLOCKED]  TxId: %-10s | Account: %-10s | Reason: %s | Thread: %s%n",
                                txn.txId(), txn.accountId(), e.getMessage(), threadName);
                    }
                });
            }
        } // Executor auto-awaits virtual threads termination here

        System.out.println("======================================================================");
        System.out.println("Virtual thread processing completed successfully.");
    }

    private static List<Transaction> generateMockTransactions() {
        List<Transaction> list = new ArrayList<>();
        Location mumbai = new Location("Mumbai", "INDIA");
        Location london = new Location("London", "UK");
        Location bangalore = new Location("Bangalore", "INDIA");
        Location newYork = new Location("New York", "USA");

        // 1. Valid Card Transaction
        list.add(new CardTransaction("TXN-001", "ACC-101", 12500.0, Instant.now(), mumbai, "****-1234", false));
        // 2. High Velocity Card Transaction (> 100k, BLOCKED)
        list.add(new CardTransaction("TXN-002", "ACC-102", 150000.0, Instant.now(), mumbai, "****-5678", false));
        // 3. International Card Transaction (BLOCKED)
        list.add(new CardTransaction("TXN-003", "ACC-103", 4500.0, Instant.now(), london, "****-9012", true));
        // 4. Valid UPI Transaction
        list.add(new UpiTransaction("TXN-004", "ACC-104", 2500.0, Instant.now(), bangalore, "rahul@okaxis"));
        // 5. High Velocity UPI Transaction (> 100k, BLOCKED)
        list.add(new UpiTransaction("TXN-005", "ACC-105", 250000.0, Instant.now(), bangalore, "priya@okhdfc"));
        // 6. Valid Card Transaction
        list.add(new CardTransaction("TXN-006", "ACC-106", 890.0, Instant.now(), mumbai, "****-4321", false));
        // 7. Invalid UPI Transaction (Bad VPA, BLOCKED)
        list.add(new UpiTransaction("TXN-007", "ACC-107", 5000.0, Instant.now(), bangalore, "invalid_vpa_address"));
        // 8. International Location Card Transaction (BLOCKED)
        list.add(new CardTransaction("TXN-008", "ACC-108", 75000.0, Instant.now(), newYork, "****-8765", true));
        // 9. Valid UPI Transaction
        list.add(new UpiTransaction("TXN-009", "ACC-109", 18500.0, Instant.now(), bangalore, "amit@oksbi"));
        // 10. Valid Card Transaction
        list.add(new CardTransaction("TXN-010", "ACC-110", 34200.0, Instant.now(), mumbai, "****-1122", false));

        return list;
    }
}
