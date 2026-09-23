package com.fraud.sys.rules;

import com.fraud.sys.model.CardTransaction;
import com.fraud.sys.model.Transaction;

/**
 * Geo-jumping rule that detects unexpected cross-border transactions for domestic accounts.
 * Fails if the transaction is flagged international or if the transaction country differs
 * from the domestic home country.
 */
public class GeoJumpingRule implements FraudRule {

    public static final String DEFAULT_DOMESTIC_COUNTRY = "INDIA";
    private final String domesticCountry;

    public GeoJumpingRule() {
        this(DEFAULT_DOMESTIC_COUNTRY);
    }

    public GeoJumpingRule(String domesticCountry) {
        this.domesticCountry = domesticCountry != null ? domesticCountry.trim() : DEFAULT_DOMESTIC_COUNTRY;
    }

    @Override
    public boolean evaluate(Transaction txn) {
        if (txn == null) {
            return false;
        }

        // Check if explicitly marked international on CardTransaction
        if (txn instanceof CardTransaction cardTxn && cardTxn.isInternational()) {
            return false; // Fails
        }

        // Check location country against domestic origin
        if (txn.location() != null && txn.location().country() != null) {
            String country = txn.location().country().trim();
            if (!country.equalsIgnoreCase(domesticCountry) && !"DOMESTIC".equalsIgnoreCase(country)) {
                return false; // Fails: international location for domestic account
            }
        }

        return true; // Passes
    }

    public String getDomesticCountry() {
        return domesticCountry;
    }
}
