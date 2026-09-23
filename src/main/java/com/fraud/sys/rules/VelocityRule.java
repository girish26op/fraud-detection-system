package com.fraud.sys.rules;

import com.fraud.sys.model.Transaction;

/**
 * Velocity rule that flags transactions exceeding a maximum monetary threshold.
 * Fails if transaction amount exceeds 100,000.
 */
public class VelocityRule implements FraudRule {

    public static final double DEFAULT_MAX_AMOUNT = 100_000.0;
    private final double maxAmount;

    public VelocityRule() {
        this(DEFAULT_MAX_AMOUNT);
    }

    public VelocityRule(double maxAmount) {
        this.maxAmount = maxAmount;
    }

    @Override
    public boolean evaluate(Transaction txn) {
        if (txn == null) {
            return false;
        }
        // Fails if amount > 100,000
        return txn.amount() <= maxAmount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }
}
