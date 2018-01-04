# Day 3: Standard Deviation
# Task: Find the standard deviation of set of numbers
import sys

args = sys.stdin.readlines()
n = int(args[0])
xs = [int(x) for x in args[1].split(' ')]

mean = sum(xs)/n
variance = sum([(x-mean)**2 for x in xs])/n
sd = variance**0.5

print(round(sd, 1))
