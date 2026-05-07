package com.cardiolink.Controllers;

import java.util.ArrayList;
import java.util.List;

public class PredictionResponse {

    private String mois;
    private List<PredictionDayPeak> topJoursPic = new ArrayList<>();

    public PredictionResponse() {
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public List<PredictionDayPeak> getTopJoursPic() {
        return topJoursPic;
    }

    public void setTopJoursPic(List<PredictionDayPeak> topJoursPic) {
        this.topJoursPic = topJoursPic;
    }
}