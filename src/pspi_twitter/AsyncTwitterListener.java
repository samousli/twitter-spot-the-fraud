/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pspi_twitter;

import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TwitterAdapter;

/**
 *
 * @author avail
 */
public class AsyncTwitterListener extends TwitterAdapter {

    @Override
    public void gotPlaceTrends(Trends trends) {
        System.out.println("In here");
        for (Trend t : trends.getTrends()) {
            System.out.println("TREND: " + t.getName());
            TrendList.getInstance().addTrend(t);

        }
    }
}