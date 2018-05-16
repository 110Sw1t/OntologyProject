import json
import mwclient
import tweepy
import re
import os

NO_OF_TWEETS = 100
NO_OF_WIKIPEDIA_PAGES = 10
hashtag = 'avengers'
#consumer_token = 'UHGeTe83gyWyYixF7RPUNkLZ2'
#consumer_secret = 'hIWynid8cNuMHMFvP5famIBPXCUXxYX5tBAWL5tSgoqswDO2f3'

#auth = tweepy.OAuthHandler(consumer_token, consumer_secret)
#api = tweepy.API(auth, wait_on_rate_limit=True);

#json_file = open("Output_json.txt", "w",encoding='utf-8')
#tweets_file = open("tweets.txt", "w",encoding='utf-8')
#wikipedia_file = open("wikipedia_output.txt","r",encoding='utf-8')

#for tweet in tweepy.Cursor(api.search, lang = 'en', q=hashtag, rpp = 100, return_type = 'popular',tweet_mode = 'extended').items(NO_OF_TWEETS):
#    tweetString = ""
#    if 'retweeted_status' in tweet._json:
#        tweetString = tweet._json['retweeted_status']['full_text']
#    else:
#        tweetString = tweet.full_text
#    tweets_file.write(tweetString + '\n\n\n')
#    json.dump(tweet._json, json_file)
	
#json_file.close()
#tweets_file.close()

site = mwclient.Site('en.wikipedia.org')
i = 0
rep = {'=':' ',
'[':' ',
']':' ',
'{':' ',
'}':' ',
'(':' ',
')':' ',
',':' ',
'<':' ',
'>':' ',
'\'s':' ',
'\'':' ',
'\"':' ',
':':' ',
'.':' ',
'|':' ',
'!':' ',
'-':' ',
'*':' '}
pattern = re.compile('|'.join(map(re.escape, rep)))
#replaces = ['=','[',']','{','}','(',')',',','<','>','\'','\"',':','.']
with open("wikipedia_output.txt","r",encoding='utf-8') as wikipedia_file:
    content = wikipedia_file.readlines()
# you may also want to remove whitespace characters like `\n` at the end of each line
content = [x.strip('\n') for x in content] 
if not os.path.exists(hashtag):
	os.makedirs(hashtag)
for result in content:
	candidate_file = open(hashtag+"/candidate"+str(i)+".txt","w",encoding='utf-8')
	pagetext = site.pages[result].text()
	pagetext = pattern.sub(lambda match: rep[match.group(0)], pagetext)
	#wikipedia_file.write(result.get('title') + '\n')
	candidate_file.write(pagetext)
	candidate_file.close()
	i = i + 1
wikipedia_file.close()