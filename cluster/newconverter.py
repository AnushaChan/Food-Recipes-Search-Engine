from __future__ import print_function
import json
import glob
import numpy as np
# import pandas as pd
import nltk
import re
import os
import codecs
from sklearn import feature_extraction
from sklearn.feature_extraction.text import TfidfVectorizer
import mpld3
import sys
import urllib
from pprint import pprint
from collections import Counter
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.cluster import KMeans
#python newconverter.py 'http://192.168.1.39:8983/solr/nutch/select?q=content%3Aindian&start=0&rows=50'


documents=[]
url_name=[]
titles=[]

userInput = sys.argv[1]
print(userInput)
query = 'http://192.168.1.39:8983/solr/nutch/select?q=content%3A'+userInput+'&start=0&rows=50'
var = urllib.urlopen(query)
data=var.read()
obj=json.loads(data)
first_elem=obj['response']
output = first_elem['docs']


for p in output:
   q=p['content']
   url=p['url']
   title=p['title']
   documents.append(q[0])
   url_name.append(url[0])
   titles.append(title[0])

tfidf_vectorizer = TfidfVectorizer(stop_words='english', use_idf=True, smooth_idf = True, sublinear_tf = True)
X = tfidf_vectorizer.fit_transform(documents)


dist = 1 - cosine_similarity(X)



num_clusters = 3
km = KMeans(n_clusters=num_clusters)
km.fit(X)
clusters = km.labels_.tolist()

from sklearn.externals import joblib
joblib.dump(km,  'doc_cluster.pkl')
km = joblib.load('doc_cluster.pkl')
clusters = km.labels_.tolist()


def most_frequent(List): 
    occurence_count = Counter(List) 
    return occurence_count.most_common(1)[0][0] 

max_cluster1 = most_frequent(clusters)

    
#print(clusters)
#print(len(clusters))
#print("max cluster: ", max_cluster1)

max_cluster = {}
max_cluster_content = []

for i in range(len(clusters)):
    if clusters[i] == max_cluster1:
        max_cluster['url'] = url_name[i]        
        max_cluster['title']=titles[i]
        max_cluster['content'] = documents[i]
        max_cluster_content.append(max_cluster)


#print(max_cluster_content[0])
print(json.dumps(max_cluster_content))



