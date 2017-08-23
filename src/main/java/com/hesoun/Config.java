package com.hesoun;

import lombok.Getter;

import java.time.LocalDate;
import java.util.Properties;

/**
 * @author Jakub Hesoun
 */
@Getter
public class Config {
    private final String yahooUrl;
    private final LocalDate from;
    private final LocalDate to;
    private final String databaseUrl;
    private final String getDatabaseUser;
    private final String databasePassword;

    public Config(Properties properties) {
        yahooUrl = properties.getProperty("yahoo.url");
        from = LocalDate.parse(properties.getProperty("date.from"));
        to = LocalDate.parse(properties.getProperty("date.to"));
        databaseUrl = properties.getProperty("database.url");
        getDatabaseUser = properties.getProperty("database.user");
        databasePassword = properties.getProperty("database.password");
    }
}
