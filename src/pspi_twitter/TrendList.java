/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pspi_twitter;

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
        // If exists renew stamp, else put it in to the hashmap
        if (trends.replace(t, new TrendData(Calendar.getInstance())) == null) {
            trends.put(t, new TrendData(Calendar.getInstance()));
        }
    }

    public String[] getNewTrendTracker() {
        List<String> s = new ArrayList<>();
        Set<Map.Entry<Trend, TrendData>> entrySet = trends.entrySet();
        for (Map.Entry<Trend, TrendData> entry : entrySet) 
            if (entry.getValue().timeElapsed() < TIMEOUT_IN_MINS)
                s.add(entry.getKey().getName());
        
        String[] sa = new String[s.size()];

        return s.toArray(sa);
    }
    
    public boolean isEmpty() {
        return trends.isEmpty();
    }

    private class TrendData {

        public Calendar time;
        //public boolean inUse = false;

        public TrendData(Calendar c) {
            time = c;
        }

        public long timeElapsed() {
            return (Calendar.getInstance().getTimeInMillis()
                    - time.getTimeInMillis()) / 60000;
        }
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

}
