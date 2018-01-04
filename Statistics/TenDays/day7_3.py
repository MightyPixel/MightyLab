import math

# https://en.wikipedia.org/wiki/Standard_score
z_score = 1.96
mu = 500
sig = 80
n = 100

sample_mu = mu
sample_sig = sig/math.sqrt(n)

A = mu - z_score*sample_sig
B = mu + z_score*sample_sig

print(round(A, 2))
print(round(B, 2))