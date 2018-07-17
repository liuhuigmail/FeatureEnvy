# -*- coding: utf-8 -*-
"""
Created on Thu Mar 22 19:59:47 2018

@author: xzf0724
"""

import numpy as np
import time
np.random.seed(1337)

from keras.preprocessing.text import Tokenizer
from keras.preprocessing.sequence import pad_sequences
from keras.utils.np_utils import to_categorical
from keras.layers import Dense, Flatten
from keras.layers import Conv1D, Embedding
from keras.models import Sequential
from keras.layers import Merge

print ("start time: "+time.strftime("%Y/%m/%d  %H:%M:%S"))
distances = []  # TrainSet
labels = []  # 0/1
texts = [] # ClassNameAndMethodName
MAX_SEQUENCE_LENGTH = 15 
EMBEDDING_DIM = 200 # Dimension of word vector

print('Indexing word vectors.')
embeddings_index = {}
f = open('D:/data/word2vecNocopy.200d.txt')
for line in f:
    values = line.split()
    word = values[0]
    coefs = np.asarray(values[1:], dtype='float32')
    embeddings_index[word] = coefs
f.close()

print('Found %s word vectors.' % len(embeddings_index))


with open('D:/data/train_Distances.txt','r') as file_to_read:
    for line in file_to_read.readlines():
        values = line.split()
        distance = values[:2]
        distances.append(distance)
        label =values[2:]
        labels.append(label)

        
with open('D:/data/train_Names.txt','r') as file_to_read:
    for line in file_to_read.readlines():
        texts.append(line)

print('Found %s train_distances.' % len(distances)) 

tokenizer = Tokenizer(num_words=None)
tokenizer.fit_on_texts(texts)
sequences = tokenizer.texts_to_sequences(texts)
word_index = tokenizer.word_index
data = pad_sequences(sequences, maxlen=MAX_SEQUENCE_LENGTH)   
distances = np.asarray(distances)
labels = to_categorical(np.asarray(labels))

print('Shape of train_data tensor:', data.shape)
print('Shape of train_label tensor:', labels.shape)

x_train = []
x_train_names = data
x_train_dis = distances 
x_train_dis = np.expand_dims(x_train_dis, axis=2)
x_train.append(x_train_names)
x_train.append(np.array(x_train_dis))
y_train = np.array(labels)

nb_words = len(word_index)
embedding_matrix = np.zeros((nb_words+1, EMBEDDING_DIM))
for word, i in word_index.items():
    embedding_vector = embeddings_index.get(word)
    if embedding_vector is not None:
        embedding_matrix[i] = embedding_vector 

embedding_layer = Embedding(nb_words + 1,
                            EMBEDDING_DIM,
                            input_length=MAX_SEQUENCE_LENGTH,
                            weights=[embedding_matrix],
                            trainable=False)

print('Training model.')

model_left = Sequential()
model_left.add(embedding_layer)
model_left.add(Conv1D(128, 1, padding = "same", activation='tanh'))
model_left.add(Conv1D(128,1, activation='tanh'))
model_left.add(Conv1D(128, 1, activation='tanh'))
model_left.add(Flatten())

model_right = Sequential()
model_right.add(Conv1D(128, 1, input_shape=(2,1), padding = "same", activation='tanh'))
model_right.add(Conv1D(128, 1, activation='tanh'))
model_right.add(Conv1D(128, 1, activation='tanh'))
model_right.add(Flatten())

merged = Merge([model_left, model_right], mode='concat') 
model = Sequential()
model.add(merged) # add merge
model.add(Dense(128, activation='tanh'))
model.add(Dense(2, activation='sigmod'))
model.compile(loss='binary_crossentropy',
              optimizer='Adadelta',
              metrics=['accuracy'])


print ("start training:"+time.strftime("%Y/%m/%d  %H:%M:%S"))
model.fit(x_train, y_train,epochs=6)

score = model.evaluate(x_train, y_train, verbose=0)
print('train loss:', score[0])
print('train accuracy:', score[1])

json_string = model.to_json()  
open('my_model.json','w').write(json_string)  
model.save_weights('my_model_weights.h5') 

print ("end time:"+time.strftime("%Y/%m/%d  %H:%M:%S"))

