package com.nz.jnawintools.input;

import lombok.Getter;

import java.nio.ByteBuffer;

@Getter
public class InputRingBuffer {
    private static final int EVENT_SIZE = 32;
    private static final int CAPACITY = 1024;

    private final ByteBuffer buffer =
            ByteBuffer.allocateDirect(EVENT_SIZE * CAPACITY);

    private final int mask = CAPACITY - 1;

    private volatile int writeIndex = 0;
    private volatile int readIndex = 0;

    public ByteBuffer buffer() {
        return buffer;
    }

    public int capacity() {
        return CAPACITY;
    }

    public int eventSize() {
        return EVENT_SIZE;
    }

}
