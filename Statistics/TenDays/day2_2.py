# Day 2: Interquartile Range
# Task: find the IQR (IQR = Q3 - Q1)

import sys

def get_median_split(xs):
  n = len(xs)
  if n % 2 == 1:
    i = n//2
    median = xs[i]
    return [median, xs[:i], xs[i+1:]]
  else:
    i = (n//2) - 1
    j = i + 1
    median = (xs[i] + xs[j])/2

    return [median, xs[:i+1], xs[j:]]


args = sys.stdin.readlines()
n = int(args[0])
xs = [int(x) for x in args[1].split(' ')]
fs = [int(x) for x in args[2].split(' ')]

ss = []
for i in range(n):
  ss += [xs[i]] * fs[i]

ss = sorted(ss)

(q2, left_split, right_split) = get_median_split(ss)
(q1, lf25, ls25) = get_median_split(left_split)
(q3, us25, uf25) = get_median_split(right_split)

print(round(float(q3-q1), 1))
