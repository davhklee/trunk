#courtesy of openCV user guide

import numpy as np
import cv2 as cv
from matplotlib import pyplot as plt

left = cv.imread('c:/temp/stereo1.png', 0)
right = cv.imread('c:/temp/stereo2.png', 0)
stereo = cv.StereoBM_create(numDisparities=16, blockSize=15)
disparity = stereo.compute(left, right)
plt.imshow(disparity, 'gray')
plt.show()
