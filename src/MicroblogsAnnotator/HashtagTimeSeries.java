/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroblogsAnnotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Collections;

/**
 *
 * @author fadia
 */
public class HashtagTimeSeries {

    public final static String TWEETSTIMEDATAFILE = "tweets_time_series.txt";
    public static final String HASHTAGFREQUENCIESTIMESERIES = "8";
    public static final String TIMESERIESPIVOT = "3";

    public static Date getDate(String dateString) {
        dateString = dateString.replace(" +0000", "");
        LocalDateTime ldt = LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss uuuu").withResolverStyle(ResolverStyle.STRICT));
        ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    
    public static TimeSeries getTweetsTimeSeries(String tweetsTimeDataFile, int granularity){
        //data from tweet file are in reversed chronological order
        BufferedReader br = null;
        FileReader fr = null;
        Calendar calendar = Calendar.getInstance();
        ArrayList<Long> tweetFrequencyInWeeks = new ArrayList<>();
        Date pivot = null;
        try {

            fr = new FileReader(tweetsTimeDataFile);
            br = new BufferedReader(fr);

            String currentLine = br.readLine();
            pivot = getDate(currentLine);
            
            calendar.setTime(pivot);
            calendar.add(granularity, -1);
            Date endDateOfPeriod = calendar.getTime();
            
            while (currentLine != null) {
                long sum = 1;
                while (((currentLine = br.readLine()) != null) && getDate(currentLine).after(endDateOfPeriod)) {
                    sum++;
                }
                calendar.setTime(endDateOfPeriod);
                calendar.add(granularity, -1);
                endDateOfPeriod = calendar.getTime();
                tweetFrequencyInWeeks.add(sum);
            }
        } catch (Exception e) {
        }
        
        calendar.setTime(pivot);
        calendar.add(granularity, -tweetFrequencyInWeeks.size());
        pivot = calendar.getTime();
        Collections.reverse(tweetFrequencyInWeeks);
        return new TimeSeries(pivot, granularity, tweetFrequencyInWeeks);
    }
    

}
