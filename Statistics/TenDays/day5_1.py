# Binomial Distribution I
# Task: The ratio of boys to girls for babies born in Russia is 1.09 : 1.
# If there is  child born per birth, what proportion of Russian families with exactly 6 children will have at least 3 boys?

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
boys_rat, girls_rat = [float(ratio) for ratio in args]

# print(boys_rat, girls_rat)
# p(boy) = 0.521 = p
# p(girl) = 0.479 = q

p = boys_rat / (boys_rat + girls_rat)

result = 0
for x in range(3, 7):
 result += binomial_distribution(x, 6, p)

print(round(result, 3))
