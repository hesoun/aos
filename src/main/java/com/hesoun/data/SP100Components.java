package com.hesoun.data;

import com.hesoun.extracting.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holder for all the S&P stock as stated on 19.8.2017 on wikipedia.
 *
 * @author Jakub Hesoun
 */
public class SP100Components {
    private static List<Pair<String, String>> components = new ArrayList<>(100);

    private SP100Components() {
        throw new AssertionError("No SP100Components class for you!. This is static helper class");
    }

    public static List<Pair<String, String>> load() {
        if (components.isEmpty()) {
            components.add(new Pair<>("AAPL", "Apple Inc."));
            components.add(new Pair<>("ABBV", "AbbVie Inc."));
            components.add(new Pair<>("ABT", "Abbott Laboratories"));
            components.add(new Pair<>("ACN", "Accenture plc"));
            components.add(new Pair<>("AGN", "Allergan plc"));
            components.add(new Pair<>("AIG", "American International Group Inc."));
            components.add(new Pair<>("ALL", "Allstate Corp."));
            components.add(new Pair<>("AMGN", "Amgen Inc."));
            components.add(new Pair<>("AMZN", "Amazon.com"));
            components.add(new Pair<>("AXP", "American Express Inc."));
            components.add(new Pair<>("BA", "Boeing Co."));
            components.add(new Pair<>("BAC", "Bank of America Corp"));
            components.add(new Pair<>("BIIB", "Biogen Idec"));
            components.add(new Pair<>("BK", "The Bank of New York Mellon"));
            components.add(new Pair<>("BLK", "BlackRock Inc"));
            components.add(new Pair<>("BMY", "Bristol-Myers Squibb"));
            components.add(new Pair<>("BRK.B", "Berkshire Hathaway"));
            components.add(new Pair<>("C", "Citigroup Inc"));
            components.add(new Pair<>("CAT", "Caterpillar Inc"));
            components.add(new Pair<>("CELG", "Celgene Corp"));
            components.add(new Pair<>("CL", "Colgate -Palmolive Co."));
            components.add(new Pair<>("CMCSA", "Comcast Corporation"));
            components.add(new Pair<>("COF", "Capital One Financial Corp."));
            components.add(new Pair<>("COP", "ConocoPhillips"));
            components.add(new Pair<>("COST", "Costco"));
            components.add(new Pair<>("CSCO", "Cisco Systems"));
            components.add(new Pair<>("CVS", "CVS Health"));
            components.add(new Pair<>("CVX", "Chevron"));
            components.add(new Pair<>("DD", "DuPont"));
            components.add(new Pair<>("DHR", "Danaher"));
            components.add(new Pair<>("DIS", "The Walt Disney Company"));
            components.add(new Pair<>("DOW", "Dow Chemical"));
            components.add(new Pair<>("DUK", "Duke Energy"));
            components.add(new Pair<>("EMR", "Emerson Electric Co."));
            components.add(new Pair<>("EXC", "Exelon"));
            components.add(new Pair<>("F", "Ford Motor"));
            components.add(new Pair<>("FB", "Facebook"));
            components.add(new Pair<>("FDX", "FedEx"));
            components.add(new Pair<>("FOX", "21st Century Fox"));
            components.add(new Pair<>("FOXA", "21st Century Fox"));
            components.add(new Pair<>("GD", "General Dynamics"));
            components.add(new Pair<>("GE", "General Electric Co."));
            components.add(new Pair<>("GILD", "Gilead Sciences"));
            components.add(new Pair<>("GM", "General Motors"));
            components.add(new Pair<>("GOOG", "Alphabet Inc"));
            components.add(new Pair<>("GOOGL", "Alphabet Inc"));
            components.add(new Pair<>("GS", "Goldman Sachs"));
            components.add(new Pair<>("HAL", "Halliburton"));
            components.add(new Pair<>("HD", "Home Depot"));
            components.add(new Pair<>("HON", "Honeywell"));
            components.add(new Pair<>("IBM", "International Business Machines"));
            components.add(new Pair<>("INTC", "Intel Corporation"));
            components.add(new Pair<>("JNJ", "Johnson&Johnson Inc"));
            components.add(new Pair<>("JPM", "JP Morgan Chase&Co"));
            components.add(new Pair<>("KHC", "Kraft Heinz"));
            components.add(new Pair<>("KMI", "Kinder Morgan Inc/DE"));
            components.add(new Pair<>("KO", "The Coca-Cola Company"));
            components.add(new Pair<>("LLY", "Eli Lilly and Company"));
            components.add(new Pair<>("LMT", "Lockheed -Martin"));
            components.add(new Pair<>("LOW", "Lowe 's"));
            components.add(new Pair<>("MA", "MasterCard Inc"));
            components.add(new Pair<>("MCD", "McDonald 's Corp"));
            components.add(new Pair<>("MDLZ", "Mondelēz International"));
            components.add(new Pair<>("MDT", "Medtronic Inc."));
            components.add(new Pair<>("MET", "Metlife Inc."));
            components.add(new Pair<>("MMM", "3M Company"));
            components.add(new Pair<>("MO", "Altria Group"));
            components.add(new Pair<>("MON", "Monsanto"));
            components.add(new Pair<>("MRK", "Merck&Co."));
            components.add(new Pair<>("MS", "Morgan Stanley"));
            components.add(new Pair<>("MSFT", "Microsoft"));
            components.add(new Pair<>("NEE", "NextEra Energy"));
            components.add(new Pair<>("NKE", "Nike"));
            components.add(new Pair<>("ORCL", "Oracle Corporation"));
            components.add(new Pair<>("OXY", "Occidental Petroleum Corp."));
            components.add(new Pair<>("PCLN", "Priceline Group Inc/The"));
            components.add(new Pair<>("PEP", "Pepsico Inc."));
            components.add(new Pair<>("PFE", "Pfizer Inc"));
            components.add(new Pair<>("PG", "Procter&Gamble Co"));
            components.add(new Pair<>("PM", "Phillip Morris International"));
            components.add(new Pair<>("PYPL", "PayPal Holdings"));
            components.add(new Pair<>("QCOM", "Qualcomm Inc."));
            components.add(new Pair<>("RTN", "Raytheon Company"));
            components.add(new Pair<>("SBUX", "Starbucks Corporation"));
            components.add(new Pair<>("SLB", "Schlumberger"));
            components.add(new Pair<>("SO", "Southern Company"));
            components.add(new Pair<>("SPG", "Simon Property Group, Inc."));
            components.add(new Pair<>("T", "AT&T Inc"));
            components.add(new Pair<>("TGT", "Target Corp."));
            components.add(new Pair<>("TWX", "Time Warner Inc."));
            components.add(new Pair<>("TXN", "Texas Instruments"));
            components.add(new Pair<>("UNH", "UnitedHealth Group Inc."));
            components.add(new Pair<>("UNP", "Union Pacific Corp."));
            components.add(new Pair<>("UPS", "United Parcel Service Inc"));
            components.add(new Pair<>("USB", "US Bancorp"));
            components.add(new Pair<>("UTX", "United Technologies Corp"));
            components.add(new Pair<>("V", "Visa Inc."));
            components.add(new Pair<>("VZ", "Verizon Communications Inc"));
            components.add(new Pair<>("WBA", "Walgreens Boots Alliance"));
            components.add(new Pair<>("WFC", "Wells Fargo"));
            components.add(new Pair<>("WMT", "Wal-Mart"));
            components.add(new Pair<>("XOM", "Exxon Mobil Corp"));
        }
        return Collections.unmodifiableList(components);
    }
}
