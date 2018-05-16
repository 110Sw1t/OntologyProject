/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroblogsAnnotator;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ANNIEConstants;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.gui.*;
import javax.swing.SwingUtilities;
import gate.util.GateException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author khaled
 */
public class GateTool {

    static Corpus corpus;
    static Document doc;
    static String Inputpath;
    public static String Outputpath;
    public GateTool(String Inputpath,String Outputpath) {
        this.Inputpath = Inputpath;
        this.Outputpath = Outputpath;
        try {
            Gate.init();
            
            //SwingUtilities.invokeAndWait(() -> MainFrame.getInstance().setVisible(true));
            corpus = Factory.newCorpus("myCorpus");
            doc = Factory.newDocument(new File(Inputpath).toURI().toURL());
            corpus.add(doc);
        } catch (Exception ex) {
        }
    }

    private String MapToString(HashMap map) {
        String output = "";
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            output += pair.getKey() + " " + pair.getValue() + "\n";
            it.remove(); // avoids a ConcurrentModificationException
        }
        return output;
    }

    private String getStringFromAnnotationSet(AnnotationSet as) {
        String word = "";
        Iterator it = as.iterator();
        while (it.hasNext()) {
            Annotation tokenAnnotation = (Annotation) it.next();
            word += tokenAnnotation.getFeatures().get("string");

        }
        return word;
    }

    private String getTokens(AnnotationSet inputAnnSet, String[] types) {
        int i = 0;
        String word = "";
        String output = "";
        HashMap<String,Integer> map = new HashMap<String,Integer>();

        AnnotationSet tokenSet = doc.getAnnotations().get("Token");
        while (i < types.length) {
            AnnotationSet typeSet = doc.getAnnotations().get(types[i]);
            Iterator it = typeSet.iterator();
            while (it.hasNext()) {
                Annotation tokenAnnotation = (Annotation) it.next();

                if (types[i].equals("Organization") || types[i].equals("Person") || types[i].equals("Location")) {
                    AnnotationSet tmp = tokenSet.get(tokenAnnotation.getStartNode().getOffset(), tokenAnnotation.getEndNode().getOffset());
                    word = getStringFromAnnotationSet(tmp);
                } else if (types[i].equals("Hashtag")||types[i].equals("UserID")) {
                    word = (String) tokenAnnotation.getFeatures().get("string");
                    word = word.substring(1);
                } else {
                    word = (String) tokenAnnotation.getFeatures().get("string");

                }
                if(map.get(word) == null)
                {
                    map.put(word, 1);
                }
                
                else
                {
                 int freq = map.get(word);
                 map.put(word, freq+1);
                }
            }

            i++;
        }
        
        output = MapToString(map);

        return output;
    }

    private File createFile(String tokens) {
        File f = new File(Outputpath);

        try {
            PrintWriter writer = new PrintWriter(f);
            writer.println(tokens);
            writer.flush();
        } catch (IOException ex) {
        }
        return f;
    }

    private SerialAnalyserController CreateController(String pp, String PRs[]) {

        SerialAnalyserController pipeline = null;

        if (!pp.equals("")) {
            for (int i = 0; i < PRs.length; i++) {
                PRs[i] = pp + PRs[i];
            }
        }

        try {

            pipeline = (SerialAnalyserController) Factory
                    .createResource("gate.creole.SerialAnalyserController");

            for (int i = 0; i < PRs.length; i++) {
                ProcessingResource pr = (ProcessingResource) Factory.createResource(PRs[i]);
                pipeline.add(pr);
            }
        } catch (ResourceInstantiationException ex) {
        }

        return pipeline;
    }

    private void startPlugins(String[] plugins) {
        try {
            File pluginsDir = Gate.getPluginsHome();
            for (int i = 0; i < plugins.length; i++) {
                File aPluginDir = new File(pluginsDir, plugins[i]);
                Gate.getCreoleRegister().registerDirectories(aPluginDir.toURI().toURL());
            }

        } catch (Exception ex) {
            System.out.println("startPlugins failed");
            System.out.println(ex.getMessage());
        }
    }

    private String ANNIE() {
        String plugins[] = {"ANNIE"};
        startPlugins(plugins);

        try {
            String pp = "gate.creole.";
            String PRs[] = {
                "annotdelete.AnnotationDeletePR",
                "tokeniser.DefaultTokeniser",
                "gazetteer.DefaultGazetteer",
                "splitter.SentenceSplitter",
                "POSTagger",
                "ANNIETransducer",
                "orthomatcher.OrthoMatcher"};
            SerialAnalyserController pipeline = CreateController(pp, PRs);

            pipeline.setDocument(doc);
            pipeline.setCorpus(corpus);
            pipeline.execute();

        } catch (GateException ex) {
            System.out.println("ANNIE failed");
            System.out.println(ex.getMessage());
        }
        String[] types = {"Token"};
        return getTokens(doc.getAnnotations(), types);

    }

    private String TwitIE() {
        try {
            String plugins[] = {"ANNIE","Twitter",
                "Tools", "Stanford_CoreNLP", "Language_Identification"};

            //gate.stanford.Tagger
            String PRs[] = {
                "gate.creole.annotdelete.AnnotationDeletePR",
                "gate.creole.annotransfer.AnnotationSetTransfer",
                "org.knallgrau.utils.textcat.LanguageIdentifier",
                "gate.creole.gazetteer.DefaultGazetteer",
                "gate.twitter.tokenizer.TokenizerEN",
                "gate.twitter.HashtagTokenizer",
                "gate.creole.gazetteer.DefaultGazetteer",
                "gate.creole.splitter.SentenceSplitter",
                "gate.twitter.Normaliser",
                "gate.creole.POSTagger",
                "gate.creole.ANNIETransducer",
                "gate.creole.orthomatcher.OrthoMatcher"
            };

            startPlugins(plugins);

            SerialAnalyserController pipeline = CreateController("", PRs);

            pipeline.setCorpus(corpus);
            pipeline.execute();

        } catch (Exception ex) {
            System.out.println("TwitIE failed");
            System.out.println(ex.getMessage());
        }

        String[] types = {"Hashtag", "Organization", "Person", "UserID", "Location"};
        //String[] types= {"Hashtag"};
        return getTokens(doc.getAnnotations(), types);

    }

    public void RunANNIE() {
        String tokens = ANNIE();

        createFile(tokens);
    }

    public void RunTwitIE() {
        String tokens = TwitIE();
        createFile(tokens);
    }

}