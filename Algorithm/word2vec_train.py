# -*- coding: utf-8 -*-
"""
Created on Wed Dec 20 21:01:30 2017

@author: xzf0724
"""


from gensim.models import word2vec
import logging

vec_dim = 200

def text8Train():
    print("start train word2vec model")
    logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
    sentences = word2vec.Text8Corpus(u"allcode.txt")  
    
    model = word2vec.Word2Vec(min_count=0, window=10, size=vec_dim, workers = 3, iter = 10)
    model.build_vocab(sentences)
    model.train(sentences, total_examples = model.corpus_count, epochs = 10)
    model.save("model.bin")
    y1 = model.similarity("public", "private")
    print(y1)
    
def moreTrain():
    logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.INFO)
    new_model = word2vec.Word2Vec.load('new_model.bin')
    more_sentences = word2vec.Text8Corpus(u"addcode-1.txt")

    new_model.build_vocab(more_sentences, update=True)
    new_model.train(more_sentences, total_examples=new_model.corpus_count, epochs=5)
    print(new_model['ergb'])
    new_model.save('new_model.bin')
    
def printResult():
    model = word2vec.Word2Vec.load('new_model.bin')
#    y1 = model.similarity("access", "switch")
#    y2 = model.similarity("public", "private")
#    print(y1)
#    print(y2)
#    print(model.most_similar(u"device"))
    print(model['_'])

if __name__ == "__main__":
#    text8Train()
#    sentences = getData()
    moreTrain()
#    printResult()
#    sentencesSimilarity()
#    model = main(sentences)   
#    model = word2vec.Word2Vec.load("model20180109-5000")

#    print model.vector_size
#    vectors = getVectors(sentences, model)
#    print vectors