import sys

args = sys.stdin.readlines()
mu_a, mu_b = map(float, args[0].split(' '))

cost_a = 160 + 40 * (mu_a + mu_a**2)
cost_b = 128 + 40 * (mu_b + mu_b**2)

print(round(cost_a, 3))
print(round(cost_b, 3))
