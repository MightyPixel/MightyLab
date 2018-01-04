import matplotlib.pyplot as plt
import numpy as np

def f(x):
    return -(3/4)*x -7/4

def g(x):
    return -(3/4)*x -2

domain = np.arange(-5.0, 5.0, 0.1)
plt.figure(1)
plt.plot(f(domain), 'b--', domain, g(domain), 'g--')
plt.show()
