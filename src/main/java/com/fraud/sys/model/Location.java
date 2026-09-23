package com.fraud.sys.model;

/**
 * Immutable record representing geographic location of a transaction.
 *
 * @param city    the city where transaction originated
 * @param country the country where transaction originated
 */
public record Location(String city, String country) {
    public Location {
        if (city == null || city.isBlank()) {
            city = "UNKNOWN";
        }
        if (country == null || country.isBlank()) {
            country = "UNKNOWN";
        }
    }
}
