package com.acry.penguinpurse;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        entities = {FinanceRecord.class, CategoryEntity.class, BudgetEntity.class, SettingsEntity.class, MonthlySnapshot.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FinanceDao dao();
}
