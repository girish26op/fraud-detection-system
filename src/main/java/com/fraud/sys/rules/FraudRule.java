package com.fraud.sys.rules;

import com.fraud.sys.model.Transaction;

/**
 * Functional contract for fraud rules.
 */
@FunctionalInterface
public interface FraudRule {

    /**
     * Evaluates whether the given transaction complies with this rule.
     *
     * @param txn the transaction to evaluate
     * @return {@code true} if the transaction passes the rule (no fraud detected);
     *         {@code false} if the transaction fails the rule (fraud suspected)
     */
    boolean evaluate(Transaction txn);
}
