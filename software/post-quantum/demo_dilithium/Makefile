CC = g++

CFLAGS = -Wall -g -fpermissive -pedantic -I./

TARGET = fips204_demo

SRCS = sign.c packing.c polyvec.c poly.c ntt.c reduce.c rounding.c fips202.c symmetric-shake.c randombytes.c main.c

OBJS = $(SRCS:.c=.o)

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) $(OBJS) -o $(TARGET)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -f $(TARGET) $(OBJS)

