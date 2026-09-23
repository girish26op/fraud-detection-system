package com.fraud.sys.cache;

import com.fraud.sys.model.Transaction;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe cache using ConcurrentHashMap to store and retrieve
 * UserHistory instances keyed by accountId.
 */
public class TransactionCache {

    private final ConcurrentHashMap<String, UserHistory> userHistories;

    public TransactionCache() {
        this.userHistories = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves the UserHistory for a given accountId, or null if none exists.
     *
     * @param accountId account identifier
     * @return the UserHistory or null
     */
    public UserHistory getHistory(String accountId) {
        if (accountId == null) {
            return null;
        }
        return userHistories.get(accountId);
    }

    /**
     * Retrieves an Optional of UserHistory for a given accountId.
     *
     * @param accountId account identifier
     * @return Optional containing UserHistory if present
     */
    public Optional<UserHistory> findHistory(String accountId) {
        return Optional.ofNullable(getHistory(accountId));
    }

    /**
     * Stores or updates the transaction in the user's history in a thread-safe manner.
     *
     * @param txn transaction to store
     */
    public void recordTransaction(Transaction txn) {
        if (txn == null || txn.accountId() == null) {
            return;
        }
        userHistories.computeIfAbsent(txn.accountId(), UserHistory::new)
                     .addTransaction(txn);
    }

    /**
     * Computes the historical average transaction amount for the given account.
     *
     * @param accountId account identifier
     * @return historical average amount, or 0.0 if user has no history
     */
    public double getHistoricalAverage(String accountId) {
        UserHistory history = getHistory(accountId);
        return history != null ? history.calculateAverageAmount() : 0.0;
    }

    /**
     * Returns total number of cached accounts.
     */
    public int size() {
        return userHistories.size();
    }

    /**
     * Clears all cached histories.
     */
    public void clear() {
        userHistories.clear();
    }
}
