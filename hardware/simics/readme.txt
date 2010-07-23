Device Modeling Language DML 1.4
https://intel.github.io/tsffs/simics/dml-1.4-reference-manual/index.html

Simics model builder users guide
https://intel.github.io/tsffs/simics/model-builder-user-guide/index.html
II Device Modeling -> Overview

$ cd [project]
project$ [simics]/bin/project-setup
project$ ./bin/project-setup --device=simple_device
vi [project]/modules/simple_device/test/s-simple_device.py
vi [project]/modules/simple_device/simple_device.dml

project$ make test
=== Building module "simple_device" ===
DML-DEP simple_device.dmldep
DMLC    simple_device-dml.c
DEP     simple_device-dml.d
CC      simple_device-dml.o
CCLD    simple_device.so
.
Ran 2 tests in 1 suites in 0.638387 seconds.
All tests completed successfully.

create [project]/targets/vacuum/my-vacuum.simics
project$ ./simics targets/vacuum/my-vacuum.simics
simics> phys_mem.read 0x1000 -l

