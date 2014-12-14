/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetCollector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import twitter4j.Trend;

/**
 *
 * @author avail
 */
public class TrendList {

    public static final int TIMEOUT_IN_MINS = 120;
    public int curr_index = 0;

    private final ConcurrentMap<Trend, TrendData> trends = new ConcurrentHashMap<>();

    public void addTrend(Trend t) {
        // If it exists renew stamp, else put it in the map
        if (trends.replace(t, new TrendData(Calendar.getInstance())) == null) {
        	trends.put(t, new TrendData(Calendar.getInstance()));
        }
    }
    /*
     * None of the trends are deleted.
     * Every time a new trend tracking list is to be generated
     * The whole collection is filtered down to trends which are
     * considered new and valid as defined by TIMEOUT_IN_MINS
     */
    public String[] getNewTrendTracker() {
        List<String> s = new ArrayList<>();
        Set<Map.Entry<Trend, TrendData>> entrySet = trends.entrySet();
        for (Map.Entry<Trend, TrendData> entry : entrySet) 
            if (entry.getValue().timeElapsedinMinutes() < TIMEOUT_IN_MINS)
                s.add(entry.getKey().getName());
        
        String[] sa = new String[s.size()];

        return s.toArray(sa);
    }
    
    public boolean isEmpty() {
        return trends.isEmpty();
    }
    
    public void postAllTrendsToDB() {
    	// All trends can be serialized from here
    	// At shutdown 
    }
    
    
    // Singleton Code
    private TrendList() {
    }

    public static TrendList getInstance() {
        return TrendListHolder.INSTANCE;
    }

    private static class TrendListHolder {

        private static final TrendList INSTANCE = new TrendList();
    }
    
    // Container class
    private class TrendData {

        public Calendar time;
        //public boolean inUse = false;

        public TrendData(Calendar c) {
            time = c;
        }

        public long timeElapsedinMinutes() {
            return (Calendar.getInstance().getTimeInMillis()
                    - time.getTimeInMillis()) / 60000;
        }
    }
}
