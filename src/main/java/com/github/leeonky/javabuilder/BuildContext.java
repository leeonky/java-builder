package com.github.leeonky.javabuilder;

import java.util.Map;

public class BuildContext<T> {
    private final int sequence;
    private final Map<String, Object> params;

    BuildContext(int sequence, Map<String, Object> params) {
        this.sequence = sequence;
        this.params = params;
    }

    public int getSequence() {
        return sequence;
    }

    @SuppressWarnings("unchecked")
    public <T> T param(String name) {
        return (T) params.get(name);
    }
}
