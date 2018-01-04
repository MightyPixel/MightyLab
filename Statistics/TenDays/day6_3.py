import sys
import math

def normal_distribution(mu, sig, x):
    exp = - ((x-mu)**2)/(2*sig**2)

    return (math.e**exp)/(sig*math.sqrt(2*math.pi))

def cumulative_normal_dist(mu, sig, x):
    return 1/2*(1 + math.erf((x-mu)/(sig*math.sqrt(2))))

args = sys.stdin.readlines()
mu, sig = map(int, args[0].split(' '))

for question in args[1:]:
    question_params = [float(param) for param in question.split(' ')]
    if len(question_params) == 1:
        x = question_params[0]
        result = cumulative_normal_dist(mu, sig, x)
    elif len(question_params) == 2:
        x = question_params[0]
        y = question_params[1]
        result = cumulative_normal_dist(mu, sig, y) - cumulative_normal_dist(mu, sig, x)
    else:
        raise ValueError

    print(round(result, 3))