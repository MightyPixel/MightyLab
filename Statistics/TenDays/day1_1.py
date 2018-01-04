# Mean, median, mode

import sys
args = sys.stdin.readlines()
n = args[0]
xs = sorted(args[1])

# n=10
# xs = sorted([64630, 11735, 14216, 99233, 14470, 4978, 14470, 38120, 51135, 67060])
# print(n, xs)

mean = sum(xs)/n

if n % 2 == 1:
  median = xs[round(len(xs)/2)]
else:
  i = int((len(xs)-1)/2)
  j = int((len(xs)+1)/2)
  median = sum(xs[i:j+1])/2

occ = {i : xs.count(i) for i in xs}
modes = []

vals = occ.values()

for x, count in occ.items():
    if max(vals) == count:
        modes.append(x)

mode = min(modes)


print(round(mean, 1))
print(round(median, 1))
print(mode)
