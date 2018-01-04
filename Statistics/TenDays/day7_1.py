import math

def cumulative_normal_dist(mu, sig, x):
    return 1/2*(1 + math.erf((x-mu)/(sig*math.sqrt(2))))

mu = 205
sig = 15
limit = 9800

n = 49

sample_mu = n * mu
sample_sig = math.sqrt(n) * sig

# print(sample_mu, sample_sig)

result = cumulative_normal_dist(sample_mu, sample_sig, limit)

print(round(result, 4))