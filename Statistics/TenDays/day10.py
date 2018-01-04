from sklearn import linear_model
lm = linear_model.LinearRegression()

m, n = tuple(map(int, input().split(' ')))

X = []
y = []

for i in range(n):
    f_i = [float(f) for f in input().split(' ')]
    y += [f_i[-1]]
    X += [f_i[:-1]]

lm.fit(X, y)

a = lm.intercept_
b = lm.coef_

q = int(input())

for i in range(q):
    q_i = [float(q_i) for q_i in input().split(' ')]
    result = a + b[0]*q_i[0] + b[1]*q_i[1]
    print(round(result, 2))



