/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroblogsAnnotator;

/**
 *
 * @author fadia
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OntologyTimeSimilarity {

    public static final String wikipediaPageTitlesFile = "wikipedia_output.txt";
    public static final ArrayList<Long> allDissimilarities = new ArrayList<>();
    
    public static final int SCALEFACTORRANGE = 20;
    public static final int NBESTDISSIMILARITYSCORES = 10;

    public static ArrayList<Double> scalingFactorRange(int range) {
        ArrayList<Double> sf = new ArrayList<>();
        
//        sf.add(0.25);
//        sf.add(0.5);
        sf.add(1.0);
//        sf.add(2.0);
//        sf.add(4.0);
//        sf.add(0.05);
//        sf.add(1.0/0.05);
//        sf.add(.001);
//        sf.add(100.0);
        
        for (int i = 2; i <= range; i++) {
            sf.add((double) i);
            sf.add(1.0 / i);
        }


        return sf;
    }

    /**
     * @return
     */
    public static double calculateDissimilarity(ArrayList<Long> ts1, ArrayList<Long> ts2) {
        //ts1 wikipedia time series
        //ts2 tweet time series
        double minDissimilarity = Double.MAX_VALUE;
        ArrayList<Double> scalingFactors = scalingFactorRange(SCALEFACTORRANGE);
        ArrayList<Long> bestNDissimilarities = new ArrayList<>();
        for (Double sf : scalingFactors) {
            for (int j = 0; j < (ts1.size() - ts2.size() + 1); j++) {
                long dissimilarityScore = 0;
                for (int i = 0; i < ts2.size(); i++) {
                    double pit1 = ts1.get(i + j) * sf;
                    double pit2 = ts2.get(i);
                    double difference = pit1 - pit2;
                    dissimilarityScore += Math.abs(difference);
                }
                if (minDissimilarity > dissimilarityScore) {
                    minDissimilarity = dissimilarityScore;
                }
                if (bestNDissimilarities.size() < NBESTDISSIMILARITYSCORES) {
                    bestNDissimilarities.add(dissimilarityScore);
                }else{
                    int maxIndex = 0;
                    for(int i = 1; i < bestNDissimilarities.size(); i++) {
                        if(bestNDissimilarities.get(i) > bestNDissimilarities.get(maxIndex)) maxIndex = i;
                    }
                    if(bestNDissimilarities.get(maxIndex) > dissimilarityScore) bestNDissimilarities.add(maxIndex, dissimilarityScore);
                }
            }
        }
        for(int i = 0; i < bestNDissimilarities.size(); i++) allDissimilarities.add(bestNDissimilarities.get(i));
        return minDissimilarity;
    }

    public static HashMap<String, Double> getSimilarityScores(HashMap<String, Double> dissimilarityScores) {
        double sum = 0;
        for (double dissimilarityScore : dissimilarityScores.values()) {
            sum += dissimilarityScore;
        }
        double average = sum / dissimilarityScores.size();
        double absoluteDissimilarityMaximum = average * 2;
        HashMap<String, Double> similiratiyScores = new HashMap<>();
        for (Map.Entry<String, Double> entry : dissimilarityScores.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();
            double similiarityScore =  (value / absoluteDissimilarityMaximum);
            if (similiarityScore < 0) {
                similiarityScore = 0.0;
            }
            similiratiyScores.put(key, similiarityScore);
        }
        return similiratiyScores;
    }

    
    public static HashMap<String, Double> getTimingSimilarity() {

        HashMap<String, Double> dissimilarityScores = new HashMap<>();
        ArrayList<String> titles = WikipediPageTimeSeries.getWikipediaTitles();
        TimeSeries hashtageTimeSeries = HashtagTimeSeries.getTweetsTimeSeries(HashtagTimeSeries.TWEETSTIMEDATAFILE, Calendar.DAY_OF_MONTH);
        for (String t : titles) {
            ArrayList<Long> ts = WikipediPageTimeSeries.getWikipediaPageTimeSeries(t, hashtageTimeSeries.getStartPivot(), hashtageTimeSeries.getEndPivot(), hashtageTimeSeries.getGranularity());
            dissimilarityScores.put(t, calculateDissimilarity(ts, hashtageTimeSeries.getTimeSeriesData()));
        }
        dissimilarityScores = getSimilarityScores(dissimilarityScores);
        normalize(dissimilarityScores);
        return dissimilarityScores;
//        normalize(similarityScores);
    }

    public static void normalize(HashMap<String, Double> sss) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (Map.Entry<String, Double> entry : sss.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();
            if (value > max) {
                max = value;
            }
            if (value < min) {
                min = value;
            }
        }
        for (Map.Entry<String, Double> entry : sss.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();
            double newVal = ((value - min) / (max - min));
            sss.put(key, newVal);
        }
    }
}
