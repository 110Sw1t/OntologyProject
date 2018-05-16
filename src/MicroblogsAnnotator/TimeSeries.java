/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MicroblogsAnnotator;

import java.util.ArrayList;

import java.util.Date;
import java.util.Calendar;
/**
 *
 * @author fadia
 */
public class TimeSeries {
    private final Date pivot;
    private final int granularity;
    private final ArrayList<Long> timeSeriesData;

    private static final double densityThreshold = 0.6;
    
    public static TimeSeries getDensePartOfTimeSerious(TimeSeries ts){
        double dataSum = 0;
        //get summation of all timeseries entries
        for(long data : ts.getTimeSeriesData()) dataSum += data;
        boolean moved = true;
        int smallPointer = 0, bigPointer = 0;
        int timeLineDifference = Integer.MAX_VALUE;
        int smallPointerCheckPoint = 0, bigPointerCheckPoint = 0;
        ArrayList<Long> dataArray = ts.getTimeSeriesData();
        double rollingSum = dataArray.get(0);
        while(moved){
            moved = false;
            if(rollingSum/dataSum > densityThreshold){
                int newTimelineDifference = bigPointer - smallPointer;
                if(newTimelineDifference < timeLineDifference){
                    timeLineDifference = newTimelineDifference;
                    smallPointerCheckPoint = smallPointer;
                    bigPointerCheckPoint = bigPointer;
                }
                rollingSum -= dataArray.get(smallPointer);
                smallPointer++;
                moved = true;
            }else if (rollingSum/dataSum < densityThreshold && bigPointer+1 < dataArray.size()){
                bigPointer++;
                rollingSum += dataArray.get(bigPointer);
                moved = true;
            }
            
        }
        Calendar c = Calendar.getInstance();
        c.setTime(ts.getStartPivot());
        c.add(ts.getGranularity(), smallPointerCheckPoint);
        return new TimeSeries(c.getTime(), smallPointer, new ArrayList<>(dataArray.subList(smallPointerCheckPoint, bigPointerCheckPoint + 1)));
    }
    
    public TimeSeries(Date pivot, int granularity, ArrayList<Long> timeSeriesData) {
        this.pivot = pivot;
        this.granularity = granularity;
        this.timeSeriesData = timeSeriesData;
    }

    public Date getStartPivot() {
        return pivot;
    }

    
    public Date getEndPivot() {
        Calendar c = Calendar.getInstance();
        c.setTime(pivot);
        c.add(this.granularity, this.timeSeriesData.size()-1);
        return c.getTime();
    }

    
    public int getGranularity() {
        return granularity;
    }

    public ArrayList<Long> getTimeSeriesData() {
        return timeSeriesData;
    }

    
}
