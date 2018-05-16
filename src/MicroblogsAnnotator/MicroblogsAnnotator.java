/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroblogsAnnotator;

import java.util.Arrays;



/**
 *
 * @author khaled
 */
public class MicroblogsAnnotator {

    public static void main(String[] args) throws Exception {
        //add relative path here, will do it later
        String hashtag = "avengers";
        String inputPath = "tweets.txt";
        String OutputPath = "keywords.txt";
        Runtime.getRuntime().exec("python First-Block.py #"+hashtag).waitFor();
        Runtime.getRuntime().exec("python generate_candidates.py "+hashtag).waitFor();
        //Runtime.getRuntime().exec("getTweetsTimeStamps.py").waitFor(); //commented because the free api allows us to get only the tweets of the last 7 days,
                                                                         //and we managed to gather data for 2 weeks, by running it every 2 days
        GateTool g = new GateTool(inputPath, OutputPath);
        g.RunTwitIE();
        //g.RunANNIE(); 
        System.out.println(Arrays.toString(OntologiesContextSimilarity.getContextandTimingSimilarities(hashtag, OntologyTimeSimilarity.getTimingSimilarity())));
    }

}
