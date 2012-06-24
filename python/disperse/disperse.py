import shutil #copy, copyfile
import os #system, popen, walk
import subprocess
import random
import time

#example: print(random_date("1/1/2007 9:00 AM", "1/1/2023 5:00 PM", random.random()))
def random_date(start, end, prop, time_format = "%m/%d/%Y %I:%M %p"):
    stime = time.mktime(time.strptime(start, time_format))
    etime = time.mktime(time.strptime(end, time_format))
    ptime = stime + prop * (etime - stime)
    return time.strftime(time_format, time.localtime(ptime))

#main
count = 0
for r, subfolder, files in os.walk("merged"):
    if "git" not in r:
        for f in files:
            count += 1
            src = "/".join([r, f])
            dst = src.replace("merged", "trunk")
            #print(src, dst)
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy(src, dst)
            os.system("cd trunk;git add --all")
            dat = random_date("1/1/2007 9:00 AM", "1/1/2023 5:00 PM", random.random())
            os.system("cd trunk;git commit --date='" + dat + "' -m 'new commit " + str(count) + "'")

