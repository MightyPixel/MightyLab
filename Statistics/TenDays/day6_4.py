import sys
import math

def normal_distribution(mu, sig, x):
    exp = - ((x-mu)**2)/(2*sig**2)

    return (math.e**exp)/(sig*math.sqrt(2*math.pi))

def cumulative_normal_dist(mu, sig, x):
    return 1/2*(1 + math.erf((x-mu)/(sig*math.sqrt(2))))

args = sys.stdin.readlines()
mu, sig = map(int, args[0].split(' '))

more_then_80 = cumulative_normal_dist(mu, sig, 120) - cumulative_normal_dist(mu, sig, 80)
more_then_60 = cumulative_normal_dist(mu, sig, 120) - cumulative_normal_dist(mu, sig, 60)
less_then_60 = cumulative_normal_dist(mu, sig, 60) - cumulative_normal_dist(mu, sig, 0)


print(round(more_then_80*100, 2))
print(round(more_then_60*100, 2))
print(round(less_then_60*100, 2))