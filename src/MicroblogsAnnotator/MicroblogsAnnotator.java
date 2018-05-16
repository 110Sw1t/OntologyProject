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
        String inputPath="tweets.txt";
        String OutputPath="keywords.txt";
        GateTool g = new GateTool(inputPath,OutputPath);
        g.RunTwitIE();
        //g.RunANNIE(); 
        //System.out.println(Arrays.toString(OntologiesContextSimilarity.getContextandTimingSimilarities(OntologyTimeSimilarity.getTimingSimilarity())));
    }

}
