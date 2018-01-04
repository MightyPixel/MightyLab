import sys
import math

def poisson_distribution(mu, k):
    return (mu**k * math.e**(-mu))/math.factorial(k)

args = sys.stdin.readlines()
mu = float(args[0])
k = int(args[1])

print(round(poisson_distribution(mu, k), 3))
