import sys

args = sys.stdin.readlines()
data = [arg.strip().split(' ') for arg in args]

X = [float(d[0]) for d in data]
y = [float(d[1]) for d in data]

n = len(y)

b_num = n * sum([xi * yi for xi, yi in zip(X, y)]) - sum(X)*sum(y)
b_denum = n * sum([xi**2 for xi in X]) - sum(X)**2
b = b_num/b_denum

x_mean = sum(X)/n
y_mean = sum(y)/n

a = y_mean - b*x_mean

y_80 = a + b*80
print(round(y_80, 3))
