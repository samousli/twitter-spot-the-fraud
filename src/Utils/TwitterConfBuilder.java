package Utils;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterConfBuilder {

	private static final String requestTokenString = "ER8jr24e1yXP0zU07a6ath0cb";
	private static final String requestSecretString = "ASySBOVsFZlm7GummsRBD2YbBoRAmvgRhOLWtKVfxTP3E0SS4g";

	private static final String accessTokenString = "2843777541-TMnA2qa58IYANJmcRXLvrtk8Mp75ybGn50Avi3n";
	private static final String accessSecretString = "shsIsXdkDlptw2yTL5vvOGVMXZMJzmsWXF2D0VMolJ7kj";

	public static Configuration buildConf() {

		// Configure to accept JSON files
		ConfigurationBuilder conf = new ConfigurationBuilder();
		conf.setJSONStoreEnabled(true);
		conf.setIncludeEntitiesEnabled(true);
		conf.setOAuthConsumerKey(requestTokenString);
		conf.setOAuthConsumerSecret(requestSecretString);
		conf.setOAuthAccessToken(accessTokenString);
		conf.setOAuthAccessTokenSecret(accessSecretString);

		return conf.build();
	}
}
