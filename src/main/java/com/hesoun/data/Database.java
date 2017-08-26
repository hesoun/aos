package com.hesoun.data;

import com.hesoun.AosException;
import com.hesoun.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton representing a DB
 * @author Jakub Hesoun
 */
enum Database {
    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(Database.class);
    private Connection connection;

    synchronized Connection connect(Config config) {
        if(connection != null) {
            return connection;
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOG.error("Driver class has not been found");
            throw new AosException(e);
        }

        try {
            connection = DriverManager.getConnection(config.getDatabaseUrl(), config.getGetDatabaseUser(), config.getDatabasePassword());
        } catch (SQLException e) {
            LOG.error("Unable to obtain connection to the database");
            throw new AosException(e);
        }

        if (connection == null) {
            LOG.error("Unable to obtain connection to the database");
            throw new AosException("Unable to obtain connection to the database");
        }
        return connection;
    }
}
