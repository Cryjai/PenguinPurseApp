package com.acry.penguinpurse;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class BudgetEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String month;
    public String category;
    public double amount;
}
