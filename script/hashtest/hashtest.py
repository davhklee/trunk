
from hashlib import sha256,sha384,sha512
from getpass import getpass
import base64

def hashtest():
    hash = sha256()
    t = int(getpass(prompt="type: "))
    mtype = t % 10
    if mtype == 1:
        pass
    elif mtype == 2:
        hash = sha384()
    else:
        hash = sha512()
    l = int(getpass(prompt="len: "))
    while True:
        str = getpass(prompt="input: ")
        if len(str) == 0:
            hash.update(b"\n")
            if t > 10:
                a = hash.digest()
                b = base64.b64encode(a)
                print(b.decode('ascii')[0:l])
            else:
                print(hash.hexdigest()[0:l])
            return
        else:
            hash.update(str.encode())

hashtest()

