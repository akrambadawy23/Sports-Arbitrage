
import jakarta.json.*;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {
    private static final String API_KEY = "bb58ea9e3c56a72fcdf0ed2ae6ec18dd";
    private static final String SPORT = "upcoming";
    private static final String REGIONS = "us,us2";
    private static final String MARKETS = "h2h";
    private static final String ODDS_FORMAT = "decimal";
    private static final String DATE_FORMAT = "iso";

    private static final int BET_SIZE = 100;


    public static void main(String[] args) throws Exception {
        //while (true) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());
        System.out.println(nowAsISO);
        int nowAsISO2 = Integer.parseInt(nowAsISO.substring(nowAsISO.lastIndexOf("T")+1, nowAsISO.lastIndexOf("T")+3));

            String odds = "https://api.the-odds-api.com/v4/sports/" + SPORT + "/odds?" +
                    "api_key=" + API_KEY +
                    "&regions=" + REGIONS +
                    "&markets=" + MARKETS +
                    "&oddsFormat=" + ODDS_FORMAT +
                    "&dateFormat=" + DATE_FORMAT;

            HttpClient httpclient = HttpClient.newHttpClient();
            HttpRequest httprequest = HttpRequest.newBuilder().uri(new URI(odds)).build();
            HttpResponse<String> response = httpclient.send(httprequest, HttpResponse.BodyHandlers.ofString());

            ArrayList<Match> matches = new ArrayList<Match>();
            ArrayList<Match> arbitrageMatches = new ArrayList<Match>();


            if (response.statusCode() == 200) {
                String jsonString = response.body();
                //System.out.println(jsonString);
                JsonReader reader = Json.createReader(new StringReader(jsonString));
                JsonArray temp1 = reader.readArray();
//System.out.println(temp1);
                for (int i = 0; i < temp1.size(); i++) {
                    JsonObject a = temp1.getJsonObject(i);
                    String s = a.getString("commence_time");
                    int s2 = Integer.parseInt(s.substring(s.lastIndexOf("T")+1, s.lastIndexOf("T")+3));
                    //System.out.println(s2 + " " + nowAsISO2);

if(nowAsISO2 < s2) {
    System.out.println(a.toString());

    //System.out.println("_______________");
    JsonArray b = (JsonArray) a.get("bookmakers");
    Match test = new Match(a);
    //System.out.println(test.outcomes);
    //System.out.println(b);
    matches.add(test);

//parse(a);
}

                }

            } else {
                System.out.println("HTTP error code: " + response.statusCode());
            }

            for (Match a : matches) {
                Object[][] best_odds = a.find_best_odds();
                if (a.arbitrage())
                    arbitrageMatches.add(a);
               // System.out.println("_________________________\n\n" + Arrays.deepToString(a.best_odds));
                //System.out.println(a.expectedEarnings);

            }

            for (Match a : arbitrageMatches) {
                System.out.println("_________________________");
                System.out.println(Arrays.toString(a.calculateBet()));
                a.convertToAmerican();
                System.out.println(Arrays.deepToString(a.best_odds));
                System.out.println(a.expectedEarnings + "%");
            }


      //  }
    }
}
