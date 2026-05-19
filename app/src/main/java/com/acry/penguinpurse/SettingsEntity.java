package com.acry.penguinpurse;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings")
public class SettingsEntity {
    @PrimaryKey
    public int id = 1;
    public String userName = "";
    public String baseCurrency = "$";
    public double currentSaving = 0;
    public double savingTarget = 10000;
    public String lastProcessedMonth = "";
}
