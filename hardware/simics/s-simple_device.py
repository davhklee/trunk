import dev_util
import conf
import stest

# Create an instance of the device to test
dev = pre_conf_object('dev', 'simple_device')
SIM_add_configuration([dev], None)
dev = conf.dev

# Create a register wrapper for the register
r = dev_util.Register_LE(dev.bank.regs, 0)

# Test that reading from the register returns 42...
stest.expect_equal(r.read(), 42)
# ...even if we write something else to it.
r.write(0x4711)
stest.expect_equal(r.read(), 42)

