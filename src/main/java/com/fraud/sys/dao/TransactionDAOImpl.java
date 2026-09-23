package com.fraud.sys.dao;

import com.fraud.sys.model.CardTransaction;
import com.fraud.sys.model.Transaction;
import com.fraud.sys.model.UpiTransaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * JDBC-based implementation of GenericDAO for Transaction persistence in MySQL/PostgreSQL.
 */
public class TransactionDAOImpl implements GenericDAO<Transaction> {

    private static final String INSERT_SQL = """
        INSERT INTO transactions (
            tx_id, account_id, amount, timestamp, city, country,
            tx_type, card_number, is_international, vpa_id, status
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private final DataSource dataSource;
    private final Connection staticConnection;

    /**
     * Production constructor injecting a connection pool DataSource.
     */
    public TransactionDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.staticConnection = null;
    }

    /**
     * Direct connection constructor for standalone or test scenarios.
     */
    public TransactionDAOImpl(Connection connection) {
        this.dataSource = null;
        this.staticConnection = connection;
    }

    private Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        if (staticConnection != null) {
            return staticConnection;
        }
        throw new SQLException("No DataSource or Connection configured for TransactionDAOImpl");
    }

    @Override
    public void save(Transaction entity) throws SQLException {
        if (entity == null) {
            throw new IllegalArgumentException("Transaction entity cannot be null");
        }

        boolean shouldCloseConn = (dataSource != null);
        Connection conn = getConnection();

        try {
            // Using try-with-resources on PreparedStatement
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {
                // 1. tx_id
                pstmt.setString(1, entity.txId());
                // 2. account_id
                pstmt.setString(2, entity.accountId());
                // 3. amount
                pstmt.setDouble(3, entity.amount());
                // 4. timestamp
                pstmt.setTimestamp(4, entity.timestamp() != null ? Timestamp.from(entity.timestamp()) : new Timestamp(System.currentTimeMillis()));
                // 5. city
                pstmt.setString(5, entity.location() != null ? entity.location().city() : "UNKNOWN");
                // 6. country
                pstmt.setString(6, entity.location() != null ? entity.location().country() : "UNKNOWN");

                // Type-specific attributes using Java 21 pattern matching
                switch (entity) {
                    case CardTransaction cardTxn -> {
                        pstmt.setString(7, "CARD");
                        pstmt.setString(8, cardTxn.cardNumber());
                        pstmt.setBoolean(9, cardTxn.isInternational());
                        pstmt.setNull(10, Types.VARCHAR); // vpa_id
                    }
                    case UpiTransaction upiTxn -> {
                        pstmt.setString(7, "UPI");
                        pstmt.setNull(8, Types.VARCHAR);  // card_number
                        pstmt.setNull(9, Types.BOOLEAN);  // is_international
                        pstmt.setString(10, upiTxn.vpaId());
                    }
                }

                // 11. status
                pstmt.setString(11, "RECORDED");

                pstmt.executeUpdate();
            }
        } finally {
            if (shouldCloseConn && conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }
}
