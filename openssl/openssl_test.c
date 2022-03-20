/*
 * See online instruction for openssl init/cleanup details
 * https://wiki.openssl.org/index.php/Library_Initialization
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>
#include <string.h>
#include <assert.h>
#include <unistd.h>

#include <openssl/ssl.h>
#include <openssl/opensslv.h>
#include <openssl/bio.h>
#include <openssl/bn.h>
#include <openssl/ec.h>
#include <openssl/ecdh.h>
#include <openssl/ecdsa.h>
#include <openssl/evp.h>
#include <openssl/hmac.h>
#include <openssl/pem.h>
#include <openssl/obj_mac.h>
#include <openssl/sha.h>

#if OPENSSL_VERSION_NUMBER < 0x10100000L
//define struct for backward compatibile
#else
typedef struct ECDSA_SIG_st
{
    BIGNUM *r;
    BIGNUM *s;
} ECDSA_SIG;
#endif

typedef struct bignum_t {
    uint32_t n;
    uint32_t *p;
} bignum_t;

typedef enum result_t {
    TEST_PASS,
    TEST_FAIL
} result_t;

typedef enum pka_curve_t {
    PKA_CURVE_NIST256,
    PKA_CURVE_SECP384
} pka_curve_t;

void pka_init(void)
{
#if OPENSSL_VERSION_NUMBER < 0x10100000L
    SSL_library_init();
#else
    OPENSSL_init_ssl(0, NULL);
#endif
    ERR_load_BIO_strings();
    //ERR_load_crypto_strings();
    OpenSSL_add_ssl_algorithms();
    OpenSSL_add_all_algorithms();
    SSL_load_error_strings();
}

void pka_uninit(void)
{
    EVP_cleanup();
    CRYPTO_cleanup_all_ex_data();
#if OPENSSL_VERSION_NUMBER < 0x10100000L
    ERR_free_strings();
#endif
}

result_t pka_point_multiply(pka_curve_t curve,
                                    bignum_t *x_in,
                                    bignum_t *y_in,
                                    bignum_t *multiply_by,
                                    bignum_t *x_out,
                                    bignum_t *y_out,
                                    bignum_t *p_register_By)
{
    EC_GROUP *p_ec_group = NULL;
    BN_CTX *p_ctx = NULL;
    BIGNUM *p_bn_x = NULL;
    BIGNUM *p_bn_y = NULL;
    BIGNUM *p_bn_scalar = NULL;
    EC_POINT *p_ec_point = NULL;
    EC_POINT *p_ec_result = NULL;
    unsigned char *ptr;
    int32_t ec_status;

    if (curve == PKA_CURVE_NIST256)
    {
        p_ec_group = EC_GROUP_new_by_curve_name(NID_X9_62_prime256v1);
    }
    else if (curve == PKA_CURVE_SECP384)
    {
        p_ec_group = EC_GROUP_new_by_curve_name(NID_secp384r1);
    }
    else
    {
        return (TEST_FAIL);
    }
    assert(p_ec_group != NULL);

    p_ctx = BN_CTX_new();
    assert(p_ctx != NULL);

    p_ec_point = EC_POINT_new(p_ec_group);
    assert(p_ec_point != NULL);
    p_ec_result = EC_POINT_new(p_ec_group);
    assert(p_ec_result != NULL);

    p_bn_x = BN_new();
    assert(p_bn_x != NULL);
    p_bn_y = BN_new();
    assert(p_bn_y != NULL);

    ptr = (uint8_t *)x_in->p;
    p_bn_x = BN_bin2bn(ptr, x_in->n * 4, NULL);

    ptr = (uint8_t *)y_in->p;
    p_bn_y = BN_bin2bn(ptr, y_in->n * 4, NULL);

    ec_status = EC_POINT_set_affine_coordinates_GFp(p_ec_group, p_ec_point, p_bn_x, p_bn_y, NULL);
    if (ec_status != 1)
    {
        printf("fail1!\n");
    }

    ptr = (uint8_t *)multiply_by->p;
    p_bn_scalar = BN_bin2bn(ptr, multiply_by->n * 4, NULL);
    printf("private: %s\n", BN_bn2hex(p_bn_scalar));

    ec_status = EC_POINT_mul(p_ec_group, p_ec_result, NULL, p_ec_point, p_bn_scalar, p_ctx);
    if (ec_status != 1)
    {
        printf("fail2!\n");
    }

    ec_status = EC_POINT_get_affine_coordinates_GFp(p_ec_group, p_ec_result, p_bn_x, p_bn_y, p_ctx);
    if (ec_status != 1)
    {
        printf("fail3!\n");
    }

    ptr = (uint8_t *)x_out->p;
    BN_bn2bin(p_bn_x, ptr);
    printf("pubkey x: %s\n", BN_bn2hex(p_bn_x));

    ptr = (uint8_t *)y_out->p;
    BN_bn2bin(p_bn_y, ptr);
    printf("pubkey y: %s\n", BN_bn2hex(p_bn_y));

    return TEST_PASS;
}

result_t pka_sign(bignum_t *p_rndm_num_k,
                          bignum_t *p_hash,
                          bignum_t *p_secret_key,
                          bignum_t *p_signature_r,
                          bignum_t *p_signature_s,
                          bignum_t *p_register_By,
                          const pka_curve_t curve)
{
    BIGNUM *p_bn_priv;
    BIGNUM *p_bn_pubx;
    BIGNUM *p_bn_puby;
    EC_POINT *ec_point;
    int ret;
    ECDSA_SIG *sig = NULL;

    EC_GROUP *ec_group;
    ec_group = EC_GROUP_new_by_curve_name(NID_secp384r1);

    p_bn_priv = BN_bin2bn((uint8_t *)p_secret_key->p, p_secret_key->n * 4, NULL);
    printf("private: %s\n", BN_bn2hex(p_bn_priv));

    EC_KEY *ec_key;
    ec_key = EC_KEY_new_by_curve_name(NID_secp384r1);

    ret = EC_KEY_set_private_key(ec_key, p_bn_priv);
    EC_KEY_check_key(ec_key);

    sig = ECDSA_do_sign((uint8_t *)p_hash->p, p_hash->n * 4, ec_key);

    BN_bn2bin(sig->r, (uint8_t *)p_signature_r->p);
    BN_bn2bin(sig->s, (uint8_t *)p_signature_s->p);

    BN_free(p_bn_priv);
    printf("signature r: %s\n", BN_bn2hex(sig->r));
    printf("signature s: %s\n", BN_bn2hex(sig->s));

    if (ret == -1 || ret == 0)
    {
        return TEST_FAIL;
    }

    return TEST_PASS;
}

result_t pka_verify(bignum_t *pubx,
                            bignum_t *puby,
                            bignum_t *hash,
                            bignum_t *sigr,
                            bignum_t *sigs,
                            const pka_curve_t curve)
{
    BIGNUM *p_bn_pubx;
    BIGNUM *p_bn_puby;
    EC_POINT *ec_point;
    EC_GROUP *ec_group;
    int ret;

    ECDSA_SIG *sig = NULL;
    sig = ECDSA_SIG_new();

    sig->r = BN_bin2bn((uint8_t *)sigr->p, sigr->n * 4, NULL);
    printf("signature r: %s\n", BN_bn2hex(sig->r));

    sig->s = BN_bin2bn((uint8_t *)sigs->p, sigs->n * 4, NULL);
    printf("signature s: %s\n", BN_bn2hex(sig->s));

    p_bn_pubx = BN_bin2bn((uint8_t *)pubx->p, pubx->n * 4, NULL);
    printf("pubkey x: %s\n", BN_bn2hex(p_bn_pubx));

    p_bn_puby = BN_bin2bn((uint8_t *)puby->p, puby->n * 4, NULL);
    printf("pubkey y: %s\n", BN_bn2hex(p_bn_puby));

    ec_group = EC_GROUP_new_by_curve_name(NID_secp384r1);

    ec_point = EC_POINT_new(ec_group);

    ret = EC_POINT_set_affine_coordinates_GFp(ec_group, ec_point, p_bn_pubx, p_bn_puby, NULL);
    if (ret != 1)
    {
        printf("bad point 0x%08x\n", ret);
    }

    EC_KEY *ec_key;
    ec_key = EC_KEY_new_by_curve_name(NID_secp384r1);
    ret = EC_KEY_set_public_key(ec_key, ec_point);
    if (ret != 1)
    {
        printf("bad public key 0x%08x\n", ret);
    }

    ret = EC_KEY_check_key(ec_key);
    if (ret != 1)
    {
        printf("bad EC key 0x%08x\n", ret);
    }

    ret = ECDSA_do_verify((uint8_t *)hash->p, hash->n * 4, sig, ec_key);

    BN_free(p_bn_pubx);
    BN_free(p_bn_puby);

    if (ret == -1 || ret == 0)
    {
        return TEST_FAIL;
    }

    return TEST_PASS;
}

int main(void)
{
    return(0);
}

