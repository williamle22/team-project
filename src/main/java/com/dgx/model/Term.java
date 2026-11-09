package com.dgx.model;

public enum Term {
    FALL("FALL"),
    WINTER("WINTER"),
    SUMMER("SUMMER");

    private final String name;

    Term(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
