
CC = gcc
CFLAGS = -I/path/to/openssl/include -I/path/to/kyber/include
LDFLAGS = -L/path/to/openssl/lib -L/path/to/kyber/lib -lssl -lcrypto -lkyber

all: verify_kyber_signature

verify_kyber_signature: verify_kyber_signature.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

clean:
	rm -f verify_kyber_signature

