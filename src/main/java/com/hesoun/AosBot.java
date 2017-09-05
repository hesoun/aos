package com.hesoun;

import com.hesoun.extracting.DataExtractor;
import com.hesoun.trading.Simulator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Jakub Hesoun
 */
public class AosBot {

    public static void main(String... args) throws IOException, SQLException {
        Properties props = new Properties();
        props.load(DataExtractor.class.getResourceAsStream("/app.properties"));
        Config config = new Config(props);
        for (String arg : args) {
            if (arg.equals("extract")) {
                new DataExtractor(config).extract();
                break;
            } else if (arg.equals("simulate")) {
                new Simulator(config).simulate();
                break;
            }
        }
    }
}
