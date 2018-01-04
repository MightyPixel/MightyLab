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

X = np.array(xs)
y = np.array(y)

mean_x = np.mean(X,axis=0)
mean_y = np.mean(y,axis=0)

X = X-mean_x
y = y-mean_y

B = np.linalg.inv(X.T.dot(X)).dot(X.T).dot(y)

q = int(input())

for i in range(q):
    q_i = np.array([float(q_i) for q_i in input().split(' ')])
    
    result = sum(B*(q_i - mean_x)) + mean_y
    print(round(result, 2))


