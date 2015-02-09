1) Collecting tweets of all globally trending topics:
	- Set up a mongo server on localhost:28888 (mongo_up.sh can be used to just do that)
	- Run java -jar TweetCollector.jar for as long as desired. 

	This will produce 2 collections called trends and tweets in mongodb in a db called 'twitter_mini'
	In our case we collected approximately 40gb of tweets, within a span for 4 days coinciding with the Super Bowl in US.


2) Group by user:
	- Run TweetAnalytics.jar
	- Choose the first option  (This is a lengthy process)

	This will produce a collection named 'users' which contains basic info regarding the user.


3) Generate basic analytics
	- Run TweetAnalytics.jar
	- Choose the second option

	The result will be added fields to the 'users' collection.
	The analytical process is partly completed during the grouping pipeline, as in incurs practically no extra cost.
	Also during this step the number of a trends a user appears in will be counted.


4) Calculate quartiles
	- Run TweetAnalytics.jar
	- Choose the third option

	Calculates the quartiles and the median of the user collection by frequency.
	Also saves the quartile each user belongs to in the 'users' collection

	Sadly with our data the values we got were 1, 1, 2..Perhaps a mean value instead of a median would produce better results.
	There seemed to be noticeable number of spammers which we noticed from the content of their messages. But a random sample
	of 40 users among 880k didn't capture such representatives.
	

5) Choose 10 users per quartile and track them
	- Run TweetAnalytics.jar	(If time allows it, the process will be moved to TweetCollector.jar)
	- Choose the fourth option 

	Starts tracking the randomly chosen users streams and dumps their tweets into "chosen_user_tweets"
	During this step we noticed that there were some stray user ids, which we tied to retweets without investigating further.
	In order to avoid processing such tweets we filtered out the tweets which didn't belong to our initial list of users and 
	saved the results into the collection "chosen_user_tweets_filtered"


6) Generate detailed analytics
	- Run TweetAnalytics.jar
	- Choose the fifth option

	Calculates each asked for statistic in the 4b part of the project description and saves them into the collection "selectedUsers".


7) Export the generated results as JSON files
	- Run export_json.sh as root
	
	2 files will be generated for part 4a and 4b named all_users.sh and selected_users.sh respectively.

8) Export the generated results as CSV files
	- A fields.txt file has to be generated
	
