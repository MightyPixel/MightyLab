#10
# 10 9.8 8 7.8 7.7 7 6 5 4 2 
# 200 44 32 24 22 17 15 12 8 4
# 
# Given two -element data sets,  and , calculate the value of the Pearson correlation coefficient.

import sys
import math

def get_standart_deviation(l, mean):
    return math.sqrt(sum([(x - mean)**2 for x in l])/n)

args = sys.stdin.readlines()

n = int(args[0].strip())
xs = [float(x) for x in args[1].strip().split(' ')]
ys = [float(y) for y in args[2].strip().split(' ')]

mean_x = sum(xs)/len(xs)
mean_y = sum(ys)/len(ys)

# print(mean_x, mean_y)

sig_x = get_standart_deviation(xs, mean_x)
sig_y = get_standart_deviation(ys, mean_y)

# print(sig_x, sig_y)

pearson_coef = 0
for i in range(n):
    pearson_coef += (xs[i] - mean_x) * (ys[i] - mean_y)

pearson_coef /= n * sig_x * sig_y

print(round(pearson_coef, 3))
