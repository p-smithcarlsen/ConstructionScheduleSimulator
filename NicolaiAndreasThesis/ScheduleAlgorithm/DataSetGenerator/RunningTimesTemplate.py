from matplotlib import pyplot as plt
import numpy as np
import pandas as pd
import os, csv, math
from os import walk
from functools import reduce

inputs = os.getcwd() + r'\ScheduleAlgorithm\Benchmarks\Results'

inputsize=[100,1000,10000]

data = [[],[],[],[]]

# average function
def Average(lst):     
    return reduce(lambda a, b: a + b, lst) / len(lst) 

for (dirpath, dirnames, files) in walk(inputs):
    c = 0
    for i in (files):      
        with open(inputs + r'\\' + i, 'r' ) as csv_file:
            csv_reader = csv.reader(csv_file, delimiter='\n')
            local = []
            for row in csv_reader:    
                local.append(float(row[0]))                              
            data[c].append(Average(local))
            c += 1        
    break

data = data[:3]

plt.figure()
plt.plot(data,inputsize, 'r--')


plt.show()


