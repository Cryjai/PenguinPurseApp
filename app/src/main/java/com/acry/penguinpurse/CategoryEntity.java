package com.acry.penguinpurse;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String emoji;
    public String type;
    public boolean preset;
}
