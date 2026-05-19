package com.acry.penguinpurse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.room.Room;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(219, 243, 255);
    private static final int DEEP = Color.rgb(36, 73, 96);
    private static final int CARD = Color.WHITE;
    private static final int ACCENT = Color.rgb(46, 143, 204);
    private static final int WARN = Color.rgb(185, 78, 69);
    private static final int OK = Color.rgb(50, 133, 95);
    private static final int REQ_EXPORT = 10;
    private static final int REQ_IMPORT = 11;

    private AppDatabase db;
    private FinanceDao dao;
    private LinearLayout root;
    private LinearLayout content;
    private SettingsEntity settings;
    private String pendingExport = "";
    private String pendingMime = "text/plain";
    private String currentTab = "Home";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "penguin_purse.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
        dao = db.dao();
        seedDefaults();
        settings = getSettings();
        processMonthRollover();
        buildShell();
        showHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dao != null) {
            processMonthRollover();
            if (content != null) route(currentTab);
        }
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(246, 251, 255));
        setContentView(root);

        LinearLayout top = row();
        top.setPadding(dp(18), dp(12), dp(12), dp(4));
        TextView title = text("Penguin Purse", 24, DEEP, true);
        top.addView(title, weight());
        Button settingsTop = pill("⚙");
        settingsTop.setOnClickListener(v -> showSettings());
        top.addView(settingsTop);
        root.addView(top);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(8), dp(14), dp(14));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        HorizontalScrollView navWrap = new HorizontalScrollView(this);
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(6), dp(8), dp(8));
        navWrap.addView(nav);
        for (String item : Arrays.asList("Home", "Records", "Stats", "Budget & Savings", "Coach")) {
            Button b = pill(item);
            b.setOnClickListener(v -> route(item));
            nav.addView(b);
        }
        root.addView(navWrap);
    }

    private void route(String tab) {
        currentTab = tab;
        if ("Home".equals(tab)) showHome();
        else if ("Records".equals(tab)) showRecords();
        else if ("Stats".equals(tab)) showStats();
        else if ("Budget & Savings".equals(tab) || "Budget".equals(tab)) showBudget();
        else if ("Savings".equals(tab)) showSavings();
        else if ("Coach".equals(tab)) showCoach();
        else showSettings();
    }

    private void clear() {
        content.removeAllViews();
        settings = getSettings();
    }

    private void showHome() {
        clear();
        YearMonth ym = YearMonth.now();
        Summary month = summaryForMonth(ym);
        double balance = month.income - month.expense + settings.currentSaving;
        LinearLayout balanceCard = card();
        balanceCard.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout balanceText = new LinearLayout(this);
        balanceText.setOrientation(LinearLayout.VERTICAL);
        balanceText.addView(text("Balance", 14, Color.GRAY, false));
        balanceText.addView(text(settings.baseCurrency + money(balance), 31, DEEP, true));
        balanceText.addView(text("Total Balance", 17, Color.rgb(112, 80, 32), true));
        balanceText.addView(text("Income " + settings.baseCurrency + money(month.income) + " · Expense " + settings.baseCurrency + money(month.expense), 13, Color.rgb(74, 92, 104), false));
        balanceCard.addView(balanceText, weight());
        balanceCard.addView(mascot(chooseMascot(month), 124));
        content.addView(balanceCard);

        TextView bubble = text(microcopy(month), 14, DEEP, true);
        bubble.setGravity(Gravity.CENTER);
        content.addView(bubble);

        LinearLayout quick = row();
        LinearLayout daily = miniCard("Daily Check", "Review today", R.drawable.penguin_writing);
        daily.setOnClickListener(v -> showStats());
        LinearLayout add = miniCard("Add Record", "+", R.drawable.penguin_money);
        add.setOnClickListener(v -> showRecordForm(null, "expense"));
        quick.addView(daily, weight());
        quick.addView(add, weight());
        content.addView(quick);

        content.addView(section("Expense Chart"));
        LinearLayout penguinStrip = row();
        penguinStrip.addView(mascot(R.drawable.penguin_eating, 58));
        penguinStrip.addView(mascot(R.drawable.penguin_shopping, 58));
        penguinStrip.addView(mascot(R.drawable.penguin_travel, 58));
        penguinStrip.addView(mascot(R.drawable.penguin_writing, 58));
        content.addView(penguinStrip);
        content.addView(progressCard("Budget used", month.expense, getOverallBudgetAmount(ym), WARN));
        content.addView(progressCard("Saving target", settings.currentSaving, settings.savingTarget, OK));

        BarChartView chart = new BarChartView(this);
        chart.setData(lastSevenDays(), settings.baseCurrency);
        content.addView(chart, new LinearLayout.LayoutParams(-1, dp(270)));
    }

    private void showRecords() {
        clear();
        addMascotHeader("Records", R.drawable.penguin_writing, "Edit your money history before it gaslights you.");
        Button add = pill("Add Record");
        add.setOnClickListener(v -> showRecordForm(null, "expense"));
        content.addView(add);

        LinearLayout filters = row();
        Button all = pill("All");
        Button exp = pill("Expense");
        Button inc = pill("Income");
        all.setOnClickListener(v -> renderRecordList(dao.allRecords()));
        exp.setOnClickListener(v -> renderRecordList(dao.filteredRecords("expense", "")));
        inc.setOnClickListener(v -> renderRecordList(dao.filteredRecords("income", "")));
        filters.addView(all); filters.addView(exp); filters.addView(inc);
        content.addView(filters);
        LinearLayout categoryFilters = row();
        Spinner catFilter = spinner(withAllCategories());
        Button apply = pill("Filter category");
        apply.setOnClickListener(v -> {
            String chosen = catFilter.getSelectedItem().toString();
            renderRecordList(dao.filteredRecordsFlexible("", "All categories".equals(chosen) ? "" : chosen));
        });
        categoryFilters.addView(catFilter, weight());
        categoryFilters.addView(apply);
        content.addView(categoryFilters);
        renderRecordList(dao.allRecords());
    }

    private void renderRecordList(List<FinanceRecord> records) {
        while (content.getChildCount() > 4) content.removeViewAt(4);
        if (records.isEmpty()) {
            content.addView(text("No records yet. Your wallet is mysterious, not organized.", 16, Color.GRAY, false));
            return;
        }
        for (FinanceRecord r : records) {
            LinearLayout c = card();
            LinearLayout top = row();
            top.addView(text(("income".equals(r.type) ? "+" : "-") + settings.baseCurrency + money(r.amount), 20, "income".equals(r.type) ? OK : WARN, true), weight());
            top.addView(text(r.date, 14, Color.GRAY, false));
            c.addView(top);
            c.addView(text(r.category + " · " + (r.note == null ? "" : r.note), 15, DEEP, false));
            LinearLayout buttons = row();
            Button edit = pill("Edit");
            edit.setOnClickListener(v -> showRecordForm(r, r.type));
            Button del = pill("Delete");
            del.setOnClickListener(v -> confirmDelete(r));
            buttons.addView(edit); buttons.addView(del);
            c.addView(buttons);
            content.addView(c);
        }
    }

    private void showRecordForm(FinanceRecord existing, String initialType) {
        clear();
        boolean edit = existing != null;
        addMascotHeader(edit ? "Edit Record" : "Add Record", R.drawable.penguin_money, "Small numbers become big consequences. Shocking, I know.");
        LinearLayout form = card();
        final String[] type = {initialType};
        LinearLayout toggle = row();
        Button income = pill("Income");
        Button expense = pill("Expense");
        toggle.addView(income, weight());
        toggle.addView(expense, weight());
        form.addView(toggle);

        EditText amount = input("Amount", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (edit) amount.setText(String.valueOf(existing.amount));
        form.addView(amount);

        LinearLayout calc = row();
        EditText a = input("A", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        EditText b = input("B", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        Spinner op = spinner(new String[]{"+", "-", "×", "÷"});
        Button use = pill("Calc → Amount");
        use.setOnClickListener(v -> {
            double av = parse(a.getText().toString()), bv = parse(b.getText().toString()), res = 0;
            String o = op.getSelectedItem().toString();
            if ("+".equals(o)) res = av + bv;
            else if ("-".equals(o)) res = av - bv;
            else if ("×".equals(o)) res = av * bv;
            else res = bv == 0 ? 0 : av / bv;
            amount.setText(money(Math.abs(res)));
        });
        calc.addView(a, weight()); calc.addView(op); calc.addView(b, weight());
        form.addView(calc);
        form.addView(use);

        TextView dateView = text(edit ? existing.date : LocalDate.now().toString(), 16, DEEP, true);
        Button pickDate = pill("Pick Date");
        pickDate.setOnClickListener(v -> pickDate(dateView));
        form.addView(label("Date")); form.addView(dateView); form.addView(pickDate);

        Spinner category = spinner(categoryLabels(type[0]));
        form.addView(label("Category")); form.addView(category);
        addCategoryVisualButtons(form, category);

        Button customCat = pill("+ Custom category");
        customCat.setOnClickListener(v -> showAddCategoryDialog(type[0], () -> {
            category.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryLabels(type[0])));
        }));
        form.addView(customCat);

        EditText note = input("Description / note", InputType.TYPE_CLASS_TEXT);
        if (edit) note.setText(existing.note);
        form.addView(note);

        income.setOnClickListener(v -> {
            type[0] = "income";
            category.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryLabels(type[0])));
        });
        expense.setOnClickListener(v -> {
            type[0] = "expense";
            category.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryLabels(type[0])));
        });

        Button save = pill(edit ? "Save Changes" : "Save Record");
        save.setOnClickListener(v -> {
            if (amount.getText().toString().trim().isEmpty()) {
                toast("Amount first,財神唔會讀心術。");
                return;
            }
            FinanceRecord r = edit ? existing : new FinanceRecord();
            String oldMonth = edit ? monthOf(existing.date) : null;
            r.amount = parse(amount.getText().toString());
            r.type = type[0];
            r.date = dateView.getText().toString();
            r.category = category.getSelectedItem() == null ? "Other" : category.getSelectedItem().toString();
            r.note = note.getText().toString();
            r.currency = settings.baseCurrency;
            long now = System.currentTimeMillis();
            if (!edit) {
                r.createdAt = now;
                r.updatedAt = now;
                dao.insertRecord(r);
            } else {
                r.updatedAt = now;
                dao.updateRecord(r);
                recalculateClosedMonth(oldMonth);
            }
            recalculateClosedMonth(monthOf(r.date));
            processMonthRollover();
            showHome();
        });
        form.addView(save);
        LinearLayout helper = row();
        helper.addView(mascot(R.drawable.penguin_writing, 70));
        helper.addView(text("Tap Save before your memory deletes the receipt like a scammer.", 13, Color.GRAY, false), weight());
        form.addView(helper);
        content.addView(form);
    }

    private void showStats() {
        clear();
        addMascotHeader("Stats", R.drawable.penguin_travel, "Charts: because vibes are not accounting.");
        YearMonth now = YearMonth.now();
        Summary month = summaryForMonth(now);
        content.addView(kpiRow("This month", month));
        BarChartView weekly = new BarChartView(this);
        weekly.setData(lastSevenDays(), settings.baseCurrency);
        content.addView(section("Daily"));
        content.addView(weekly, new LinearLayout.LayoutParams(-1, dp(260)));
        BarChartView weeks = new BarChartView(this);
        weeks.setData(lastFourWeeks(), settings.baseCurrency);
        content.addView(section("Weekly"));
        content.addView(weeks, new LinearLayout.LayoutParams(-1, dp(260)));
        BarChartView months = new BarChartView(this);
        months.setData(lastSixMonths(), settings.baseCurrency);
        content.addView(section("Monthly trend"));
        content.addView(months, new LinearLayout.LayoutParams(-1, dp(260)));
        BarChartView years = new BarChartView(this);
        years.setData(lastThreeYears(), settings.baseCurrency);
        content.addView(section("Yearly"));
        content.addView(years, new LinearLayout.LayoutParams(-1, dp(260)));
        content.addView(section("Expense by category"));
        Map<String, Double> cat = categoryExpenseMap(now);
        for (String k : cat.keySet()) content.addView(text(k + ": " + settings.baseCurrency + money(cat.get(k)), 16, DEEP, false));
        LinearLayout statBirds = row();
        statBirds.addView(mascot(R.drawable.penguin_camera, 64));
        statBirds.addView(text("Penguin paparazzi caught your spending habits.", 14, Color.GRAY, false), weight());
        content.addView(statBirds);
        Button csv = pill("Export report CSV");
        csv.setOnClickListener(v -> exportText("penguin_report.csv", "text/csv", reportCsv()));
        content.addView(csv);
    }

    private void showBudget() {
        clear();
        addMascotHeader("Budget & Savings", R.drawable.penguin_rich_cash, "Budget first. Crying later is not a strategy.");
        YearMonth ym = YearMonth.now();
        Summary month = summaryForMonth(ym);
        LinearLayout savingTop = card();
        savingTop.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout savingText = new LinearLayout(this);
        savingText.setOrientation(LinearLayout.VERTICAL);
        savingText.addView(text("Current Saving", 14, Color.GRAY, false));
        savingText.addView(text(settings.baseCurrency + money(settings.currentSaving), 27, DEEP, true));
        savingText.addView(text("Target " + settings.baseCurrency + money(settings.savingTarget), 15, OK, true));
        Button updateSaving = pill("Edit saving");
        updateSaving.setOnClickListener(v -> promptSavings());
        savingText.addView(updateSaving);
        savingTop.addView(savingText, weight());
        savingTop.addView(mascot(savingMascot(), 118));
        content.addView(savingTop);
        content.addView(progressCard("Overall budget", month.expense, getOverallBudgetAmount(ym), WARN));
        Button setOverall = pill("Set monthly budget");
        setOverall.setOnClickListener(v -> promptBudget(null));
        content.addView(setOverall);
        content.addView(section("Category budgets"));
        for (CategoryEntity cat : dao.categoriesForType("expense")) {
            BudgetEntity b = dao.categoryBudget(ym.toString(), cat.emoji + " " + cat.name);
            double amount = b == null ? 0 : b.amount;
            double spent = dao.expenseForCategoryBetween(cat.emoji + " " + cat.name, startOf(ym), startOf(ym.plusMonths(1)));
            content.addView(progressCard(cat.emoji + " " + cat.name, spent, amount, WARN));
            Button set = pill("Set " + cat.name);
            set.setOnClickListener(v -> promptBudget(cat.emoji + " " + cat.name));
            content.addView(set);
        }
        content.addView(section("Saving Coach"));
        LinearLayout saving = card();
        saving.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout savingWords = new LinearLayout(this);
        savingWords.setOrientation(LinearLayout.VERTICAL);
        savingWords.addView(text("Target Amount", 14, Color.GRAY, false));
        savingWords.addView(text(settings.baseCurrency + money(settings.savingTarget), 24, DEEP, true));
        savingWords.addView(text("Saved " + settings.baseCurrency + money(settings.currentSaving), 15, OK, true));
        Button coach = pill("Open Coach");
        coach.setOnClickListener(v -> showCoach());
        savingWords.addView(coach);
        saving.addView(savingWords, weight());
        saving.addView(mascot(savingMascot(), 112));
        content.addView(saving);

        content.addView(section("Export / Import"));
        LinearLayout exportBox = card();
        Button json = pill("JSON Backup");
        json.setOnClickListener(v -> exportText("penguin_purse_backup.json", "application/json", exportJson().toString()));
        Button csv = pill("Export Data CSV");
        csv.setOnClickListener(v -> exportText("penguin_records.csv", "text/csv", recordsCsv()));
        Button imp = pill("Import JSON");
        imp.setOnClickListener(v -> importJsonFile());
        exportBox.addView(json);
        exportBox.addView(csv);
        exportBox.addView(imp);
        content.addView(exportBox);

        content.addView(section("Advanced AI Coach"));
        LinearLayout ai = card();
        ai.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout aiWords = new LinearLayout(this);
        aiWords.setOrientation(LinearLayout.VERTICAL);
        aiWords.addView(text("Custom Prompt", 17, DEEP, true));
        aiWords.addView(text("Copy your local finance summary and paste it into your own AI.", 13, Color.GRAY, false));
        Button copy = pill("Copy");
        copy.setOnClickListener(v -> {
            android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(android.content.ClipData.newPlainText("Penguin Purse AI Prompt", buildAiPrompt()));
            toast("Copied. External AI only, no API bill. 財務自由第一步：唔好亂燒錢。");
        });
        aiWords.addView(copy);
        ai.addView(aiWords, weight());
        ai.addView(mascot(R.drawable.penguin_accounting, 104));
        content.addView(ai);
    }

    private void showSavings() {
        clear();
        addMascotHeader("Savings", savingMascot(), "Savings is your future self not wanting to slap you.");
        content.addView(progressCard("Saving progress", settings.currentSaving, settings.savingTarget, OK));
        Button update = pill("Update saving amount / target");
        update.setOnClickListener(v -> promptSavings());
        content.addView(update);
        content.addView(section("Monthly rollover snapshots"));
        YearMonth start = YearMonth.now().minusMonths(5);
        for (int i = 0; i < 6; i++) {
            YearMonth ym = start.plusMonths(i);
            MonthlySnapshot s = dao.snapshot(ym.toString());
            if (s != null) {
                content.addView(text(ym + " net " + settings.baseCurrency + money(s.net) + " · applied " + settings.baseCurrency + money(s.appliedToSaving), 15, DEEP, false));
            }
        }
    }

    private void showCoach() {
        clear();
        addMascotHeader("Saving Coach", R.drawable.penguin_happy, "A plan beats hoping money magically respawns.");
        LinearLayout form = card();
        EditText have = input("Money you currently have", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText wantSave = input("How much you want to save", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText needSpend = input("How much you need to spend", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText thing = input("What do you want to buy?", InputType.TYPE_CLASS_TEXT);
        CheckBox needs = new CheckBox(this); needs.setText("Separate wants vs needs");
        TextView output = text("", 15, DEEP, false);
        Button plan = pill("Generate local saving plan");
        plan.setOnClickListener(v -> {
            double h = parse(have.getText().toString()), target = parse(wantSave.getText().toString()), need = parse(needSpend.getText().toString());
            double gap = Math.max(0, target - h);
            double safeMonthly = Math.max(0, (h - need) * 0.25);
            String item = thing.getText().toString().trim().isEmpty() ? "your goal" : thing.getText().toString().trim();
            output.setText("Plan for " + item + ":\nNeed gap: " + settings.baseCurrency + money(gap) +
                    "\nProtect needs first: " + settings.baseCurrency + money(need) +
                    "\nSuggested monthly saving: " + settings.baseCurrency + money(Math.max(50, safeMonthly)) +
                    "\nRule: needs survive, wants queue up. If it does not improve your life or income, penguin says wait.");
        });
        form.addView(have); form.addView(wantSave); form.addView(needSpend); form.addView(thing); form.addView(needs); form.addView(plan); form.addView(output);
        content.addView(form);

        content.addView(section("Advanced Personalized AI Coach"));
        TextView prompt = text(buildAiPrompt(), 14, DEEP, false);
        LinearLayout p = card();
        p.addView(prompt);
        Button copy = pill("Copy prompt");
        copy.setOnClickListener(v -> {
            android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(android.content.ClipData.newPlainText("Penguin Purse AI Prompt", buildAiPrompt()));
            toast("Copied. Paste into your own AI. No API bill, rich-girl brain move.");
        });
        p.addView(copy);
        content.addView(p);
    }

    private void showSettings() {
        clear();
        addMascotHeader("Profile / Settings", R.drawable.penguin_happy, "Local-first. Your data stays here, not floating around like gossip.");
        LinearLayout form = card();
        EditText name = input("Enter your name, eg. Acry", InputType.TYPE_CLASS_TEXT);
        name.setText(settings.userName);
        Spinner currency = spinner(new String[]{"$", "¥", "£", "€", "₽", "₹", "₺"});
        int idx = Arrays.asList("$", "¥", "£", "€", "₽", "₹", "₺").indexOf(settings.baseCurrency);
        currency.setSelection(Math.max(0, idx));
        Button save = pill("Save settings");
        save.setOnClickListener(v -> {
            settings.userName = name.getText().toString();
            settings.baseCurrency = currency.getSelectedItem().toString();
            dao.saveSettings(settings);
            toast("Saved. Tiny penguin bureaucracy completed.");
            showSettings();
        });
        form.addView(name); form.addView(label("Base currency")); form.addView(currency); form.addView(save);
        LinearLayout mascotLine = row();
        mascotLine.addView(mascot(R.drawable.penguin_sleeping, 72));
        mascotLine.addView(text("Settings live here now; bottom nav is for core money moves.", 13, Color.GRAY, false), weight());
        form.addView(mascotLine);
        content.addView(form);
        Button json = pill("Export JSON backup");
        json.setOnClickListener(v -> exportText("penguin_purse_backup.json", "application/json", exportJson().toString()));
        Button csv = pill("Export records CSV");
        csv.setOnClickListener(v -> exportText("penguin_records.csv", "text/csv", recordsCsv()));
        Button imp = pill("Import JSON backup");
        imp.setOnClickListener(v -> importJsonFile());
        content.addView(json); content.addView(csv); content.addView(imp);
        TextView footer = text("Made by Acry and her teammates\nhttps://github.com/Cryjai", 12, Color.GRAY, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(28), 0, dp(8));
        content.addView(footer);
    }

    private void processMonthRollover() {
        SettingsEntity s = getSettings();
        YearMonth current = YearMonth.now();
        if (s.lastProcessedMonth == null || s.lastProcessedMonth.isEmpty()) {
            s.lastProcessedMonth = current.toString();
            dao.saveSettings(s);
            settings = s;
            return;
        }
        YearMonth cursor = YearMonth.parse(s.lastProcessedMonth);
        while (cursor.isBefore(current)) {
            applySnapshot(cursor, s);
            s.lastProcessedMonth = cursor.plusMonths(1).toString();
            dao.saveSettings(s);
            cursor = cursor.plusMonths(1);
        }
        settings = s;
    }

    private void applySnapshot(YearMonth ym, SettingsEntity s) {
        Summary sum = summaryForMonth(ym);
        MonthlySnapshot old = dao.snapshot(ym.toString());
        double oldApplied = old == null ? 0 : old.appliedToSaving;
        double newApplied = sum.net;
        s.currentSaving += newApplied - oldApplied;
        MonthlySnapshot snap = new MonthlySnapshot();
        snap.month = ym.toString();
        snap.income = sum.income;
        snap.expense = sum.expense;
        snap.net = sum.net;
        snap.appliedToSaving = newApplied;
        snap.needsRecalculation = false;
        snap.processedAt = System.currentTimeMillis();
        dao.saveSnapshot(snap);
        dao.saveSettings(s);
    }

    private void recalculateClosedMonth(String month) {
        if (month == null || month.isEmpty()) return;
        YearMonth ym = YearMonth.parse(month);
        if (!ym.isBefore(YearMonth.now())) return;
        SettingsEntity s = getSettings();
        applySnapshot(ym, s);
        settings = getSettings();
    }

    private void seedDefaults() {
        SettingsEntity s = dao.settings();
        if (s == null) {
            s = new SettingsEntity();
            s.lastProcessedMonth = YearMonth.now().toString();
            dao.saveSettings(s);
        }
        if (dao.categoryCount() == 0) {
            addCat("Food", "🍙", "expense", true);
            addCat("Shopping", "🛍️", "expense", true);
            addCat("Travel", "🧳", "expense", true);
            addCat("Treats", "🍰", "expense", true);
            addCat("Study", "📚", "expense", true);
            addCat("Income", "💰", "income", true);
            addCat("Gift", "🎁", "income", true);
            addCat("Other", "🐧", "both", true);
        }
    }

    private void addCat(String name, String emoji, String type, boolean preset) {
        CategoryEntity c = new CategoryEntity();
        c.name = name; c.emoji = emoji; c.type = type; c.preset = preset;
        dao.insertCategory(c);
    }

    private SettingsEntity getSettings() {
        SettingsEntity s = dao.settings();
        if (s == null) {
            s = new SettingsEntity();
            s.lastProcessedMonth = YearMonth.now().toString();
            dao.saveSettings(s);
        }
        return s;
    }

    private Summary summaryForMonth(YearMonth ym) {
        String start = startOf(ym), end = startOf(ym.plusMonths(1));
        return new Summary(ym.toString(), dao.sumByTypeBetween("income", start, end), dao.sumByTypeBetween("expense", start, end));
    }

    private List<Summary> lastSevenDays() {
        List<Summary> out = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String start = d.toString(), end = d.plusDays(1).toString();
            out.add(new Summary(d.getDayOfWeek().toString().substring(0, 3), dao.sumByTypeBetween("income", start, end), dao.sumByTypeBetween("expense", start, end)));
        }
        return out;
    }

    private List<Summary> lastFourWeeks() {
        List<Summary> out = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 3; i >= 0; i--) {
            LocalDate end = today.minusWeeks(i).plusDays(1);
            LocalDate start = end.minusDays(7);
            int week = start.get(WeekFields.ISO.weekOfWeekBasedYear());
            out.add(new Summary("W" + week, dao.sumByTypeBetween("income", start.toString(), end.toString()), dao.sumByTypeBetween("expense", start.toString(), end.toString())));
        }
        return out;
    }

    private List<Summary> lastSixMonths() {
        List<Summary> out = new ArrayList<>();
        for (int i = 5; i >= 0; i--) out.add(summaryForMonth(YearMonth.now().minusMonths(i)));
        return out;
    }

    private List<Summary> lastThreeYears() {
        List<Summary> out = new ArrayList<>();
        int year = LocalDate.now().getYear();
        for (int i = 2; i >= 0; i--) {
            int y = year - i;
            String start = y + "-01-01";
            String end = (y + 1) + "-01-01";
            out.add(new Summary(String.valueOf(y), dao.sumByTypeBetween("income", start, end), dao.sumByTypeBetween("expense", start, end)));
        }
        return out;
    }

    private Map<String, Double> categoryExpenseMap(YearMonth ym) {
        Map<String, Double> map = new HashMap<>();
        for (FinanceRecord r : dao.recordsBetween(startOf(ym), startOf(ym.plusMonths(1)))) {
            if ("expense".equals(r.type)) map.put(r.category, map.getOrDefault(r.category, 0.0) + r.amount);
        }
        return map;
    }

    private double getOverallBudgetAmount(YearMonth ym) {
        BudgetEntity b = dao.overallBudget(ym.toString());
        return b == null ? 0 : b.amount;
    }

    private void promptBudget(String category) {
        EditText input = input(category == null ? "Monthly budget amount" : "Budget for " + category, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        new AlertDialog.Builder(this)
                .setTitle(category == null ? "Set monthly budget" : "Set category budget")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    YearMonth ym = YearMonth.now();
                    BudgetEntity b = category == null ? dao.overallBudget(ym.toString()) : dao.categoryBudget(ym.toString(), category);
                    if (b == null) b = new BudgetEntity();
                    b.month = ym.toString(); b.category = category; b.amount = parse(input.getText().toString());
                    if (b.id == 0) dao.insertBudget(b); else dao.updateBudget(b);
                    showBudget();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptSavings() {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(12), 0, dp(12), 0);
        EditText current = input("Current saving amount", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        current.setText(String.valueOf(settings.currentSaving));
        EditText target = input("Saving target", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        target.setText(String.valueOf(settings.savingTarget));
        box.addView(current); box.addView(target);
        new AlertDialog.Builder(this)
                .setTitle("Update savings")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    settings.currentSaving = parse(current.getText().toString());
                    settings.savingTarget = parse(target.getText().toString());
                    dao.saveSettings(settings);
                    showSavings();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(FinanceRecord r) {
        new AlertDialog.Builder(this)
                .setTitle("Delete record?")
                .setMessage("This removes the local record. No cloud fairy can rescue it.")
                .setPositiveButton("Delete", (d, w) -> {
                    String m = monthOf(r.date);
                    dao.deleteRecord(r);
                    recalculateClosedMonth(m);
                    showRecords();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddCategoryDialog(String type, Runnable after) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(12), 0, dp(12), 0);
        EditText emoji = input("Emoji", InputType.TYPE_CLASS_TEXT);
        EditText name = input("Category name", InputType.TYPE_CLASS_TEXT);
        box.addView(emoji); box.addView(name);
        new AlertDialog.Builder(this)
                .setTitle("Custom category")
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    addCat(name.getText().toString(), emoji.getText().toString(), type, false);
                    after.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String[] categoryLabels(String type) {
        List<CategoryEntity> cats = dao.categoriesForType(type);
        String[] arr = new String[cats.size()];
        for (int i = 0; i < cats.size(); i++) arr[i] = cats.get(i).emoji + " " + cats.get(i).name;
        return arr;
    }

    private String[] withAllCategories() {
        List<CategoryEntity> cats = dao.allCategories();
        String[] arr = new String[cats.size() + 1];
        arr[0] = "All categories";
        for (int i = 0; i < cats.size(); i++) arr[i + 1] = cats.get(i).emoji + " " + cats.get(i).name;
        return arr;
    }

    private JSONObject exportJson() {
        JSONObject root = new JSONObject();
        try {
            JSONObject st = new JSONObject();
            st.put("userName", settings.userName);
            st.put("baseCurrency", settings.baseCurrency);
            st.put("currentSaving", settings.currentSaving);
            st.put("savingTarget", settings.savingTarget);
            st.put("lastProcessedMonth", settings.lastProcessedMonth);
            root.put("settings", st);
            JSONArray records = new JSONArray();
            for (FinanceRecord r : dao.allRecords()) {
                JSONObject o = new JSONObject();
                o.put("date", r.date); o.put("amount", r.amount); o.put("type", r.type); o.put("category", r.category);
                o.put("note", r.note); o.put("currency", r.currency); o.put("createdAt", r.createdAt); o.put("updatedAt", r.updatedAt);
                records.put(o);
            }
            root.put("records", records);
            JSONArray budgets = new JSONArray();
            for (BudgetEntity b : dao.allBudgets()) {
                JSONObject o = new JSONObject();
                o.put("month", b.month); o.put("category", b.category); o.put("amount", b.amount);
                budgets.put(o);
            }
            root.put("budgets", budgets);
            JSONArray categories = new JSONArray();
            for (CategoryEntity c : dao.allCategories()) {
                JSONObject o = new JSONObject();
                o.put("name", c.name); o.put("emoji", c.emoji); o.put("type", c.type); o.put("preset", c.preset);
                categories.put(o);
            }
            root.put("categories", categories);
            JSONArray snapshots = new JSONArray();
            for (MonthlySnapshot s : dao.allSnapshots()) {
                JSONObject o = new JSONObject();
                o.put("month", s.month); o.put("income", s.income); o.put("expense", s.expense); o.put("net", s.net);
                o.put("appliedToSaving", s.appliedToSaving); o.put("needsRecalculation", s.needsRecalculation); o.put("processedAt", s.processedAt);
                snapshots.put(o);
            }
            root.put("monthlySnapshots", snapshots);
        } catch (Exception ignored) {}
        return root;
    }

    private void importJsonFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/json");
        startActivityForResult(i, REQ_IMPORT);
    }

    private void importJson(String raw, boolean overwrite) {
        try {
            JSONObject root = new JSONObject(raw);
            if (overwrite) {
                dao.deleteAllRecords(); dao.deleteAllBudgets(); dao.deleteAllSnapshots();
            }
            JSONObject st = root.optJSONObject("settings");
            if (st != null) {
                settings.userName = st.optString("userName", settings.userName);
                settings.baseCurrency = st.optString("baseCurrency", settings.baseCurrency);
                settings.currentSaving = st.optDouble("currentSaving", settings.currentSaving);
                settings.savingTarget = st.optDouble("savingTarget", settings.savingTarget);
                settings.lastProcessedMonth = st.optString("lastProcessedMonth", YearMonth.now().toString());
                dao.saveSettings(settings);
            }
            JSONArray records = root.optJSONArray("records");
            if (records != null) {
                for (int i = 0; i < records.length(); i++) {
                    JSONObject o = records.getJSONObject(i);
                    FinanceRecord r = new FinanceRecord();
                    r.date = o.optString("date", LocalDate.now().toString());
                    r.amount = o.optDouble("amount", 0);
                    r.type = o.optString("type", "expense");
                    r.category = o.optString("category", "🐧 Other");
                    r.note = o.optString("note", "");
                    r.currency = o.optString("currency", settings.baseCurrency);
                    r.createdAt = o.optLong("createdAt", System.currentTimeMillis());
                    r.updatedAt = o.optLong("updatedAt", System.currentTimeMillis());
                    dao.insertRecord(r);
                }
            }
            JSONArray budgets = root.optJSONArray("budgets");
            if (budgets != null) {
                for (int i = 0; i < budgets.length(); i++) {
                    JSONObject o = budgets.getJSONObject(i);
                    BudgetEntity b = new BudgetEntity();
                    b.month = o.optString("month", YearMonth.now().toString());
                    b.category = o.isNull("category") ? null : o.optString("category", null);
                    b.amount = o.optDouble("amount", 0);
                    dao.insertBudget(b);
                }
            }
            JSONArray categories = root.optJSONArray("categories");
            if (categories != null) {
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject o = categories.getJSONObject(i);
                    CategoryEntity c = new CategoryEntity();
                    c.name = o.optString("name", "Other");
                    c.emoji = o.optString("emoji", "🐧");
                    c.type = o.optString("type", "both");
                    c.preset = o.optBoolean("preset", false);
                    dao.insertCategory(c);
                }
            }
            processMonthRollover();
            toast("Import completed locally.");
            showSettings();
        } catch (Exception e) {
            toast("Import failed: " + e.getMessage());
        }
    }

    private void exportText(String fileName, String mime, String body) {
        pendingExport = body; pendingMime = mime;
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType(mime);
        i.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(i, REQ_EXPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == REQ_EXPORT) {
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                os.write(pendingExport.getBytes(StandardCharsets.UTF_8));
                toast("Exported locally.");
            } catch (Exception e) { toast("Export failed: " + e.getMessage()); }
        } else if (requestCode == REQ_IMPORT) {
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                String raw = readAll(is);
                new AlertDialog.Builder(this)
                        .setTitle("Import JSON backup?")
                        .setMessage("Choose merge or overwrite. Overwrite deletes existing local records/budgets first. Penguin is cute, but deletion is still deletion.")
                        .setPositiveButton("Merge", (d, w) -> importJson(raw, false))
                        .setNegativeButton("Overwrite", (d, w) -> importJson(raw, true))
                        .setNeutralButton("Cancel", null)
                        .show();
            } catch (Exception e) { toast("Import failed: " + e.getMessage()); }
        }
    }

    private String recordsCsv() {
        StringBuilder sb = new StringBuilder("date,type,category,amount,currency,note,createdAt,updatedAt\n");
        for (FinanceRecord r : dao.allRecords()) {
            sb.append(r.date).append(',').append(r.type).append(',').append(csv(r.category)).append(',').append(r.amount).append(',').append(csv(r.currency)).append(',').append(csv(r.note)).append(',').append(r.createdAt).append(',').append(r.updatedAt).append('\n');
        }
        return sb.toString();
    }

    private String reportCsv() {
        StringBuilder sb = new StringBuilder("period,income,expense,net\n");
        for (Summary s : lastFourWeeks()) sb.append("week ").append(s.label).append(',').append(s.income).append(',').append(s.expense).append(',').append(s.net).append('\n');
        for (Summary s : lastSixMonths()) sb.append(s.label).append(',').append(s.income).append(',').append(s.expense).append(',').append(s.net).append('\n');
        for (Summary s : lastThreeYears()) sb.append(s.label).append(',').append(s.income).append(',').append(s.expense).append(',').append(s.net).append('\n');
        return sb.toString();
    }

    private String buildAiPrompt() {
        Summary m = summaryForMonth(YearMonth.now());
        return "You are my external finance coach. Analyze this local Penguin Purse summary without needing bank login or APIs.\n" +
                "Name: " + (settings.userName == null || settings.userName.isEmpty() ? "User" : settings.userName) + "\n" +
                "Currency: " + settings.baseCurrency + "\nCurrent savings: " + money(settings.currentSaving) +
                "\nSaving target: " + money(settings.savingTarget) +
                "\nThis month income: " + money(m.income) + "\nThis month expense: " + money(m.expense) + "\nThis month net: " + money(m.net) +
                "\nRecent records CSV:\n" + recordsCsv() +
                "\nGive me a practical plan: spending cuts, saving targets, risk warnings, and one money-making action.";
    }

    private void pickDate(TextView out) {
        LocalDate now = LocalDate.now();
        new DatePickerDialog(this, (view, y, m, d) -> out.setText(LocalDate.of(y, m + 1, d).toString()), now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()).show();
    }

    private int chooseMascot(Summary month) {
        double budget = getOverallBudgetAmount(YearMonth.now());
        if (budget > 0 && month.expense > budget) return R.drawable.penguin_accounting;
        if (settings.currentSaving < settings.savingTarget * 0.15) return R.drawable.penguin_grass;
        if (month.net >= 0) return R.drawable.penguin_rich_cash;
        return R.drawable.penguin_shopping;
    }

    private int savingMascot() {
        if (settings.savingTarget > 0 && settings.currentSaving >= settings.savingTarget) return R.drawable.penguin_rich_cash;
        if (settings.currentSaving <= 0) return R.drawable.penguin_grass;
        if (settings.currentSaving < settings.savingTarget * 0.25) return R.drawable.penguin_poor;
        return R.drawable.penguin_money;
    }

    private String microcopy(Summary month) {
        String name = settings.userName == null || settings.userName.trim().isEmpty() ? "" : ", " + settings.userName.trim();
        double budget = getOverallBudgetAmount(YearMonth.now());
        if (budget > 0 && month.expense > budget) return "Iceberg ahead! Your budget is sinking faster than the Titanic.";
        if (month.net < 0) return "Your savings account called. It wants its dignity back.";
        String[] lines = new String[]{"Welcome back" + name + ".", "Hey, time to track your expense!", "Have you earned any income?", "Waddle you waiting for? Log expenses before money vanished."};
        return lines[(int) (System.currentTimeMillis() / 60000 % lines.length)];
    }

    private ImageView mascot(int image, int sizeDp) {
        ImageView img = new ImageView(this);
        img.setImageResource(image);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setPadding(dp(2), dp(2), dp(2), dp(2));
        img.setBackgroundColor(BLUE);
        img.setClipToOutline(false);
        img.setLayoutParams(new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp)));
        return img;
    }

    private LinearLayout miniCard(String title, String subtitle, int image) {
        LinearLayout c = card();
        c.setGravity(Gravity.CENTER);
        ImageView img = mascot(image, 82);
        c.addView(img);
        TextView t = text(title, 16, DEEP, true);
        t.setGravity(Gravity.CENTER);
        c.addView(t);
        TextView sub = text(subtitle, 14, ACCENT, true);
        sub.setGravity(Gravity.CENTER);
        c.addView(sub);
        return c;
    }

    private void addCategoryVisualButtons(LinearLayout form, Spinner category) {
        HorizontalScrollView wrap = new HorizontalScrollView(this);
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        String[] labels = {"🛍️ Shopping", "🍙 Food", "🧳 Travel", "🐧 Other"};
        int[] imgs = {R.drawable.penguin_shopping, R.drawable.penguin_eating, R.drawable.penguin_travel, R.drawable.penguin_happy};
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            LinearLayout tile = miniCard(label.replaceAll("^.. ", ""), label.substring(0, Math.min(2, label.length())), imgs[i]);
            tile.setOnClickListener(v -> selectSpinnerValue(category, label));
            strip.addView(tile, new LinearLayout.LayoutParams(dp(126), -2));
        }
        wrap.addView(strip);
        form.addView(wrap);
    }

    private void selectSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equals(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void addMascotHeader(String title, int image, String message) {
        LinearLayout box = card();
        box.setOrientation(LinearLayout.HORIZONTAL);
        ImageView img = new ImageView(this);
        img.setImageResource(image);
        img.setAdjustViewBounds(true);
        box.addView(img, new LinearLayout.LayoutParams(dp(92), dp(92)));
        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.addView(text(title, 22, DEEP, true));
        words.addView(text(message, 14, Color.rgb(74, 92, 104), false));
        box.addView(words, weight());
        content.addView(box);
    }

    private LinearLayout kpiRow(String title, Summary s) {
        LinearLayout c = card();
        c.addView(text(title, 18, DEEP, true));
        c.addView(text("Income " + settings.baseCurrency + money(s.income) + " · Expense " + settings.baseCurrency + money(s.expense) + " · Net " + settings.baseCurrency + money(s.net), 16, s.net >= 0 ? OK : WARN, true));
        return c;
    }

    private View progressCard(String title, double value, double target, int color) {
        LinearLayout c = card();
        c.addView(text(title, 17, DEEP, true));
        c.addView(text(settings.baseCurrency + money(value) + " / " + settings.baseCurrency + money(target), 15, Color.rgb(75, 94, 107), false));
        TextView bar = text(progressText(value, target), 20, color, true);
        c.addView(bar);
        if (target > 0 && value > target && color == WARN) c.addView(text("Overspending warning. Penguin judgment activated.", 14, WARN, true));
        return c;
    }

    private String progressText(double value, double target) {
        int total = 18;
        int filled = target <= 0 ? 0 : (int) Math.min(total, Math.round((value / target) * total));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < total; i++) sb.append(i < filled ? "■" : "□");
        return sb.toString();
    }

    private TextView section(String s) {
        TextView t = text(s, 19, DEEP, true);
        t.setPadding(dp(4), dp(18), dp(4), dp(8));
        return t;
    }

    private LinearLayout card() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(14), dp(12), dp(14), dp(12));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(CARD); bg.setCornerRadius(dp(18)); bg.setStroke(1, Color.rgb(218, 234, 242));
        l.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        l.setLayoutParams(lp);
        return l;
    }

    private Button pill(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextColor(DEEP);
        b.setTextSize(13);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(BLUE); bg.setCornerRadius(dp(18));
        b.setBackground(bg);
        b.setPadding(dp(10), dp(4), dp(10), dp(4));
        return b;
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setPadding(0, dp(3), 0, dp(3));
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return t;
    }

    private TextView label(String s) { return text(s, 13, Color.GRAY, false); }

    private EditText input(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(type);
        e.setSingleLine(false);
        return e;
    }

    private Spinner spinner(String[] items) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items));
        return s;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0, -2, 1);
    }

    private String startOf(YearMonth ym) { return ym.atDay(1).toString(); }
    private String monthOf(String date) { return date == null || date.length() < 7 ? "" : date.substring(0, 7); }
    private double parse(String s) { try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; } }
    private String money(double d) { return String.format(Locale.US, "%.2f", d); }
    private String csv(String s) { return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\""; }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    private String readAll(InputStream is) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append('\n');
        return sb.toString();
    }
}
