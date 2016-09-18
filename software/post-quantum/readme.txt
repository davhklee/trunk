Reference to open source implementations:
- OpenSSL 3.x
- WolfSSL
- NIST
- Original authors

Test vectors from NIST
https://github.com/usnistgov/ACVP-Server/tree/master/gen-val/json-files

OpenSSL 1.1.1 architecture
- application
- libssl
- libcrypto
- engine

OpenSSL 3.0 architecture
- application
- service
- protocol
- legacy
- providers

Type of providers (5):
Default - load as default no other provider specified by builtin to libcrypto library access all supported algo
Legacy - old algo no longer used md4 blowfish des, no longer secured backward compatibility
FIPS - when FIPS compliant is required 3.0.8 certified by NIST, not install by default
Base - implementation for all non-cryptographic algo eg serial/deserialize asn.1 der m encoding
Null - no implementation to make sure nothing loaded from default

Example:
openssl version -a
ls /usr/lib/x86_64-linux-gnu/ossl-modules
openssl list -providers

Install from package:
sudo apt install libssl-dev
dpkg -L libssl-dev

Install from tarball:
wget https://www.openssl/org/source/openssl-3.0.9.tar.gz
cd openssl-3.0.9/
./config --prefix=/opt/openssl -openssldir=/opt/openssl enable-fips
make -j
sudo make install --OR--
sudo make install_runtime --OR--
sudo make install_sw && sudo make install_fips && sudo make_install_ssldirs
sudo make uninstall //to remove
export PATH=/opt/openssl/bin:$PATH
export LD_LIBRARY_PATH=/opt/openssl/lib64

Enable new providers:
vi opt/openssl/openssl.cnf
- modify openssl_init->provider_sect->legacy = legacy_sect //add [legacy_sect] activate = 1

