package com.acry.penguinpurse;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface FinanceDao {
    @Query("SELECT * FROM records ORDER BY date DESC, updatedAt DESC")
    List<FinanceRecord> allRecords();

    @Query("SELECT * FROM records WHERE date >= :start AND date < :end ORDER BY date DESC")
    List<FinanceRecord> recordsBetween(String start, String end);

    @Query("SELECT * FROM records WHERE type = :type AND (:category = '' OR category = :category) ORDER BY date DESC")
    List<FinanceRecord> filteredRecords(String type, String category);

    @Query("SELECT * FROM records WHERE (:type = '' OR type = :type) AND (:category = '' OR category = :category) ORDER BY date DESC")
    List<FinanceRecord> filteredRecordsFlexible(String type, String category);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM records WHERE type = :type AND date >= :start AND date < :end")
    double sumByTypeBetween(String type, String start, String end);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM records WHERE type = 'expense' AND category = :category AND date >= :start AND date < :end")
    double expenseForCategoryBetween(String category, String start, String end);

    @Insert
    long insertRecord(FinanceRecord record);

    @Update
    void updateRecord(FinanceRecord record);

    @Delete
    void deleteRecord(FinanceRecord record);

    @Query("DELETE FROM records")
    void deleteAllRecords();

    @Query("SELECT * FROM categories ORDER BY preset DESC, name ASC")
    List<CategoryEntity> allCategories();

    @Query("SELECT * FROM categories WHERE type = :type OR type = 'both' ORDER BY preset DESC, name ASC")
    List<CategoryEntity> categoriesForType(String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(CategoryEntity category);

    @Query("SELECT COUNT(*) FROM categories")
    int categoryCount();

    @Query("SELECT * FROM budgets WHERE month = :month ORDER BY category IS NOT NULL, category ASC")
    List<BudgetEntity> budgetsForMonth(String month);

    @Query("SELECT * FROM budgets ORDER BY month DESC, category ASC")
    List<BudgetEntity> allBudgets();

    @Query("SELECT * FROM budgets WHERE month = :month AND category IS NULL LIMIT 1")
    BudgetEntity overallBudget(String month);

    @Query("SELECT * FROM budgets WHERE month = :month AND category = :category LIMIT 1")
    BudgetEntity categoryBudget(String month, String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertBudget(BudgetEntity budget);

    @Update
    void updateBudget(BudgetEntity budget);

    @Query("DELETE FROM budgets")
    void deleteAllBudgets();

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    SettingsEntity settings();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveSettings(SettingsEntity settings);

    @Query("SELECT * FROM monthly_snapshots WHERE month = :month LIMIT 1")
    MonthlySnapshot snapshot(String month);

    @Query("SELECT * FROM monthly_snapshots ORDER BY month DESC")
    List<MonthlySnapshot> allSnapshots();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveSnapshot(MonthlySnapshot snapshot);

    @Query("DELETE FROM monthly_snapshots")
    void deleteAllSnapshots();
}
