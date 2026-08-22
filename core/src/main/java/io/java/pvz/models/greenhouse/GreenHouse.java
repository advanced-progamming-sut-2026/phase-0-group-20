package io.java.pvz.models.greenhouse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GreenHouse {
    private static int ROW = 3;
    private static int COL = 4;

    @JsonProperty("pots")
    private Pot[][] pots = new Pot[ROW][COL];

    public GreenHouse() {
        for (int i = 0; i < pots.length; i++) {
            for (int j = 0; j < pots[i].length; j++) {
                pots[i][j] = new Pot();
                if (i >= 1)
                    pots[i][j].setPotCondition(PotCondition.LOCKED);
                else {
                    pots[i][j].setPotCondition(PotCondition.EMPTY);
                }
            }
        }
    }

    @JsonIgnore
    public static int getROW() {
        return ROW;
    }

    @JsonIgnore
    public static int getCOL() {
        return COL;
    }

    public Pot[][] getPots() {
        return pots;
    }

    public Pot getSpecificPot(int x, int y) {
        return pots[x][y];
    }
}
