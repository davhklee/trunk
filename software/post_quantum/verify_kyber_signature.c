#include <openssl/evp.h>
#include <openssl/err.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Include Kyber headers
#include "kyber.h"

// Function to verify a Kyber signature
int verify_kyber_signature(const unsigned char *msg, size_t msg_len,
                           const unsigned char *sig, size_t sig_len,
                           const unsigned char *pubkey, size_t pubkey_len) {
    EVP_PKEY *pkey = EVP_PKEY_new();
    if (!pkey) {
        ERR_print_errors_fp(stderr);
        return 0;
    }

    // Convert Kyber public key to OpenSSL EVP_PKEY
    if (!EVP_PKEY_set1 Kyber(pkey, pubkey, pubkey_len)) {
        ERR_print_errors_fp(stderr);
        EVP_PKEY_free(pkey);
        return 0;
    }

    // Create an EVP_MD_CTX for the signature verification
    EVP_MD_CTX *md_ctx = EVP_MD_CTX_new();
    if (!md_ctx) {
        ERR_print_errors_fp(stderr);
        EVP_PKEY_free(pkey);
        return 0;
    }

    // Initialize the context for signature verification
    if (!EVP_DigestVerifyInit(md_ctx, NULL, EVP_sha256(), NULL, pkey)) {
        ERR_print_errors_fp(stderr);
        EVP_MD_CTX_free(md_ctx);
        EVP_PKEY_free(pkey);
        return 0;
    }

    // Update the context with the message
    if (!EVP_DigestVerifyUpdate(md_ctx, msg, msg_len)) {
        ERR_print_errors_fp(stderr);
        EVP_MD_CTX_free(md_ctx);
        EVP_PKEY_free(pkey);
        return 0;
    }

    // Finalize the signature verification
    int result = EVP_DigestVerifyFinal(md_ctx, sig, sig_len);

    // Clean up
    EVP_MD_CTX_free(md_ctx);
    EVP_PKEY_free(pkey);

    return result;
}

int main() {
    // Example data
    unsigned char msg[] = "Hello, World!";
    size_t msg_len = strlen((char *)msg);
    unsigned char sig[] = { /* Your signature here */ };
    size_t sig_len = sizeof(sig);
    unsigned char pubkey[] = { /* Your public key here */ };
    size_t pubkey_len = sizeof(pubkey);

    // Verify the signature
    int result = verify_kyber_signature(msg, msg_len, sig, sig_len, pubkey, pubkey_len);
    if (result) {
        printf("Signature is valid.\n");
    } else {
        printf("Signature is invalid.\n");
    }

    return 0;
}

