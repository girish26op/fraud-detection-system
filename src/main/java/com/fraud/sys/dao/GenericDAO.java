package com.fraud.sys.dao;

import java.sql.SQLException;

/**
 * Generic Data Access Object interface defining standard persistence operations.
 *
 * @param <T> entity type
 */
public interface GenericDAO<T> {

    /**
     * Persists the given entity to the database.
     *
     * @param entity the entity to save
     * @throws SQLException if a database access error occurs
     */
    void save(T entity) throws SQLException;
}
