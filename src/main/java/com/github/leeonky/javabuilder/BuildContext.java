package com.github.leeonky.javabuilder;

public class BuildContext<T> {
    private final int sequence;

    BuildContext(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }
}
