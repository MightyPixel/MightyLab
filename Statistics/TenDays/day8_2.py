# find ranks
# find di and di^2
# calculate r_xy

import sys

def get_ranks(xs):
  ordered_xs = sorted(xs)
  rank = range(1, len(xs)+1)
  return {ordered_xs[i-1]: i for i in rank}


args = sys.stdin.readlines()

n = int(args[0])
xs = [float(x) for x in args[1].strip().split(' ')]
ys = [float(y) for y in args[2].strip().split(' ')]

# print(xs, ys)
xs_rank = get_ranks(xs)
ys_rank = get_ranks(ys)

# print(xs_rank, ys_rank)

d2 = []
for i in range(n):
  x_rank = xs_rank[xs[i]]
  y_rank = ys_rank[ys[i]]
  d2.append((x_rank - y_rank)**2)

rs = 1 - (6*sum(d2))/(n*(n**2 - 1))
print(round(rs, 3))
