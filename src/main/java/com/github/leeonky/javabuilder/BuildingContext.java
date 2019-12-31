package com.github.leeonky.javabuilder;

import java.util.ArrayList;
import java.util.List;

public class BuildingContext {
    private List<Object> unSavedObjects = new ArrayList<>();

    public List<Object> getUnSavedObjects() {
        return unSavedObjects;
    }
}
