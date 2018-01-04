# weighted mean

import sys
args = sys.stdin.readlines()
n = int(args[0])
xs = [int(x) for x in args[1].split(' ')]
ws = [int(w) for w in args[2].split(' ')]

mw = sum([x*w for x, w in zip(xs, ws)])/sum(ws)

print(round(mw, 1))
