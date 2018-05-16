import mwclient
import re
import os
import sys

NO_OF_TWEETS = 100
NO_OF_WIKIPEDIA_PAGES = 10
hashtag = sys.argv[1]


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