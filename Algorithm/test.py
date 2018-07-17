# -*- coding: utf-8 -*-
"""
Created on Wed Apr  4 11:32:28 2018

@author: xzf0724
"""
import numpy as np
import time
import os
np.random.seed(1337)

from keras.preprocessing.text import Tokenizer
from keras.preprocessing.sequence import pad_sequences
from keras.models import model_from_json  

MAX_SEQUENCE_LENGTH = 15 

TESTPATH = 'data/test-projectName/'
MODELPATH = 'model/'
FILENAME = 'data/test-projectName/test_ClssId.txt'
TARGETPATH = 'data/test-projectName/targetClasses.txt'
values = []
predsTargetClassNames = []
print ("start time:"+time.strftime("%Y/%m/%d  %H:%M:%S"))
start = time.clock()
f = open(TARGETPATH, 'r', encoding = 'utf-8')
for line in f:
    predsTargetClassName = line.split()
    predsTargetClassNames.append(predsTargetClassName)
    
f = open(FILENAME, 'r', encoding = 'utf-8')
for line in f:
    value = line.split()
    values.append(value)
TP = 0 
FN = 0 
FP = 0 
TN = 0 
NUM_CORRECT = 0
TOTAL = 0
model = model_from_json(open(MODELPATH + 'my_model_n#Fold.json').read())  
model.load_weights(MODELPATH + 'my_model_weights_n#Fold.h5')
ii = 0
for sentence in values:
    ii=ii+1
    test_distances = []
    test_labels = []
    test_texts = []
    targetClassNames=[]
    classId = sentence[0]
    label = sentence[1]
    if(os.path.exists(TESTPATH + 'test_Distances'+classId+'.txt')):
        with open(TESTPATH + 'test_Distances'+classId+'.txt','r') as file_to_read:
            for line in file_to_read.readlines():
                values = line.split()
                test_distance = values[:2]
        
                test_distances.append(test_distance)
                test_label =values[2:]
                test_labels.append(test_label)
        
                
        with open(TESTPATH + 'test_Names'+classId+'.txt','r') as file_to_read:
            for line in file_to_read.readlines():
                test_texts.append(line)
                line = line.split()
                targetClassNames.append(line[10:])
        
        tokenizer1 = Tokenizer(num_words=None)
        tokenizer1.fit_on_texts(test_texts)
        test_sequences = tokenizer1.texts_to_sequences(test_texts)
        test_word_index = tokenizer1.word_index
        test_data = pad_sequences(test_sequences, maxlen=MAX_SEQUENCE_LENGTH)  
        test_distances = np.asarray(test_distances)
        test_labels1 = test_labels
        test_labels = np.asarray(test_labels)

        
        x_val = []
        x_val_names = test_data
        x_val_dis = test_distances
        x_val_dis = np.expand_dims(x_val_dis, axis=2)
        x_val.append(x_val_names)
        x_val.append(np.array(x_val_dis))
        y_val = np.array(test_labels) 
        
        preds = model.predict_classes(x_val)
        preds_double = model.predict(x_val)
        NUM_ZERO = 0
        NUM_ONE = 0
        MAX = 0
        for i in range(len(preds)):
            if(preds[i][0]==0):
                NUM_ZERO += 1
            else:
                NUM_ONE += 1
        if(len(preds)!=0 and label == '1'):
            TOTAL+=1
        if(label == '1' and NUM_ONE == 0):
            FN += 1
        if(label == '1' and NUM_ONE != 0):
            TP+=1;
            correctTargets = []
            for i in range(len(preds_double)):
                if(preds_double[i][0]>=MAX):
                    MAX = preds_double[i][0]
            for i in range(len(preds_double)):
                if(preds_double[i][0] == MAX):
                    correctTargets.append(targetClassNames[i])
            for i in range(len(correctTargets)):
                if(correctTargets[i]==predsTargetClassNames[TOTAL-1]):
                    NUM_CORRECT += 1
                    break
        if(label == '0' and NUM_ONE == 0):
            TN += 1
        if(label == '0' and NUM_ONE !=0):
            FP += 1

print('TP--------', TP)
print('TN--------', TN)
print('FP--------', FP)
print('FN--------', FN)
print('NUM_ZERO---', NUM_ZERO)
print('NUM_ONE---', NUM_ONE)
print('NUM_CORRECT----',NUM_CORRECT)
print('TargetAccuracy---',NUM_CORRECT/TP)
if(TP+FP!=0):
    print('Test Precision',TP/(TP+FP))
else:
    print('Test Precision',0)
if(TP+FN!=0):
    print('Test Recall',TP/(TP+FN))
else:
    print('Test Recall',0)
end = time.clock()

print('Running time: %s Seconds'%(end-start))   
        

print ("end time:"+time.strftime("%Y/%m/%d  %H:%M:%S"))