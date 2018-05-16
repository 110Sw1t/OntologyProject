import tweepy
import json
import mwclient

NO_OF_TWEETS = 100
NO_OF_WIKIPEDIA_PAGES = 10
hashtag = '#avengers'
consumer_token = 'UHGeTe83gyWyYixF7RPUNkLZ2'
consumer_secret = 'hIWynid8cNuMHMFvP5famIBPXCUXxYX5tBAWL5tSgoqswDO2f3'

auth = tweepy.OAuthHandler(consumer_token, consumer_secret)
api = tweepy.API(auth, wait_on_rate_limit=True);

json_file = open("Output_json.txt", "w",encoding='utf-8')
tweets_file = open("tweets.txt", "w",encoding='utf-8')
wikipedia_file = open("wikipedia_output.txt","w",encoding='utf-8')

for tweet in tweepy.Cursor(api.search, lang = 'en', q=hashtag, rpp = 100, return_type = 'popular',tweet_mode = 'extended').items(NO_OF_TWEETS):
    tweetString = ""
    if 'retweeted_status' in tweet._json:
        tweetString = tweet._json['retweeted_status']['full_text']
    else:
        tweetString = tweet.full_text
    tweets_file.write(tweetString + '\n\n\n')
    json.dump(tweet._json, json_file)
	
json_file.close()
tweets_file.close()

site = mwclient.Site('en.wikipedia.org')
i = 0
for result in site.search(hashtag):
    if(i == NO_OF_WIKIPEDIA_PAGES):
        break
    wikipedia_file.write(result.get('title') + '\n')
    i = i + 1  
	
wikipedia_file.close()