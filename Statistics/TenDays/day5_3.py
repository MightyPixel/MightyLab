# The probability that a machine produces a defective product is 1/3. What is the probability that the 1st defect is found during the 5th inspection?

import sys

def geometry_distribution(n, p):
    q = 1 - p
    return q**(n - 1)*p

args = sys.stdin.readlines()
defect_num, defect_denum = map(int, args[0].split(' '))
n = int(args[1])

# print(defect_num/defect_denum, n)
print(round(geometry_distribution(n, defect_num/defect_denum), 3))