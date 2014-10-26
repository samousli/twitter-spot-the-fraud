/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pspi_twitter;

import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 *
 * @author george
 */
public class PSPI_Twitter {

    private static final String requestTokenString = "ER8jr24e1yXP0zU07a6ath0cb";
    private static final String requestSecretString = "ASySBOVsFZlm7GummsRBD2YbBoRAmvgRhOLWtKVfxTP3E0SS4g";

    private static final long   userID = 2843777541L;
    private static final String accessTokenString = "2843777541-TMnA2qa58IYANJmcRXLvrtk8Mp75ybGn50Avi3n";
    private static final String accessSecretString = "shsIsXdkDlptw2yTL5vvOGVMXZMJzmsWXF2D0VMolJ7kj";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here

            Twitter twitter = TwitterFactory.getSingleton();
            twitter.setOAuthConsumer(requestTokenString, requestSecretString);
            
            /*
             * Request token is not required once you get  
             * the OAuth Access tokens from the API.
             */
            //RequestToken requestToken = twitter.getOAuthRequestToken();
            
            /*
             * Alternatively you can authenticate using a PIN given by 
             * the URL returned from the request token.
             */
            
            //System.out.println(requestToken.getAuthorizationURL());
            // ... Type PIN ...
            //AccessToken tok = twitter.getOAuthAccessToken(requestToken, pin);
            
            twitter.setOAuthAccessToken(new AccessToken(accessTokenString, 
                                                        accessSecretString) );
            

            // Get trends at global level (WOEID 1 = Global)
            // (Yahoo Where On Earth ID) 
            Trends placeTrends = twitter.trends().getPlaceTrends(1);

            for (Trend t : placeTrends.getTrends()) {
                System.out.println(t.getName());
            }

        } catch (TwitterException ex) {
            Logger.getLogger(PSPI_Twitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
