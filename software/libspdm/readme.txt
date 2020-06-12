/*
 * tested with openssl 3.0.14
 * check openssl root dir contains: crypto/aes/aes_ecb.c
 */
export OPENSSL_ROOT_DIR=/path/to/openssl
export OPENSSL_INCLUDE_DIR=$OPENSSL_ROOT_DIR/include
export OPENSSL_LIBRARY=$OPENSSL_ROOT_DIR/lib64/libcrypto.so

git clone 
git submodule update --init --recursive
mkdir build;cd build
cmake -DARCH=x64 -DTOOLCHAIN=GCC -DTARGET=Debug -DCRYPTO=openssl ..
make copy_sample_key
make

