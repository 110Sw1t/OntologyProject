/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroblogsAnnotator;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Islam
 */
public class OntologiesContextSimilarity {

    /**
     * @param args the command line arguments
     */
    public static Tuple[] getContextandTimingSimilarities(String required_keyword, HashMap<String, Double> timing) {
        ArrayList<KeyWord> keywords = new ArrayList();
        try {
            /*
            assume all keywords (hashtags) are stored in a file called "keywords.txt" with line format as follows:
            keyword  PWH_for_keyword
             */
            BufferedReader kw = new BufferedReader(new FileReader("keywords.txt"));
            int total_freq = 0;
            while (kw.ready()) {
                String[] words = kw.readLine().split("\\s+");
                //create new KeyWord with params (keyword_name, PWH_for_keyword, number_of_candidate_pages)
                if(words.length < 2) break;
                int keyword_freq = Integer.parseInt(words[1]);
                total_freq += keyword_freq;
                if (words[0].equalsIgnoreCase(required_keyword)) {
                    keywords.add(new KeyWord(words[0], keyword_freq, new File(words[0]).listFiles().length, timing));
                }
                //break;
            }
            kw.close();
            for (int i = 0; i < keywords.size(); i++) {
                KeyWord keyword = keywords.get(i);
                keyword.setPWH(keyword.getPWH() / total_freq);
                for (int j = 0; j < keyword.scoreWithCandidate.length; j++) {
                    /*
                    assume all candidate wikipedia pages used for comparison with every keyword is found under "candidates directory"
                    in text format with files named "candidate0.txt" to "candidate{n-1}.txt"
                     */
                    //BufferedReader cd = new BufferedReader(new FileReader("candidates/candidate" + j + ".txt"));
                    BufferedReader cd = new BufferedReader(new FileReader(keyword.keyword + "/candidate" + j + ".txt"));

                    /*
                    calculate context similarity score as follows:
                    freq(keyword)/total length of document
                    this value is caluclated for each keyword with every candidate wikipedia page
                    may add later weighted likelihood concerning temporal edit of wikipedia page
                     */
                    CandidatePage cp = new CandidatePage();
                    cp.calcFrequencies(cd);
                    double value = 0;
                    for (double freq : cp.frequencies.values()) {
                        value += freq;
                    }
                    keyword.calcPWE(j, 0, cp.getValue(keyword.keyword));
                    keyword.setSigma_PWE(j, value);
                    cd.close();
                }
                keyword.calculateContextSimilarities();
                /*
                sort scores array based on higher score
                for now the sorting is based only on context similarity
                 */
                Arrays.sort(keyword.scoreWithCandidate, Collections.reverseOrder());
                return keyword.scoreWithCandidate;
            }
            return null;
        } catch (Exception ex) {
            Logger.getLogger(OntologiesContextSimilarity.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    
}

class KeyWord {

    String keyword;
    double PWH;
    double PWE[];
    double sigma_PWE[];
    Tuple scoreWithCandidate[];
    //means take only in consideration general page freq/length
    static double lambda = 0;

    public KeyWord(String keyword, double PWH, int pagesNum, HashMap<String, Double> timingSimilarityScores) {
        BufferedReader titles = null;
        try {
            this.keyword = keyword;
            this.PWH = PWH;
            this.PWE = new double[pagesNum];
            this.sigma_PWE = new double[pagesNum];
            this.scoreWithCandidate = new Tuple[pagesNum];
            titles = new BufferedReader(new FileReader("wikipedia_output.txt"));
            for (int i = 0; i < pagesNum; i++) {
                String title = titles.readLine();
                scoreWithCandidate[i] = new Tuple(title, timingSimilarityScores.get(title));
            }
        } catch (Exception ex) {
            Logger.getLogger(KeyWord.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                titles.close();
            } catch (IOException ex) {
                Logger.getLogger(KeyWord.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void calcPWE(int index, double PWMCT, double PWMC) {
        //PWMCT is the temporal edit freq/length, PWMC is general page freq/length
        this.PWE[index] = lambda * PWMCT + (1 - lambda) * PWMC;
    }

    public void setSigma_PWE(int index, double value) {
        this.sigma_PWE[index] = value;
    }

    public void calculateContextSimilarities() {
        //normalise scores calculated to be in range 0 to 1
        //normalisePWE();
        for (int index = 0; index < scoreWithCandidate.length; index++) {
            if(this.PWE[index]!=0)
                scoreWithCandidate[index].contextScore = Math.exp(-sigma_PWE[index] * (this.PWE[index] / this.PWH));
        }
        normaliseContext();
    }

    public void normalisePWE() {
        double req_min = 0, req_max = 1;
        double min = 1, max = 0;
        for (int i = 0; i < PWE.length; i++) {
            min = Math.min(min, PWE[i]);
            max = Math.max(max, PWE[i]);
        }
        for (int i = 0; i < PWE.length; i++) {
            PWE[i] = (((PWE[i] - min) / (max - min)) * (req_max - req_min)) + req_min;
        }
    }

    public void normaliseContext() {
        double req_min = 0, req_max = 1;
        double min = 1, max = 0;
        for (int i = 0; i < scoreWithCandidate.length; i++) {
            min = Math.min(min, scoreWithCandidate[i].contextScore);
            max = Math.max(max, scoreWithCandidate[i].contextScore);
        }
        for (int i = 0; i < scoreWithCandidate.length; i++) {
            scoreWithCandidate[i].contextScore = (((scoreWithCandidate[i].contextScore - min) / (max - min)) * (req_max - req_min)) + req_min;
        }
    }

    public double getPWH() {
        return PWH;
    }

    public void setPWH(double PWH) {
        this.PWH = PWH;
    }

}

class CandidatePage {

    HashMap<String, Double> frequencies;
    int total_length;

    public CandidatePage() {
        frequencies = new HashMap();
    }

    public void calcFrequencies(BufferedReader cd) {
        try {
            while (cd.ready()) {
                String[] words = cd.readLine().split("\\s+");

                total_length += words.length;
                for (String word : words) {
                    frequencies.put(word.toLowerCase(), frequencies.getOrDefault(word, 0.0) + 1);
                }
            }
            for (String word : frequencies.keySet()) {
                frequencies.put(word, frequencies.get(word) / total_length);
            }
        } catch (IOException ex) {
            Logger.getLogger(CandidatePage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public double getValue(String word) {
        return frequencies.getOrDefault(word.toLowerCase(),0.0);
    }
}
//A class to hold the entry mapping between the keyword and the wikipedia page, to be used to store similarity scores and later sort

class Tuple implements Comparable {

    String index;
    double contextScore;
    double timingScore;
    static double weight_mean = 0.5;

    public Tuple(String index, Double timingScore) {
        this.index = index;
        this.timingScore = timingScore;
    }
    //double timingScore

    @Override
    public int compareTo(Object o) {
        double mean = weight_mean * contextScore + (1 - weight_mean) * timingScore;
        double omean = (weight_mean * ((Tuple) o).contextScore + (1 - weight_mean) * ((Tuple) o).timingScore);
        if (mean < omean) {
            return -1;
        } else if (mean == omean) {
            return 0;
        }
        return 1;
    }

    @Override
    public String toString() {
        return index + ": context: " + contextScore + ", timing: " + timingScore + "\n";
    }

}
