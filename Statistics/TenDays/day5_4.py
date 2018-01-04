# TODO
# The probability that a machine produces a defective product is 1/3. What is the probability that the 1st defect is found during the first 5 inspections?

import sys
from math import factorial as fact

def negative_bin_dist(x, n, p):
    q = 1 - p
    b = fact(n-1)/(fact(x-1)*fact((n-1) - (x-1)))
    return b * p**x * q**(n-x)

def geometry_distribution(n, p):
    q = 1 - p
    return q**(n - 1)*p

args = sys.stdin.readlines()
defect_num, defect_denum = map(int, args[0].split(' '))
n = int(args[1])
p = defect_num/defect_denum
q = 1 - p

print(round(1 - q**5, 3))
