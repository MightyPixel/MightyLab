# A manufacturer of metal pistons finds that, on average, 12% of the pistons they manufacture are rejected because they are incorrectly sized. What is the probability that a batch of 10 pistons will contain:
# No more than 2 rejects?
# At least 2 rejects?


import sys
from math import factorial

def binom(x, n):
    return factorial(n)/(factorial(x)*factorial(n-x))

def binomial_distribution(x, n, p):
    q = 1 - p
    b = binom(x, n)
    px = p**(x)
    qd = q**(n-x)

    return b*px*qd
    

args = sys.stdin.readlines()[0].split(' ')
defect_percent, batch_size = [float(arg) for arg in args]

result = 0
for x in range(0, 3):
 result += binomial_distribution(x, batch_size, defect_percent/100)

print(round(result, 3))

result = 0
for x in range(2, 11):
 result += binomial_distribution(x, batch_size, defect_percent/100)

print(round(result, 3))

# 0.891
# 0.342