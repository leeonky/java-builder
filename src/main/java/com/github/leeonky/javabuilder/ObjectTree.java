package com.github.leeonky.javabuilder;

import java.util.*;
import java.util.function.Consumer;

class ObjectTree {
    private Map<Object, List<Object>> nodes = new HashMap<>();

    void addNode(Object parent, Object node) {
        nodes.computeIfAbsent(parent, k -> new ArrayList<>()).add(node);
    }

    void foreach(Object root, Consumer<Object> consumer) {
        nodes.getOrDefault(root, Collections.emptyList()).forEach(o -> consume(consumer, o));
    }

    private void consume(Consumer<Object> consumer, Object o) {
        nodes.getOrDefault(o, Collections.emptyList()).forEach(s -> consume(consumer, s));
        consumer.accept(o);
    }
}
