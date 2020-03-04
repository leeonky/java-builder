package com.github.leeonky.javabuilder;

import com.github.leeonky.javabuilder.spec.ObjectTree;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class ObjectTreeTest {
    private List<Integer> result = new ArrayList<>();

    @Test
    void add_nodes() {
        ObjectTree objectTree = new ObjectTree();
        objectTree.addNode(1, 2);
        objectTree.addNode(2, 3);
        objectTree.addNode(2, 4);

        objectTree.foreach(1, o -> result.add((int) o));

        assertThat(result).isEqualTo(asList(3, 4, 2));
    }
}