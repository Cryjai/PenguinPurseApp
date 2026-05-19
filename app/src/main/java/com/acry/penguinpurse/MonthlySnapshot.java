package com.acry.penguinpurse;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "monthly_snapshots")
public class MonthlySnapshot {
    @PrimaryKey
    @NonNull
    public String month;
    public double income;
    public double expense;
    public double net;
    public double appliedToSaving;
    public boolean needsRecalculation;
    public long processedAt;
}
