import csv 
import getopt, sys, os
import random   
from datetime import datetime

# current date and time for benchmark
now = datetime.now()
start = datetime.timestamp(now)


print("Enter the number of rows")
number_of_rows = [i for i in range(int(input()))]

filename = 'dataset' + str(len(number_of_rows))

number_of_zones = int(len(number_of_rows) / 5)
print("number og zones: " + str(number_of_zones) )


tasks = [ "Structural Foundation",
         "Sewage",
         "Floor",
         "Structural Walls",
         "Interior Walls",
         "Electricity",
         "Piping",
         "Gypsum"
    ]
crafts = ["Concrete",
          "VVS",
          "Carpenter",
          "Electrician",
          "Painter"
    ]

dir = os.getcwd()+r'\dataset'

def chunks(lst, n):
    """Yield successive n-sized chunks from lst."""
    for i in range(0, len(lst), n):
        yield lst[i:i + n]

taskList = list(chunks(number_of_rows, int(len(number_of_rows ) / 5)) )



zones = { float : list()}

for i in range(len(taskList)):  
    zoneid = i
    if not zoneid in zones:
        zones.update({zoneid : taskList[i]})

headers = ["Id","Zone","Task operation","Duration","Resources","Craft","Construciton order"]

try:
    with open((os.path.join(dir,filename + '.csv')),'w', newline= '') as file:
        writer = csv.writer(file)
        writer.writerow(headers)
        for (key,val) in zones.items():
            for v in val:
                i = float(v)
                if i == 0.0:
                    writer.writerow([i, 
                                    str('z'+str(key)),
                                    random.choice(tasks),
                                    random.randrange(1,10,1),
                                    random.randrange(1,4,1), 
                                    random.choice(crafts),None])
                    i += 0.1
            
                else:             
                    writer.writerow([i,
                                    str('z'+str(key)),
                                    random.choice(tasks),
                                    random.randrange(1,10,1),
                                    random.randrange(1,4,1), 
                                    random.choice(crafts),i - 1])
                    i += 0.1               
except :
    raise Exception('zones are not encountered: {}'.format(zones))

finally :
    benchmark = round((datetime.timestamp(datetime.now()) - start) * 1, 3)
    print('Time to perform the dataset: ' + str(benchmark) + ' seconds')

