/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroblogsAnnotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static MicroblogsAnnotator.OntologyTimeSimilarity.wikipediaPageTitlesFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import java.util.Calendar;

/**
 *
 * @author fadia
 */
public class WikipediPageTimeSeries {
    
    
    //should be divisible by 2 for proper execution of calculate dissimilarity function
    public static final int TIMEPADDINGUNITS = 6;
    private static final String FUTUREPADDING = "future";
    private static final String PASTPADDING = "history";
    
    private static String formatDate(Date d){
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyyMMdd");
        return dt1.format(d);
    }
    
    private static Date padDate(Date d, int granularity, String type){
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        switch(type){
            case FUTUREPADDING:
                c.add(granularity, TIMEPADDINGUNITS/2);
                break;
            case PASTPADDING:
                c.add(granularity, -(TIMEPADDINGUNITS/2));
        }
        return c.getTime();
    }
    
    private static String getGranularity(int g){
        //wikimedia doesnt support any other granularity
        switch(g){
            case Calendar.DAY_OF_MONTH:
            case Calendar.DAY_OF_WEEK:
            case Calendar.DAY_OF_WEEK_IN_MONTH:
            case Calendar.DAY_OF_YEAR:
                return "daily";
            case Calendar.MONTH:
                return "monthly";
            default:
                System.out.println("Wikimedia doesnt support this granularity");
                return null;
        }
    }
    public static ArrayList<String> getWikipediaTitles() {
        ArrayList<String> titles = new ArrayList<>();
        BufferedReader br = null;
        FileReader fr = null;

        try {

            fr = new FileReader(wikipediaPageTitlesFile);
            br = new BufferedReader(fr);

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                titles.add(sCurrentLine);
            }

        } catch (IOException e) {
        } finally {
            try {
                br.close();
                fr.close();
            } catch (Exception ex) {
                Logger.getLogger(OntologyTimeSimilarity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return titles;
    }

    public static ArrayList<Long> getWikipediaPageTimeSeries(String title, Date startDate, Date endDate, int granularity) {
        title = title.replace(" ", "_");
        startDate = padDate(startDate, granularity, PASTPADDING);
        endDate = padDate(endDate, granularity, FUTUREPADDING);
        ArrayList<Long> timeSeries = new ArrayList<>();
        try {
            URL service = new URL("https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia/all-access/all-agents/" + title + "/"+getGranularity(granularity)+"/"+formatDate(startDate)+"/"+formatDate(endDate));
            URLConnection yc = service.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine = "";
            String next;
            while ((next = in.readLine()) != null) {
                inputLine += next;
            }
            in.close();
            JSONObject data = new JSONObject(inputLine);
            JSONArray items = data.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                //System.out.println("Time Stamp: " + items.getJSONObject(i).getString("timestamp"));
                //System.out.println("Views: " + items.getJSONObject(i).getString("views"));
                timeSeries.add(Long.parseLong(items.getJSONObject(i).getString("views")));
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(OntologyTimeSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OntologyTimeSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(OntologyTimeSimilarity.class.getName()).log(Level.SEVERE, null, ex);
        }
        return timeSeries;
    }

    
}
