package com.superbool.easylpr.edges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kofee on 2016/3/9.
 */
public class ScoreKeeper {

    private static final Logger logger = LoggerFactory.getLogger(ScoreKeeper.class);

    List<String> weight_ids = new ArrayList<>();
    List<Float> weights = new ArrayList<>();
    List<Float> scores = new ArrayList<>();

    public ScoreKeeper() {
    }


    public void setScore(String weight_id, float score, float weight) {
        // Assume that we never set this value twice
        weight_ids.add(weight_id);
        scores.add(score);
        weights.add(weight);
    }


    public float getTotal() {

        float score = 0;

        for (int i = 0; i < weights.size(); i++) {
            score += scores.get(i) * weights.get(i);
        }

        return score;
    }


    public int size() {
        return weight_ids.size();
    }


    public void printDebugScores() {

        float total = getTotal();

        logger.info("--------------------");
        logger.info("Total: ", total);
        for (int i = 0; i < weight_ids.size(); i++) {
            float temp = scores.get(i) * weights.get(i);
            float percent_of_total = temp / total * 100;

            logger.info("{}  Weighted Score:{}  Orig Score:{}  {}% of total",
                    weight_ids.get(i), temp, scores.get(i), percent_of_total);
        }
        logger.info("--------------------");
    }

}
