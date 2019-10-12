
import numpy as np
from matplotlib import pyplot as plt
import cv2 as cv

def ssd(a, b):
    if a.shape != b.shape:
        return -1
    delta = a - b
    return np.sum(delta * delta)

BLOCK_SIZE = 3
SCAN_SIZE = 60

def stereo_match(y, x, A_block, B_in, block_size=5):
    min_x = max(0, x - SCAN_SIZE)
    min_err = None
    min_idx = None
    for x in range(min_x, x):
        B_block = B_in[y: y+block_size, x: x+block_size]
        error = ssd(A_block, B_block)
        if min_err is None or error < min_err:
            min_err = error
            min_idx = (y, x)
    return min_idx

A_in = cv.imread("c:/temp/stereo1.png",0)
B_in = cv.imread("c:/temp/stereo2.png",0)

h, w = A_in.shape
C_out = np.zeros((h, w))
for y in range(BLOCK_SIZE, h-BLOCK_SIZE):
    for x in range(BLOCK_SIZE, w-BLOCK_SIZE):
        A_block = A_in[y:y + BLOCK_SIZE, x:x + BLOCK_SIZE]
        min_idx = stereo_match(y, x, A_block, B_in, block_size=BLOCK_SIZE)
        C_out[y, x] = abs(min_idx[1] - x)

plt.imshow(C_out, 'gray')
plt.show()
