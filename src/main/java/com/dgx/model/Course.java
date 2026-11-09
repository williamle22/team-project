package com.dgx.model;

import lombok.Getter;
import lombok.Setter;

/*
CREATE TABLE courses (
    code TEXT PRIMARY KEY NOT NULL,   -- CSC148H1
    designator TEXT NOT NULL,         -- CSC: code[0:3]
    number TEXT NOT NULL,             -- 148: code[3:6]
    level INTEGER NOT NULL,           -- 100-400: code[3] is 1-4 or A-D
    weight FLOAT NOT NULL,            -- 0.5 if code[6] is H, 1.0 if Y
    campus TEXT NOT NULL,             -- code[7]: 0 online, 1 utsg, 3 utsc, 5 utm
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    prerequisites TEXT,               -- requisite JSON, or NULL if none
    corequisites TEXT,                -- requisite JSON, or NULL if none
    exclusions TEXT,                  -- exclusions JSON, or NULL if none
    breadth TEXT,                     -- breadth JSON, or NULL if none
    sessions TEXT                     -- sessions JSON, or NULL if not offered
) WITHOUT ROWID;
 */

// TODO-Jonathan: look at how the structure looks like from the api
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
