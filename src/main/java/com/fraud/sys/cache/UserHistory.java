package com.fraud.sys.cache;

import com.fraud.sys.model.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Encapsulates the historical transactions for a specific user account.
 */
public class UserHistory {

    private final String accountId;
    private final List<Transaction> transactions;

    public UserHistory(String accountId) {
        this.accountId = accountId;
        this.transactions = new CopyOnWriteArrayList<>();
    }

    public UserHistory(String accountId, List<Transaction> initialTransactions) {
        this.accountId = accountId;
        this.transactions = new CopyOnWriteArrayList<>(initialTransactions);
    }

    /**
     * Appends a new transaction to the user's history.
     *
     * @param transaction the transaction to add
     */
    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            this.transactions.add(transaction);
        }
    }

    /**
     * Calculates the historical average transaction amount for this user using Java Streams API.
     *
     * @return the average transaction amount, or 0.0 if no history exists
     */
    public double calculateAverageAmount() {
        return transactions.stream()
                .mapToDouble(Transaction::amount)
                .average()
                .orElse(0.0);
    }

    public String getAccountId() {
        return accountId;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public int getTransactionCount() {
        return transactions.size();
    }
}
