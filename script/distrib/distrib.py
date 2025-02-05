import shutil #copy, copyfile
import os #system, popen, walk
import subprocess
import random
import math
import time
from datetime import datetime, timedelta
import glob

def random_date2(start, end, time_format = "%m/%d/%Y %I:%M %p"):
    random.seed(datetime.now().timestamp())
    prop = random.random()
    stime = time.mktime(time.strptime(start, time_format))
    etime = time.mktime(time.strptime(end, time_format))
    ptime = stime + prop * (etime - stime)
    return time.strftime(time_format, time.localtime(ptime))

def random_date(start, end, time_format = "%m/%d/%Y %I:%M %p"):
    random.seed(datetime.now().timestamp())
    sdate = datetime.strptime(start, time_format)
    edate = datetime.strptime(end, time_format)
    rday = math.floor((edate - sdate).days * random.random())
    rhour = math.floor(24 * random.random())
    rmin = math.floor(60 * random.random())
    return str(sdate + timedelta(days = rday, hours=rhour, minutes=rmin))

#current folder
listing = glob.glob("./*trunk")
print(listing)

test_list = ["git"]

fp = open("comments.txt","r")
msg_list = [x.rstrip() for x in fp]
fp.close()

count = 0
for r, subfolder, files in os.walk("backup"):
    if not any([x in r for x in test_list]):
        for f in files:
            count += 1
            src = "/".join([r, f])
            dst = src.replace("backup", "trunk")
            print(dst)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy(src, dst)
            os.system("cd trunk;git add --all")
            dat = random_date("1/1/2007 9:00 AM", "12/31/2025 5:00 PM")
            print(dat)
            os.system("cd trunk;git commit --date='" + dat + "' -m '" + msg_list[math.floor(random.random() * len(msg_list))] + "'")

