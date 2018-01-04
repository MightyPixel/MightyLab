# Quartiles
# Task: find q1, q2, q3

import sys

def get_median_split(xs):
  n = len(xs)
  if n % 2 == 1:
    i = n//2
    median = xs[i]
    return [median, xs[:i], xs[i+1:]]
  else:
    # 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
    # len = 10
    # len/2 = 5
    # xs[5] = 6
    # => xs[len/2 - 1] = xs[4] = 5
    j = (n//2)
    i = j - 1
    median = (xs[i] + xs[j])/2

    return [median, xs[:i+1], xs[j:]]

args = sys.stdin.readlines()
n = int(args[0])
xs = sorted([int(x) for x in args[1].split(' ')])
(q2, left_split, right_split) = get_median_split(xs)
(q1, lf25, ls25) = get_median_split(left_split)
(q3, us25, uf25) = get_median_split(right_split)

print(round(q1))
print(round(q2))
print(round(q3))

# get median
# split left right
# find median left
# find median right
