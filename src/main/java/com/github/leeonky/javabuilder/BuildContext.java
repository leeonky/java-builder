package com.github.leeonky.javabuilder;

import java.util.Map;

public class BuildContext {
    private final Map<String, Object> params;
    private final Map<String, Object> properties;
    private final int sequence;

    public BuildContext(int sequence, Map<String, Object> properties, Map<String, Object> params) {
        this.params = params;
        this.properties = properties;
        this.sequence = sequence;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public int getSequence() {
        return sequence;
    }
}
