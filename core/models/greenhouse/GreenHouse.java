package models.greenhouse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GreenHouse {
    private static int ROW = 4;
    private static int COL = 5;

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

    public void setPots(Pot[][] pots) {
        this.pots = pots;
    }

    public String showGreenHouse() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Greenhouse Status ---\n");

        for (int i = 0; i < pots.length; i++) {
            for (int j = 0; j < pots[i].length; j++) {
                Pot currentPot = pots[i][j];

                sb.append("Pot [").append(i + 1).append("][").append(j + 1).append("]: ");

                switch (currentPot.getPotCondition()) {
                    case LOCKED -> sb.append("Locked");
                    case EMPTY -> sb.append("Empty");
                    case PLANTED -> {
                        String plantName;
                        if (currentPot.isItMari()) {
                            plantName = "Marigold";
                        } else if (currentPot.getPlantedPlant() != null) {
                            plantName = currentPot.getPlantedPlant().getName();
                        } else {
                            plantName = "Unknown";
                        }

                        sb.append("Growing - Plant: ").append(plantName)
                                .append(" | Time left: ")
                                .append(currentPot.getFormattedRemainingTime())
                                .append(" hours");
                    }
                    case COLLECTABLE -> sb.append("Ready");
                    default -> sb.append("Unknown State");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public Pot getSpecificPot(int x, int y) {
        return pots[x][y];
    }
}