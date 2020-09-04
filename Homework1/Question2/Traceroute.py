import matplotlib.pyplot as plt
import numpy as np

# read in file
f = open("Question2aLine1.txt", "r")
g = open("Question2aLine2.txt", "r")

line = f.readline()
line2 = g.readline()
totalTime = 0.0
totalTime2 = 0.0
ip = ""
ip2 = ""

x = []
y = []
# hopNunber = []

x2 = []
y2 = []

# read by first line
while line:
    hop = line[:2]

    # when hop number doesn't change
    if hop == "  ":
        tempLine = line[line.find(")") + 1:]
        # tempLine.replace("ms","")
        times = tempLine.split(" ms")
        for i in times:
            # print(i.strip())
            if i != "\n" and i != " *\n":
                totalTime += float(i.strip())
        line = f.readline()
        continue

    if line.find("(") == -1:
        line = f.readline()
        continue

    # when hop changes
    if ip != "":
        x.append(ip)
        # ip = ""
    if totalTime != 0:
        y.append(round(totalTime/3,3))
        totalTime = 0.0

    ip = line[line.find("(")+1:line.find(")")]

    tempLine = line[line.find(")")+1:]
    # tempLine.replace("ms","")
    times = tempLine.split(" ms")
    # print(times)
    for i in times:
        if i != "\n" and i != " *\n" and i != '':
            # print(i.strip())
            totalTime += float(i.strip())

    # a = int(hop)
    # hopNunber.append(a)
    # print(a)
    # str(hop)

    line = f.readline()

x.append(ip)
y.append(round(totalTime/3,3))

# read by second line
while line2:
    hop = line2[:2]

    # when hop number doesn't change
    if hop == "  ":
        tempLine = line2[line2.find(")") + 1:]
        # tempLine.replace("ms","")
        times = tempLine.split(" ms")
        for i in times:
            # print(i.strip())
            if i != "\n" and i != " *\n":
                totalTime2 += float(i.strip())
        line2 = g.readline()
        continue

    if line2.find("(") == -1:
        line2 = g.readline()
        continue

    # when hop changes
    if ip2 != "":
        x2.append(ip2)
        # ip = ""
    if totalTime2 != 0:
        y2.append(round(totalTime2/3,3))
        totalTime2 = 0.0

    ip2 = line2[line2.find("(")+1:line2.find(")")]

    tempLine = line2[line2.find(")")+1:]
    # tempLine.replace("ms","")
    times = tempLine.split(" ms")
    # print(times)
    for i in times:
        if i != "\n" and i != " *\n" and i != '':
            # print(i.strip())
            totalTime2 += float(i.strip())

    a = int(hop)
    # hopNunber.append(a)
    # print(a)
    # str(hop)

    line2 = g.readline()

x.append(ip)
y.append(round(totalTime/3,3))

x2.append(ip2)
y2.append(round(totalTime2/3,3))

# print(x)
# print(y)
# print(hopNunber)

# output a file with ip and time
output = open("Question2aLine1Output.txt", "w")
index = 0
while index < len(x):
    output.write(x[index] + " " + str(y[index]) + "ms\n")
    index += 1

# output a file with line2's ip and time
output = open("Question2aLine2Output.txt", "w")
index = 0
while index < len(x2):
    output.write(x2[index] + " " + str(y2[index]) + "ms\n")
    index += 1

# plot the graph
plt.plot(x, y)
plt.plot(x2, y2)

plt.xticks(rotation=45)
# fig, ax = plt.subplots()
# fig.canvas.draw()
# labels=[item.get_text() for item in ax.get_xticklabels()]
# labelIndex=0
# while labelIndex<16:
#     labels[i] = x[i]
#     labelIndex +=1
plt.show()

# close files
f.close()
output.close()
