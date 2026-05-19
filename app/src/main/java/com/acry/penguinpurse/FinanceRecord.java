package com.acry.penguinpurse;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "records")
public class FinanceRecord {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String date;
    public double amount;
    public String type;
    public String category;
    public String note;
    public String currency;
    public long createdAt;
    public long updatedAt;
}
