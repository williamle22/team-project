package com.dgx.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Course {
    private String code;
    private String designator;
    private int number; // TODO: should this be string?
    private int level;
    private String campus;
    private String title;
    private String description;
}
