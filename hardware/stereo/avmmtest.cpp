
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "HLS/hls.h"

#define IMAGE_HEIGHT  (256)
#define IMAGE_WIDTH   (256)
#define IMAGE_SIZE    (IMAGE_HEIGHT * IMAGE_WIDTH)
#define BLOCK_SIZE    (3)
#define SCAN_SIZE     (60)

typedef unsigned char pixel;
typedef ihc::mm_master<pixel, ihc::aspace<1>, ihc::dwidth<8>, ihc::awidth<32>> MMIn;
typedef ihc::mm_master<pixel, ihc::aspace<2>, ihc::dwidth<8>, ihc::awidth<32>> MMOut;

template <int y, int h, int w, int bz, int sz>
void match_block(MMIn *A_in, MMIn *B_in, MMOut *C_out)
{

    hls_memory pixel cacheA[bz][w];
    hls_memory pixel cacheB[bz][w];
    hls_memory int ssd[bz * bz];

    // load from avalon memory map master to internal memory
    for(int i=0;i<(bz * w);i++)
    {
        int row = i / w;
        int col = i % w;
        cacheA[row][col] = (*A_in)[y * w + i];
        cacheB[row][col] = (*B_in)[y * w + i];
    }

    for(int x=0;x<(w - bz);x++)
    {
        int min_err = -1;
        int min_idx = -1;

        int min_x = (x - sz);
        if(min_x < 0)
        {
            min_x = 0;
        }

        for(int s=min_x;s<=x;s++)
        {
            ssd[0] = cacheA[0][x] - cacheB[0][s];
            ssd[1] = cacheA[0][x+1] - cacheB[0][s+1];
            ssd[2] = cacheA[0][x+2] - cacheB[0][s+2];
            ssd[3] = cacheA[1][x] - cacheB[1][s];
            ssd[4] = cacheA[1][x+1] - cacheB[1][s+1];
            ssd[5] = cacheA[1][x+2] - cacheB[1][s+2];
            ssd[6] = cacheA[2][x] - cacheB[2][s];
            ssd[7] = cacheA[2][x+1] - cacheB[2][s+1];
            ssd[8] = cacheA[2][x+2] - cacheB[2][s+2];

            int sum = 0;
            for(int i=0;i<(bz * bz);i++)
            {
                ssd[i] = ssd[i] * ssd[i];
                sum += ssd[i];
            }

            if(sum < min_err || min_idx == -1)
            {
                min_err = sum;
                min_idx = s;
            }
        }

        (*C_out)[y * w + x] = (x - min_idx);
    }

}

component int fullchip(int timer, MMIn &A_in, MMIn &B_in, MMOut &C_out)
{

  ihc::launch((match_block<0, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>), &A_in, &B_in, &C_out);
  ihc::launch((match_block<1, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>), &A_in, &B_in, &C_out);
  ihc::launch((match_block<2, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>), &A_in, &B_in, &C_out);
  ihc::launch((match_block<3, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>), &A_in, &B_in, &C_out);
  ihc::launch((match_block<4, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>), &A_in, &B_in, &C_out);
  ihc::collect((match_block<0, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>));
  ihc::collect((match_block<1, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>));
  ihc::collect((match_block<2, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>));
  ihc::collect((match_block<3, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>));
  ihc::collect((match_block<4, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>));
  //repeat for every line

#if 0
  match_block<0, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>(&A_in, &B_in, &C_out);
  match_block<1, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>(&A_in, &B_in, &C_out);
  match_block<2, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>(&A_in, &B_in, &C_out);
  match_block<3, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>(&A_in, &B_in, &C_out);
  match_block<4, IMAGE_HEIGHT, IMAGE_WIDTH, BLOCK_SIZE, SCAN_SIZE>(&A_in, &B_in, &C_out);
  //repeat for every line
#endif

  return(~timer);
}

int main(void)
{

  int timer = 0xaaaaaaaa;

  pixel left[IMAGE_SIZE];
  pixel right[IMAGE_SIZE];
  pixel stereo[IMAGE_SIZE];

  MMIn A_in(left, IMAGE_SIZE * sizeof(pixel));
  MMIn B_in(right, IMAGE_SIZE * sizeof(pixel));
  MMOut C_out(stereo, IMAGE_SIZE * sizeof(pixel));

  // testbench vector
  for (int i = 0; i < IMAGE_SIZE; ++i)
  {
    left[i] = i & 0xff;
    right[i] = i & 0xff;
    stereo[i] = 0xaa;
  }

  timer = fullchip(timer, A_in, B_in, C_out);

  bool passed = true;
  for (int i = 0; i < 5 * IMAGE_WIDTH; ++i)
  {
    bool data_okay = (stereo[i] == 0);
    passed &= data_okay;
    if (!data_okay)
      printf("ERROR: stereo[%d] = %d != %d\n", i, stereo[i], 0);
  }

  if (passed)
    printf("PASSED\n");
  else
    printf("FAILED\n");

  return 0;

}

