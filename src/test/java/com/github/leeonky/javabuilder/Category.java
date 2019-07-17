package com.github.leeonky.javabuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Category {
    private String name;
    private int level;
}
