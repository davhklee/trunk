    addi x2, x0, 1
    li x2, 1
loop:
    sub x1, x1, x2
    sw x1, 4(x0)
    blt x0, x1, loop
    hlt

