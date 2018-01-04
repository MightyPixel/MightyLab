import math

def cumulative_normal_dist(mu, sig, x):
    return 1/2*(1 + math.erf((x-mu)/(sig*math.sqrt(2))))

mu = 2.4
sig = 2.0
limit = 250
n = 100

sample_mu = n * mu
sample_sig = math.sqrt(n) * sig

# print(sample_mu, sample_sig)

result = cumulative_normal_dist(sample_mu, sample_sig, limit)

print(round(result, 4))