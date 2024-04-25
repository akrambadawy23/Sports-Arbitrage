import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import java.util.HashSet;

public class Match {
    public JsonObject data;
    public String sportkey;
    public JsonArray bookmakers;
    public JsonArray markets;
    public JsonArray outcomes;
    public String id;
    Object[][] best_odds;

    public Float impliedOdds;


    public Float expectedEarnings;

    public Float betSize = 100F;



    public Match(JsonObject data) {
        this.data = data;


        sportkey = data.get("sport_key").toString();
        id = data.get("id").toString();
        bookmakers = (JsonArray) data.get("bookmakers");
        markets = parse(bookmakers, "markets");
        outcomes = parse(markets, "outcomes");
        //System.out.println(bookmakers.size() + " " + markets.size() + " " + outcomes.size());
        if(outcomes != null)
        best_odds = new Object[outcomes.size()][3];


    }

    public Object[][] find_best_odds() {
        HashSet<String> visited = new HashSet<String>();
          int maxOutcomes = 0;
          if(outcomes != null)
              maxOutcomes = outcomes.size();
          else
              return null;

          Object[][] best_odds = new Object[maxOutcomes][3];

          for(int i = 0; i < maxOutcomes; i++) {
best_odds[i][0] = "None";
best_odds[i][1] = "None";
best_odds[i][2] = Float.NEGATIVE_INFINITY;

for(int j = 0; j < bookmakers.size(); j++) {
    JsonObject bookmaker = bookmakers.getJsonObject(j);
    JsonArray bookmakerOutcomes = bookmaker.getJsonArray("markets").getJsonObject(0).getJsonArray("outcomes");
    if(maxOutcomes == bookmakerOutcomes.size()) {
        for (int k = 0; k < bookmakerOutcomes.size(); k++) {
            //System.out.println(bookmakerOutcomes.size());
            JsonObject outcome = bookmakerOutcomes.getJsonObject(k);
            float book_odds = Float.parseFloat(outcome.get("price").toString());
            if (best_odds[k] == null || best_odds[k].length < 3) {
                best_odds[k] = new Object[3];
                best_odds[k][0] = "None";
                best_odds[k][1] = "None";
                best_odds[k][2] = Float.NEGATIVE_INFINITY;
            }
            Float current_best = null;
            if (best_odds[k][2] != null)
                current_best = (Float) best_odds[k][2];

            Boolean condition = (current_best != null) ? (book_odds > current_best) : false;
            if (condition && !visited.contains(bookmaker.get("title").toString())) {
                best_odds[k][0] = bookmaker.get("title").toString();
                visited.add(bookmaker.get("title").toString());
                best_odds[k][1] = outcome.get("name").toString();
                best_odds[k][2] = book_odds;
            }
        }
    }
}

          }
          this.best_odds = best_odds;
          return best_odds;

    }

    public boolean arbitrage() {
        this.impliedOdds = (float) 0;
        if(best_odds == null)
            return false;
        for(int i = 0; i < best_odds.length; i++) {
            if(best_odds[i][0].toString().equals("None"))
                return false;
            impliedOdds += (1/((Float)best_odds[i][2]));
        }
        this.impliedOdds = impliedOdds;
        this.expectedEarnings = (betSize / this.impliedOdds) - betSize;


        return impliedOdds < 1;
    }

    public Object[][] convertToAmerican() {
        Object[][] best_odds = this.best_odds;
        double american;
        for(int i = 0; i < best_odds.length; i++) {
            Float decimal = (Float) best_odds[i][2];
            if(decimal >= 2)
                american = (decimal - 1) * 100;
            else
                american = -100 / (decimal - 1);
            best_odds[i][2] = Math.round(american);
        }
        return best_odds;
    }

    public Float[] calculateBet() {
        Float[] betAmts = new Float[this.outcomes.size()];
        for(int i = 0; i < this.outcomes.size(); i++) {
            Float arbpercent = 1 / ((Float) this.best_odds[i][2]);
            Float betAmt =  (betSize * arbpercent) / this.impliedOdds;
            Float betAmtRound = (float) (Math.round(betAmt * 100.0) / 100.0);
            betAmts[i] = betAmtRound;
        }

    return betAmts;
    }

    public JsonArray parse(JsonArray jsonObj, String target) {
        if(jsonObj == null)
            return null;
        return parse("", "", jsonObj, target, 0);


    }


    public JsonArray parse(String grandParentKey, String parentKey, JsonArray json, String target, int count)  {
JsonObject a = null;
for(int i = 0; i < json.size(); i++) {
    JsonObject j = json.getJsonObject(i);
        for (String key : j.keySet()) {
            if (key.trim().equals((target))) {
                return (JsonArray) j.get(key);
            }
        }

        }
        return null;
    }
}
