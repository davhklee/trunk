from math import log, floor, ceil
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import OrdinalEncoder

# Based on Kaggle project and UC Irvine ML Car Evaluation dataset 1997
# https://www.kaggle.com/code/prashant111/decision-tree-classifier-tutorial
# https://archive.ics.uci.edu/dataset/19/car+evaluation

class Utility(object):
    def entropy(self, labels):
        retval = 0
        a = np.array(labels)
        for x in np.unique(a):
            p = np.where(a == x)[0].shape[0]
            p /= len(labels)
            retval += -(p * log(p)/log(2))
        return retval

    def split(self, X, y, attrib, threshold):
        X_left, X_right, y_left, y_right = [], [], [], []
        for xx, yy in zip(X, y):
            if xx[attrib] <= threshold:
                X_left.append(xx)
                y_left.append(yy)
            else:
                X_right.append(xx)
                y_right.append(yy)
        return(X_left, X_right, y_left, y_right)

    def gain(self, prev, post):
        info_gain = 0
        h = self.entropy(prev)
        hl = self.entropy(post[0])
        hr = self.entropy(post[1])
        info_gain = h - (hl * (len(post[0]) / len(prev)) + hr * (len(post[1]) / len(prev)))
        return info_gain

    def ideal_split(self, X, y):
        attrib = 0
        threshold = 0
        X_left, X_right, y_left, y_right = [], [], [], []
        info_gain = -1
        npa = np.array(X)
        for xx in range(npa.shape[0]):
            for yy in range(npa.shape[1]):
                a, b, c, d = self.split(X, y, yy, npa[xx][yy])
                ig = self.gain(y, [c, d])
                if ig > info_gain:
                    attrib = yy
                    threshold = npa[xx][yy]
                    X_left = a
                    X_right = b
                    y_left = c
                    y_right = d
                    info_gain = ig
        dict = {}
        dict["attrib"] = attrib
        dict["threshold"] = threshold
        dict["X_left"] = X_left
        dict["X_right"] = X_right
        dict["y_left"] = y_left
        dict["y_right"] = y_right
        dict["info_gain"] = info_gain
        return dict

class CARTree(object):
    def __init__(self, max_depth):
        self.tree = {}
        self.max_depth = max_depth

    def learn(self, X, y, depth = 0):
        ut = Utility()
        if depth > self.max_depth or len(y) == 0:
            return np.int32(np.round(np.random.rand()))
        elif len(np.unique(y)) == 1:
            return np.int32(np.unique(y)[0])
        else:
            node = {}
            dict = ut.ideal_split(X, y)
            node["attrib"] = dict["attrib"]
            node["threshold"] = dict["threshold"]
            node["left"] = self.learn(dict["X_left"], dict["y_left"], depth = depth + 1)
            node["right"] = self.learn(dict["X_right"], dict["y_right"], depth = depth + 1)
            self.tree = node
            return node

    def classify(self, record):
        node = self.tree
        while type(np.int32(1)) != type(node):
            if record[node["attrib"]] > node["threshold"]:
                node = node["right"]
            else:
                node = node["left"]
        return node

if __name__ == '__main__':
    print("testcases")
    ut = Utility()
    print(ut.entropy([0,0,0,1,1,1,1,1,1]))
    print(ut.gain([0,0,0,1,1,1], [[0,0], [1,1,1,0]]))
    print(ut.ideal_split([[3,10],[1,22],[2,28],[5,32],[4,32]], [1,1,0,0,1]))
    print("exploratory")
    df = pd.read_csv("car_evaluation.csv", header=None)
    print(df.shape)
    print(df.head())
    col_names = ["buying", "maint", "doors", "persons", "lug_boot", "safety", "class"]
    df.columns = col_names
    print(col_names)
    print(df.head())
    print(df.info())
    for col in col_names:
        print(df[col].value_counts())
    print(df["class"].value_counts())
    print(df.isnull().sum())

    encoder = OrdinalEncoder(dtype=np.int32)
    XX = encoder.fit_transform(df.to_numpy())
    X = np.array(XX[:,0:-1])
    y = np.array(XX[:,-1])

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.33, random_state=42)
    print(X_train.shape, y_train.shape)
    print(X_train)
    print(y_train)
    ct = CARTree(20)
    ct.learn(X_train, y_train)
    result = []
    for xx in X_test:
        result.append(ct.classify(xx))
    result = np.array(result)
    print(y_test.shape, result.shape)
    verdict = np.array([a == b for a,b in zip(y_test, result)])
    print(len(np.where(verdict == False)), "/", len(verdict))
    print(encoder.inverse_transform(XX))

