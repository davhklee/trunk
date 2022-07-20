#include <demo_vector.h>
#include <iostream>
#include <stdint.h>
#include <string.h>

using std::cout;
using std::endl;
using std::string;

int pqcrystals_dilithium5_ref_verify_internal(const uint8_t *sig, size_t siglen,
                                              const uint8_t *m, size_t mlen,
                                              const uint8_t *pre, size_t prelen,
                                              const uint8_t *pk);

int pqcrystals_dilithium5_ref_verify(const uint8_t *sig, size_t siglen,
                                     const uint8_t *m, size_t mlen,
                                     const uint8_t *ctx, size_t ctxlen,
                                     const uint8_t *pk);

int main(int argc, char *argv[]) {
  int result = 0;

  cout << "Demo program for FIPS204 Dilithium" << endl;
  cout << "Total arguments: " << argc << endl;
  for (int i = 0; i < argc; i++) {
    cout << "Argument " << i << ": " << argv[i] << endl;
  }

  result = pqcrystals_dilithium5_ref_verify_internal(
      mode5_sigver_tc169_sig, sizeof(mode5_sigver_tc169_sig),
      mode5_sigver_tc169_msg, sizeof(mode5_sigver_tc169_msg), nullptr, 0,
      mode5_sigver_tc169_pk);
  if (result == 0) {
    cout << "Internal Successful!" << endl;
  } else {
    cout << "Internal Failed!" << endl;
  }

  result = pqcrystals_dilithium5_ref_verify(
      mode5_sigver_tc63_sig, sizeof(mode5_sigver_tc63_sig),
      mode5_sigver_tc63_msg, sizeof(mode5_sigver_tc63_msg),
      mode5_sigver_tc63_ctx, sizeof(mode5_sigver_tc63_ctx),
      mode5_sigver_tc63_pk);
  if (result == 0) {
    cout << "External Successful!" << endl;
  } else {
    cout << "External Failed!" << endl;
  }

  return 0;
}
