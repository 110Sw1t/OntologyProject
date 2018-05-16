import tweepy
import json
import mwclient

hashtag = '#avengers'
consumer_token = 'UHGeTe83gyWyYixF7RPUNkLZ2'
consumer_secret = 'hIWynid8cNuMHMFvP5famIBPXCUXxYX5tBAWL5tSgoqswDO2f3'

auth = tweepy.OAuthHandler(consumer_token, consumer_secret)
api = tweepy.API(auth, wait_on_rate_limit=True);

text_file = open("tweets_time_series.txt", "w",encoding='utf-8')

i = 0
for tweet in tweepy.Cursor(api.search, lang = 'en', q=hashtag, rpp = 1).items():
    text_file.write(tweet._json['created_at'] + '\n')
	
text_file.close()