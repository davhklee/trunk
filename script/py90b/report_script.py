#!/usr/bin/python

import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import axes3d
import numpy as np
import struct
import sys
import random
import math
import statistics
import bz2
import seaborn as sns
import pandas as pd
import scipy as sp
import numpy.linalg as la
from sklearn.cluster import KMeans

cvalue = {}
cvalue[ 2 ] = 10.8276
cvalue[ 6 ] = 13.8155
cvalue[ 9 ] = 27.8772
cvalue[ 14 ] = 36.1233
cvalue[ 30 ] = 59.7031
cvalue[ 62 ] = 102.1662
cvalue[ 126 ] = 180.7989
cvalue[ 254 ] = 329.3828
cvalue[ 510 ] = 614.4182
cvalue[ 1022 ] = 1167.4288
cvalue[ 2046 ] = 2249.3909

def longestRepeatedSubstring(str):
 
    n = len(str)
    LCSRe = [[0 for x in range(n + 1)] 
                for y in range(n + 1)]
 
    res = "" # To store result
    res_length = 0 # To store length of result
 
    # building table in bottom-up manner
    index = 0
    for i in range(1, n + 1):
        for j in range(i + 1, n + 1):
             
            # (j-i) > LCSRe[i-1][j-1] to remove
            # overlapping
            if (str[i - 1] == str[j - 1] and
                LCSRe[i - 1][j - 1] < (j - i)):
                LCSRe[i][j] = LCSRe[i - 1][j - 1] + 1
 
                # updating maximum length of the
                # substring and updating the finishing
                # index of the suffix
                if (LCSRe[i][j] > res_length):
                    res_length = LCSRe[i][j]
                    index = max(i, index)
                 
            else:
                LCSRe[i][j] = 0
 
    # If we have non-empty result, then insert 
    # all characters from first character to 
    # last character of string
    if (res_length > 0):
        for i in range(index - res_length + 1,
                                    index + 1):
            res = res + str[i - 1]
 
    return res

# Count number of 1 bits in a single byte
def countbit(n):
    count = 0
    #for i in range(0,8):
    while n > 0:
        if n & 1 == 1:
            count += 1
        n = n >> 1
    return count

# Calculating BER (Hamming distance/length) between two binaries
def comparefile(f1, f2):
	# Read in files and take first 128 bytes
	a = []
	f = open(f1, "rb")
	byte = f.read(1)
	while byte:
		a.append(byte)
		byte = f.read(1)

	f.close()
	a = a[:128]
	b = []
	f = open(f2, "rb")
	byte = f.read(1)
	while byte:
		b.append(byte)
		byte = f.read(1)

	f.close()
	b = b[:128]
	# Calculate Hamming distance by XOR'ing corresponding bytes
	sum = 0
	for i in range(0, len(a)):
		flipped = a[i][0] ^ b[i][0]
		sum += countbit(flipped)
	return sum

ber_global = []

def process_rand(infiles):
    fcount = 0
    for fn in infiles:
        fp = open(fn,"r")
        data = fp.read()
        fp.close()
        data = data.strip()
        l = data.split(" ")
        assert(len(l) == 512)
        count = 0
        num = 0
        for x in l:
            x = x.strip()
            if count % 32 == 0:
                num = num + 1
                fp = open("{}_{}.bin".format(fcount,num),"wb")
            fp.write(struct.pack("<I", int(x, 16)))
            if count % 32 == 31:
                fp.close()
                if num > 8:
                    bindiff = comparefile("{}_{}.bin".format(fcount,num), "{}_{}.bin".format(fcount,num-8))
                    ber_global.append(bindiff)
                    ber = bindiff/1024*100
                    print("Array" + str(num-8), ber, "%")
            count = count + 1
        fcount += 1

def scc(n, k1, k11):
    threshold = 0.4
    val = 0
    num = (n - 1) * k11 - k1 * k1
    denom = (n - 1) * k1 - k1 * k1
    val = num / denom
    if val > threshold:
        val = threshold
    if val < -threshold:
        val = -threshold
    return val

def plot_scc(n = 1024):
    x_interval = (0, n-1)
    y_interval = (0, n-1)
    x_points = np.linspace(x_interval[0], x_interval[1], 100)
    y_points = np.linspace(y_interval[0], y_interval[1], 100)
    #X, Y, Z = axes3d.get_test_data(0.05)
    X, Y = np.meshgrid(x_points, y_points)
    Z = np.zeros(X.shape)
    for a in range(X.shape[0]):
        for b in range(X.shape[1]):
            Z[a, b] = scc(n, X[a, b], Y[a, b])
    # Plot the 3D surface
    ax = plt.figure().add_subplot(projection='3d')
    ax.plot_surface(X, Y, Z, edgecolor='royalblue', lw=0.5, rstride=8, cstride=8,
                alpha=0.3)
    # Plot projections of the contours for each dimension.  By choosing offsets
    # that match the appropriate axes limits, the projected contours will sit on
    # the 'walls' of the graph.
    ax.contour(X, Y, Z, zdir='z', offset=-2, cmap='coolwarm')
    ax.contour(X, Y, Z, zdir='x', offset=-40, cmap='coolwarm')
    ax.contour(X, Y, Z, zdir='y', offset=40, cmap='coolwarm')
    ax.set(xlim=(0, 1024), ylim=(0, 1024), zlim=(-2, 2), xlabel='X', ylabel='Y', zlabel='Z')
    plt.show()

def bit_array(fn):
    bit = []
    fp = open(fn, "rb")
    by = fp.read(1)
    while by:
        temp = by[0]
        for i in range(8):
            if temp & 1 == 1:
                bit.append(1)
            else:
                bit.append(0)
            temp = temp >> 1
        by = fp.read(1)
    fp.close()
    return bit

def count1(fn):
    count = 0
    bit = bit_array(fn)
    for x in bit:
        if x == 1:
            count += 1
    return count

def count11(fn):
    count = 0
    last = None
    bit = bit_array(fn)
    for x in bit:
        if last != None:
            if x == 1 and last == 1:
                count += 1
        last = x
    return count

def conv1(a):
    ret = [0 for x in range(math.floor(len(a) / 8))]
    if len(a) % 8 > 0:
        ret += [0]
    for i in range(len(a)):
        ret[math.floor(i / 8)] += a[i]
    return ret

def conv2(a):
    ret = [0 for x in range(math.floor(len(a) / 8))]
    if len(a) % 8 > 0:
        ret += [0]
    for i in range(len(a)):
        ret[math.floor(i / 8)] |= (a[i] << (7 - (i % 8)))
    return ret

def alt_seq1(a):
    ret = [0 for x in range(len(a) - 1)]
    for i in range(len(a) - 1):
        if a[i] > a[i+1]:
            ret[i] = -1
        else:
            ret[i] = 1
    return ret

def alt_seq2(a, md = None):
    if md is None:
        md = statistics.median(a)
    ret = [0 for x in range(len(a))]
    for i in range(len(a)):
        if a[i] < md:
            ret[i] = -1
        else:
            ret[i] = 1
    return ret

def excur(a):
    m = statistics.mean(a)
    s = 0
    maxi = 0
    for i in range(len(a)):
        s += a[i]
        d_i = abs(s - ((i+1) * m))
        if d_i > maxi:
            maxi = d_i
    return maxi

def num_direct_run(a):
    n = 0
    if len(a) > 0:
        n += 1
    for i in range(len(a) - 1):
        if a[i+1] != a[i]:
            n += 1
    return n

def len_direct_run(a):
    maxn = 0
    n = 1
    for i in range(len(a) - 1):
        if a[i+1] == a[i]:
            n += 1
        else:
            if n > maxn:
                maxn = n
            n = 1
    if n > maxn:
        maxn = n
    return maxn

def num_incr_decr(a):
    pos = 0
    for x in a:
        if x > 0:
            pos += 1
    rpos = len(a) - pos
    return max(pos, rpos)

def find_col(a):
    ret = []
    n = len(a)
    k = 256
    i = 0
    j = 0
    while i + j < n:
        dups = [False for l in range(k)]
        while i + j < n:
            if dups[a[i + j]]:
                ret.append(j + 1)
                i += j
                j = 0
                break
            else:
                dups[a[i+j]] = True
                j += 1
        i += 1
    return ret

def avg_col(a):
    return statistics.mean(find_col(a))

def max_col(a):
    return max(find_col(a))

def period_test(a, p = [1, 2, 8, 16, 32]):
    ret = []
    for q in p:
        T = 0
        n = len(a)
        for i in range(n - q):
            if a[i] == a[i + q]:
                T += 1
        ret.append(T)
    return ret

def covar_test(a, p = [1, 2, 8, 16, 32]):
    ret = []
    for q in p:
        T = 0
        n = len(a)
        for i in range(n - q):
            T += a[i] * a[i + q]
        ret.append(T)
    return ret

def compress_test(a):
    ss = ""
    for x in a:
        ss += str(x) + " "
    ss = ss[:-1]
    ss = ss.encode("utf-8")
    ss = bz2.compress(ss)
    return len(ss)

def chisq_independ(a, isbin = True):
    assert(isbin)

    p0 = 0
    p1 = 0
    for x in a:
        p1 += x
    p1 /= len(a)
    p0 = 1.0 - p1

    min_p = min(p0, p1)
    m = 11
    thr = 5
    while m > 1:
        if (pow(min_p, m) * (len(a) / m)) >= thr:
            break
        else:
            m -= 1

    assert(m > 1)

    tuple_count = 1 << m
    block_count = int(len(a) / m)

    T = 0

    occ = np.zeros(tuple_count)

    for i in range(block_count):
        symbol = 0
        for j in range(m):
            symbol = (symbol << 1) | a[i * m + j]
        occ[symbol] += 1
    occ = occ.tolist()

    for i in range(len(occ)):
        w = countbit(i)
        e = pow(p1, w) * pow(p0, m - w) * block_count
        T += pow(occ[i] - e, 2) / e

    df = pow(2, m) - 2
    #print("df:", df, "err:", 0.001, "cvalue:", cvalue[df])

    return([T, cvalue[df]])

def chisq_goodness(a, isbin = True):
    assert(isbin)

    sublength = int(len(a) / 10)

    ones = 0
    for i in a:
        ones += i

    p = ones / len(a)
    T = 0

    e0 = (1 - p) * sublength
    e1 = p * sublength

    for i in range(10):
        o0 = 0
        o1 = 0
        for j in range(sublength):
            o1 += a[i * sublength + j]
        o0 = sublength - o1
        T += (pow(o0 - e0, 2) / e0) + (pow(o1 - e1, 2) / e1)

    df = 9
    #print("df:", df, "err:", 0.001, "cvalue:", cvalue[df])

    return([T, cvalue[df]])

def chisq_LRS(a, isbin = True):
    assert(isbin)

    k = 2
    p0 = 0
    p1 = 0
    for x in a:
        p1 += x
    p1 /= len(a)
    p0 = 1.0 - p1

    p_col = pow(p0, 2) + pow(p1, 2)
    assert(p_col > 1 / k)
    assert(p_col < 1)

    W = len(longestRepeatedSubstring("".join([str(x) for x in a])))

    N = math.comb(len(a) - W + 1, 2)

    p_colPower = pow(p_col, W)
    pr = (1 - pow(1 - p_colPower, N))

    #print("W:", W, "err:", 0.001)

    return([pr, 0.001])

def estimator_common_value(a):
    s = set(a)

    pmax = 0
    for x in s:
        p = 0
        for y in a:
            if x == y:
                p += 1
        if p > pmax:
            pmax = p

    pmax /= len(a)

    pu = min(1, pmax + 2.576 * math.sqrt((pmax * (1 - pmax)) / (len(a) - 1)))

    mine = -math.log(pu) / math.log(2)

    return mine

def estimator_collision(a, isbin = True):
    assert(isbin)

    i = 0
    t_v = 0
    v = 0
    s = 0.0
    while i < len(a) - 1:
        if a[i] == a[i + 1]:
            t_v = 2
        elif i < len(a) - 2:
            t_v = 3
        else:
            break
        v += 1
        s += t_v * t_v
        i += t_v

    x = i / v
    s = math.sqrt((s - i * x) / (v - 1))
    ZALPHA = 2.5758293035489008
    x -= ZALPHA * s/math.sqrt(v)

    if x < 2.0:
        x = 2.0

    if x < 2.5:
        p = 0.5 + math.sqrt(1.25 - 0.5 * x)
        mine = -math.log(p) / math.log(2)
    else:
        p = 0.5
        mine = 1.0

    return mine

def estimator_markov(a, isbin = True):
    assert(isbin)

    p0 = 0
    p1 = 0
    for x in a:
        p1 += x
    p1 /= len(a)
    p0 = 1 - p1

    c00 = c01 = c10 = c11 = 0
    for i in range(len(a) - 1):
        if a[i] == 0 and a[i+1] == 0:
            c00 += 1
        elif a[i] == 0 and a[i+1] == 1:
            c01 += 1
        elif a[i] == 1 and a[i+1] == 0:
            c10 += 1
        else:
            c11 += 1

    p00 = c00 / (c00 + c01)
    p01 = c01 / (c00 + c01)
    p10 = c10 / (c10 + c11)
    p11 = c11 / (c10 + c11)

    pmax = max(p0 * pow(p00, 127),
               p0 * pow(p01, 64) * pow(p10, 63),
               p0 * p01 * pow(p11, 126),
               p1 * p10 * pow(p00, 126),
               p1 * pow(p10, 64) * pow(p01, 63),
               p1 * pow(p11, 127))

    mine = min(-math.log(pmax) / math.log(2) / 128, 1)

    return mine

iid_global = [[] for x in range(17)]

def iid_test(a):
    print("===IID Test===")
    iid_global[0].append(excur(a))
    print("excursion test:", iid_global[0][-1])

    iid_global[1].append(num_direct_run(alt_seq1(conv1(a))))
    print("num directional run test:", iid_global[1][-1])

    iid_global[2].append(len_direct_run(alt_seq1(conv1(a))))
    print("len directional run test:", iid_global[2][-1])

    iid_global[3].append(num_incr_decr(alt_seq1(conv1(a))))
    print("num increase and decrease test:", iid_global[3][-1])

    iid_global[4].append(num_direct_run(alt_seq2(a, 0.5)))
    print("num directional run test2:", iid_global[4][-1])
    
    iid_global[5].append(len_direct_run(alt_seq2(a, 0.5)))
    print("len directional run test2:", iid_global[5][-1])

    iid_global[6].append(avg_col(conv2(a)))
    print("average collision test:", iid_global[6][-1])

    iid_global[7].append(max_col(conv2(a)))
    print("max collision test:", iid_global[7][-1])

    iid_global[8].append(period_test(conv1(a)))
    print("periodic test (1, 2, 8, 16, 32):", iid_global[8][-1])

    iid_global[9].append(covar_test(conv1(a)))
    print("covar test (1, 2, 8, 16, 32):", iid_global[9][-1])

    iid_global[10].append(compress_test(a))
    print("compress test:", iid_global[10][-1])

    iid_global[11].append(chisq_independ(a))
    print("chi square independ test:", iid_global[11][-1])

    iid_global[12].append(chisq_goodness(a))
    print("chi square goodness test:", iid_global[12][-1])

    iid_global[13].append(chisq_LRS(a))
    print("chi square LRS test:", iid_global[13][-1])

    iid_global[14].append(estimator_common_value(a))
    print("estimator common value:", iid_global[14][-1])

    iid_global[15].append(estimator_collision(a))
    print("estimator collision:", iid_global[15][-1])

    iid_global[16].append(estimator_markov(a))
    print("estimator markov:", iid_global[16][-1])

def unit_test():
    print("===Unit Test===")
    val = conv1([1,0,0,0,1,1,1,0,1,1,0,1,1,0,1,1,0,0,1,1])
    print("conv1:", val)
    assert(val == [4, 6, 2])
    val = conv2([1,0,0,0,1,1,1,0,1,1,0,1,1,0,1,1,0,0,1,1])
    print("conv2:", val)
    assert(val == [142, 219, 48])
    val = alt_seq1([2, 2, 2, 5, 7, 7, 9, 3, 1, 4, 4])
    print("alt_seq1:", val)
    assert(val == [1, 1, 1, 1, 1, 1, -1, -1, 1, 1])
    val = alt_seq2([5, 15, 12, 1, 13, 9, 4])
    print("alt_seq2:", val)
    assert(val == [-1, 1, 1, -1, 1, 1, -1])
    val = excur([2, 15, 4, 10, 9])
    print("excursion test:", val)
    assert(val == 6)
    val = num_direct_run(alt_seq1([2, 2, 2, 5, 7, 7, 9, 3, 1, 4, 4]))
    print("num directional run test:", val)
    assert(val == 3)
    val = len_direct_run(alt_seq1([2, 2, 2, 5, 7, 7, 9, 3, 1, 4, 4]))
    print("len directional run test:", val)
    assert(val == 6)
    val = num_incr_decr(alt_seq1([2, 2, 2, 5, 7, 7, 9, 3, 1, 4, 4]))
    print("num increase and decrease test:", val)
    assert(val == 8)
    val = num_direct_run(alt_seq2([5, 15, 12, 1, 13, 9, 4]))
    print("num directional run test2:", val)
    assert(val == 5)
    val = len_direct_run(alt_seq2([5, 15, 12, 1, 13, 9, 4]))
    print("len directional run test2:", val)
    assert(val == 2)
    val = avg_col([2, 1, 1, 2, 0, 1, 0, 1, 1, 2])
    print("average collision test:", val)
    assert(val == 3)
    val = max_col([2, 1, 1, 2, 0, 1, 0, 1, 1, 2])
    print("max collision test:", val)
    assert(val == 4)
    val = period_test([2, 1, 2, 1, 0, 1, 0, 1, 1, 2], [2])
    print("periodic test (2):", val)
    assert(val == [5])
    val = covar_test([5, 2, 6, 10, 12, 3, 1], [2])
    print("covar test (2):", val)
    assert(val == [164])
    val = compress_test([144, 21, 139, 0, 0, 15])
    print("compress test:", val)
    assert(val == 49)
    val = estimator_common_value([0, 1, 1, 2, 0, 1, 2, 2, 0, 1, 0, 1, 1, 0, 2, 2, 1, 0, 2, 1])
    print("estimator common value:", val)
    val = estimator_collision([1, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0])
    print("estimator collision:", val)
    val = estimator_markov([1, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0])
    print("estimator markov:", val)

def perm_test(func, a, xform = 0):
    n = 10000
    c0 = 1
    c1 = 2
    if xform == 1:
        t = func(alt_seq1(conv1(a)))
    elif xform == 2:
        t = func(alt_seq2(a, 0.5))
    elif xform == 3:
        t = func(conv2(a))
    elif xform == 4:
        t = func(conv1(a))
    else:
        t = func(a)
    for i in range(n):
        b = a.copy()
        for j in range(len(b), 0, -1):
            r = random.randint(1, j)
            tmp = b[j - 1]
            b[j - 1] = b[r - 1]
            b[r - 1] = tmp
        if xform == 1:
            tp = func(alt_seq1(conv1(b)))
        elif xform == 2:
            tp = func(alt_seq2(b, 0.5))
        elif xform == 3:
            tp = func(conv2(b))
        elif xform == 4:
            tp = func(conv1(b))
        else:
            tp = func(b)
        if t > tp:
            c0 += 1
        elif t == tp:
            c1 += 1
    result = [c0 + c1, c0]
    print(result)
    assert((c0 + c1) > 5)
    assert(c0 < 9995)
    return result

#plot_scc()

infiles = ['rand12.txt', 'rand34.txt', 'rand56.txt', 'rand78.txt', 'rand90.txt']
#infiles = ['rand78_room_cold.txt', 'rand78_room_hot.txt', 'rand78_cold_hot.txt']

process_rand(infiles)

scc_global = []

fcount = 0
for y in infiles:
    for x in range(16):
        fn = "{}_{}.bin".format(fcount,x+1)
        k1 = count1(fn)
        k11 = count11(fn)
        s = scc(1024, k1, k11)
        print("bias:", k1, "count11:", k11, "scc:", s)
        scc_global.append([k1, k11, s])
        assert(abs(s) < 0.4)

        a = bit_array(fn)
        iid_test(a)

        continue
        #fisher-yates shuffle
        perm_test(excur, a)
        perm_test(num_direct_run, a, 1)
        perm_test(len_direct_run, a, 1)
        perm_test(num_incr_decr, a, 1)
        perm_test(num_direct_run, a, 2)
        perm_test(len_direct_run, a, 2)
        perm_test(avg_col, a, 3)
        perm_test(max_col, a, 3)
        perm_test(period_test, a, 4)
        perm_test(covar_test, a, 4)
        perm_test(compress_test, a)
        perm_test(chisq_independ, a)
        perm_test(chisq_goodness, a)
        perm_test(chisq_LRS, a)
        exit(0)

    fcount += 1

unit_test()

plot_titles = ['Excursion',
        'Num Dir Run',
        'Length Dir Run',
        'Num Incr/Decr',
        'Num Dir Run (Median)',
        'Length Dir Run (Median)',
        'Avg Collision',
        'Max Collision',
        'Periodic',
        'Covariance',
        'Compression',
        'Chi Square Independ',
        'Chi Square Goodness',
        'Chi Square LRS',
        'Common Val Est',
        'Collision Est',
        'Markov Est']

def plot_kde(gg, hh, ww, aa, tt):
    plotcount = 0
    for x in range(17):
        if x+1 in aa:
            ax = plt.subplot(hh,ww,plotcount+1)
            ax.title.set_text(plot_titles[x])
            if tt == 1:
                sns.kdeplot(gg[x], color='Orange')
                ax.twinx().hist(gg[x], color='Blue')
            elif tt == 2:
                gg[x] = np.array(gg[x]).transpose().tolist()
                sns.kdeplot(gg[x], multiple='stack')
            elif tt == 3:
                gg[x] = np.array(gg[x]).transpose().tolist()
                plt.plot(gg[x][0], color='b')
                plt.plot(gg[x][1], color='g')
            plotcount += 1
    plt.legend('', frameon=False)
    plt.show()

#plot_kde(iid_global, 3, 3, [1,2,3,4,5,6,7,8,11], 1)
#plot_kde(iid_global, 2, 1, [9, 10], 2)
#plot_kde(iid_global, 1, 3, [12, 13, 14], 3)

def plot_ber(gg):
    ll = np.reshape(np.array(gg), (-1, 8)).transpose().tolist()
    df = pd.DataFrame(ll)
    df.columns = ['Room vs Cold', 'Room vs Hot', 'Cold vs Hot']
    sns.kdeplot(df, multiple='fill')
    plt.show()

#plot_ber(ber_global)

def plot_group(gg):
    species = ("Room vs Cold", "Room vs Hot", "Cold vs Hot")
    penguin_means = {
        'Min': [18.35, 18.43, 14.98],
        'Max': [38.79, 48.83, 47.50],
        'Avg': [189.95, 195.82, 217.19],
    }

    ll = np.reshape(np.array(gg), (-1, 8)).tolist()
    #print("ll",ll)
    for x in range(3):
        penguin_means["Min"][x] = round(np.min(ll[x]) / 1024 * 100, 2)
        penguin_means["Max"][x] = round(np.max(ll[x]) / 1024 * 100, 2)
        penguin_means["Avg"][x] = round(np.mean(ll[x]) / 1024 * 100, 2)

    x = np.arange(len(species))  # the label locations
    width = 0.25  # the width of the bars
    multiplier = 0

    fig, ax = plt.subplots(layout='constrained')

    for attribute, measurement in penguin_means.items():
        offset = width * multiplier
        rects = ax.bar(x + offset, measurement, width, label=attribute)
        ax.bar_label(rects, padding=3)
        multiplier += 1

    # Add some text for labels, title and custom x-axis tick labels, etc.
    ax.set_ylabel('no. BER')
    ax.set_title('Bit Error Rate at Temperatures (in %)')
    ax.set_xticks(x + width, species)
    ax.legend(loc='upper left', ncols=3)
    ax.set_ylim(0, 9)
    plt.show()

#plot_group(ber_global)

def plot_contour(gg):
    ll = np.array(gg).transpose().tolist()
    # set seaborn style
    sns.set_style("white")
    # Custom the color, add shade and bandwidth
    for x in range(4):
        ax = plt.subplot(2, 2, x + 1)
        if x == 0:
            sns.kdeplot(x=ll[0], y=ll[1], cmap="Reds", shade=True, bw_adjust=.5, thresh=0)
        elif x == 1:
            sns.kdeplot(x=ll[0], y=ll[1], cmap="Greens", shade=True, bw_adjust=.5, thresh=0)
        elif x == 2:
            sns.kdeplot(x=ll[0], y=ll[1], cmap="Blues", shade=True, bw_adjust=.5, thresh=0)
        else:
            sns.kdeplot(x=ll[0], y=ll[1], bw_adjust=.5, thresh=0)
        plt.xlabel("k1")
        plt.ylabel("k11")
        plt.title("Serial Correlation (SCC)")
    plt.show()

#plot_contour(scc_global)

def plot_estimate(gg, aa):
    for x in range(17):
        if x+1 in aa:
            plt.fill_between([k for k in range(len(gg[x]))], gg[x], alpha=0.4, label=plot_titles[x])
    plt.xlabel("Sample")
    plt.ylabel("Entropy Rate")
    plt.title("min-entropy Estimation")
    plt.legend(loc="upper right")
    plt.show()

plot_estimate(iid_global, [15, 16, 17])

def dimred(images):
    dmin = -1
    dmax = -1
    epsilon = 2000
    distancetype = 2
    n = images.shape[1]
    A = np.zeros([n, n])
    for i in range(n):
        for j in range(n):
            dist = np.linalg.norm(images[:, i] - images[:, j], distancetype)
            if dist < epsilon and i != j:
                A[i][j] = dist
                A[j][i] = dist
            if i != j:
                if dmin == -1 or dist < dmin:
                    dmin = dist
                if dmax == -1 or dist > dmax:
                    dmax = dist
    print("dmin:", dmin)
    print("dmax:", dmax)

    #find shortest geodesci distances
    D = sp.sparse.csgraph.shortest_path(A, directed=False, method="FW")
    print("is D symetric? " + str((D == D.T).all()))

    D2 = D * D
    H = np.identity(n) - (1/n) * np.ones([n, n])
    C = -(1 / 2) * (H @ D2 @ H)
    v, w = la.eig(C)

    k = 2
    ZT = w[:,:k] @ np.diag(v[:k])
    xx = ZT[:,0].T
    yy = ZT[:,1].T
    colorline = [int(np.floor(x/8) * int(110/(len(xx)/8))) for x in range(len(xx))]
    print(colorline)
    plt.scatter(xx, yy, cmap="tab20b", c=colorline)
    plt.title("IsoMap Dimensionality Reduction")
    plt.xlabel("Embed_X (epsilon = " + str(epsilon) + ")")
    plt.ylabel("Embed_Y")
    plt.colorbar()
    plt.show()

    #elbow/silhouete method
    hor = []
    ver = []
    df = np.column_stack((xx, yy))
    df = pd.DataFrame(df)
    for x in range(10):
        km = KMeans(x + 1)
        km.fit(df)
        sse = 0
        for y in range(df.shape[0]):
            sse += np.sum((df.iloc[y,:] - km.cluster_centers_[int(km.labels_[y])])**2)
        hor.append(x + 1)
        ver.append(sse)
    plt.plot(hor, ver)
    plt.title("Elbow/Silhouete Method")
    plt.xlabel("Number of Clusters")
    plt.ylabel("SSE")
    plt.grid()
    plt.show()

    #clustering
    bestcluster = 6
    km = KMeans(bestcluster)
    km.fit(df)
    print(km.cluster_centers_)
    print(km.labels_)
    scatter = plt.scatter(xx, yy, c=km.labels_)
    plt.title("Classes")
    plt.xlabel("Embed_X")
    plt.ylabel("Embed_Y")
    handles, labels = scatter.legend_elements()
    plt.legend(handles = handles, loc="upper right", labels=["c1", "c2", "c3", "c4", "c5", "c6"], title="Clusters")
    plt.show()

    return dmin, dmax, A

images = None
fcount = 0
for x in range(5):
    for y in range(8):
        fn = "{}_{}.bin".format(x, y+1)
        a = np.array(conv2(bit_array(fn)))
        try:
            if images == None:
                images = a.reshape(a.shape[0], 1)
            else:
                images = np.append(images, a.transpose(), 1)
        except:
            images = np.append(images, a.reshape(a.shape[0], 1), 1)
    fcount += 1

print(images)
print(images.shape)

dimred(images)


