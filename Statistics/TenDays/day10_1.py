import numpy as np

def mul_matrix(X, Y):
    result = []
    n = len(X)
    m = len(Y[0])
    
    for i in range(n):
        for j in range(len(Y[0])):
            row = X[i]
            col = [row[j] for row in Y]
            
            
            sum([x*y for x, y in zip(x_row, y_col)])

m, n = tuple(map(int, input().split(' ')))

xs = []
y = []

for i in range(n):
    f_i = [float(f) for f in input().split(' ')]
    y += [f_i[-1]]
    xs += [f_i[:-1]]

X = np.ones((n, m+1)) 
X[:, 1:] = np.array(xs)
y = np.array(y)

B = np.linalg.inv(X.T.dot(X)).dot(X.T).dot(y)
# print(B)
a = B[0]
b1 = B[1]
b2 = B[2]

q = int(input())

for i in range(q):
    q_i = [float(q_i) for q_i in input().split(' ')]
    result = a + b1*q_i[0] + b2*q_i[1]
    print(round(result, 2))




    


