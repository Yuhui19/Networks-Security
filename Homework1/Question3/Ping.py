
# read in file
import sys

f = open('Question3.txt',"r")

# read line
line = f.readline()

# min time
minTime = sys.maxsize
# total time
totalTime = 0
# number of lines
lineNumber = 0

# using loop to get all lines
while line:
    indexOfTime = line.find('time=')
    if indexOfTime != -1:
        indexOfMs = line.find('ms')
        temp = float(line[indexOfTime+5:indexOfMs])
        totalTime += temp
        lineNumber += 1
        if temp < minTime:
            minTime = temp
        # print(temp)
    line = f.readline()

# output text file
output=open("Output.txt","w")
# print out number of lines
output.write("Total lines of received packets: "+str(lineNumber)+"\n")
# print out min time
output.write("Minimum time: "+str(minTime)+"\n")
# print out average time
output.write("Average time: "+str(round(totalTime/lineNumber,3))+"\n")

# close the file
f.close()
output.close()
