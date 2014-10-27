/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pspi_twitter;

import java.util.TimerTask;
import static pspi_twitter.PSPI_Twitter.accessSecretString;
import static pspi_twitter.PSPI_Twitter.accessTokenString;
import static pspi_twitter.PSPI_Twitter.requestSecretString;
import static pspi_twitter.PSPI_Twitter.requestTokenString;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.auth.AccessToken;

/**
 *
 * @author avail
 */
public class TrendGetter extends TimerTask {

    private boolean initialized = false;
    private AsyncTwitter twitter;
    
    private void initialize() {
        
        twitter = new AsyncTwitterFactory().getInstance();
        // Authenticate using the credentials (Tokens don't time out)
        twitter.setOAuthConsumer(requestTokenString, requestSecretString);
        twitter.setOAuthAccessToken(new AccessToken(accessTokenString, accessSecretString) );
        
        
        twitter.addListener(new AsyncTwitterListener());
        
        initialized = true;
    }
    
    @Override
    public void run() {
        if (!initialized)
            initialize();
        System.out.println(initialized);
        twitter.getPlaceTrends(1);
        
    }

    
    
}
