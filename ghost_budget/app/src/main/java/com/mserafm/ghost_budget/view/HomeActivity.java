package com.mserafm.ghost_budget.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;
import androidx.transition.Transition;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.ghost_budget.R;
import com.example.ghost_budget.databinding.ActivityHomeBinding;
import com.mserafm.ghost_budget.adapter.BudgetAdapter;
import com.mserafm.ghost_budget.model.Expense;
import com.mserafm.ghost_budget.preferences.PreferencesActivity;
import com.mserafm.ghost_budget.viewmodel.BudgetViewModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private String email;

    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private LineChart mpLineChart;
    private PieChart mpPieChart;
    private PieChart innerChart;
    private BarChart mpBarChart;
    private FloatingActionButton buttonAddExpense;
    private ArrayList<Expense> expensesChartList;
    private String selectedChart = "sr";
    private final int PIE_CHART = 0;
    private final int LINEAR_CHART = 1;
    private final int BAR_CHART = 2;
    private int chartInUse = 0;
    private int fID = 0;
    private float limit = 0;
    private BudgetAdapter adapter;
    private View inflatedView;


    private RelativeLayout chartsLayout;

    private ShapeableImageView btnChartSettings, btnLineChart, btnPieChart, btnBarChart, btnSimulateRealChart, btnRealChart, btnSimulateChart;

    private BudgetViewModel budgetViewModel;

    private boolean chartSettingsOpen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setTitle("GhostBudget");
        inflatedView = getLayoutInflater().inflate(R.layout.fragment_manage_expenses, null);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        chartsLayout = findViewById(R.id.charts_layout);
        btnChartSettings = findViewById(R.id.btn_chart_options);
        btnLineChart = findViewById(R.id.btn_line_chart);
        btnPieChart = findViewById(R.id.btn_pie_chart);
        btnBarChart = findViewById(R.id.btn_bar_chart);
        btnSimulateRealChart = findViewById(R.id.btn_simulated_real);
        btnRealChart = findViewById(R.id.btn_real);
        btnSimulateChart = findViewById(R.id.btn_simulated);
        expensesChartList = new ArrayList<>();
        mpLineChart = findViewById(R.id.line_chart);
        mpPieChart = findViewById(R.id.pie_chart);
        mpBarChart = findViewById(R.id.bar_chart);
        fID = findUnusedId();

        Bundle bundle = getIntent().getExtras();
        String auth = bundle.getString("auth");
        email = bundle.getString("email");

        SharedPreferences.Editor sharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
        sharedPreferences.putString("auth", auth);
        sharedPreferences.putString("email", email);
        sharedPreferences.apply();

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setHasFixedSize(true);

        adapter = new BudgetAdapter(getApplicationContext());
        binding.recycler.setAdapter(adapter);

        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        budgetViewModel.getMonthlyExpensesData(email, getThisMonth()).observe(this, new Observer<List<Expense>>() {
            @Override
            public void onChanged(List<Expense> expenses) {
                adapter.setExpenseList(expenses);
                expensesChartList.clear();
                expensesChartList.addAll(expenses);
                setUpChartGeneral(chartInUse);
            }
        });

        budgetViewModel.getLimit(email).observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                limit = aFloat;
                mpLineChart.getAxisLeft().removeAllLimitLines();
                if (limit != 0) {
                    LimitLine ll = new LimitLine(limit);
                    mpLineChart.getAxisLeft().addLimitLine(ll);
                }
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                Expense deleteExpense = adapter.getExpenseAt(viewHolder.getAdapterPosition());

                budgetViewModel.deleteExpense(email, getThisMonth(), deleteExpense);
                adapter.notifyDataSetChanged();

                Snackbar.make(binding.recycler, getString(R.string.data_deleted), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        budgetViewModel.updateExpense(email, getThisMonth(), deleteExpense, deleteExpense);
                        adapter.notifyDataSetChanged();
                    }
                }).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(Color.RED)
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(binding.recycler);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                Expense updateExpense = adapter.getExpenseAt(viewHolder.getAdapterPosition());
                if (updateExpense.getChart().equals("s")) {
                    Expense updatedExpense = updateExpense;
                    updatedExpense.setChart("sr");

                    budgetViewModel.updateExpense(email, getThisMonth(), updateExpense, updatedExpense);
                }

                adapter.notifyItemChanged(viewHolder.getAdapterPosition());

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightBackgroundColor(Color.parseColor("#2da8eb"))
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {

                Expense updateExpense = adapter.getExpenseAt(viewHolder.getAdapterPosition());
                if (!updateExpense.getChart().equals("s")) {
                    return 0.1f;
                } else {
                    return super.getSwipeThreshold(viewHolder);
                }
            }
        }).attachToRecyclerView(binding.recycler);

        adapter.setOnItemLongClickListener(new BudgetAdapter.onItemLongClickListener() {
            @Override
            public void onItemLongCLick(Expense expense) {
                ManageExpenses manage_expenses = ManageExpenses.newInstance(email,
                        getThisMonth(),
                        expense.getKey(),
                        expense.getDate(),
                        expense.getName(),
                        expense.getCost(),
                        expense.getChart(),
                        expense.getType(),
                        expense.getRepetition());
                manage_expenses.show(getSupportFragmentManager(), "manage_expenses");
            }
        });


        buttonAddExpense = (FloatingActionButton) findViewById(R.id.fbtn_add_expense);
        buttonAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ManageExpenses manage_expenses = ManageExpenses.newInstance(email, getThisInstant(), getThisMonth(), budgetViewModel.getItemCount().getValue());
                manage_expenses.show(getSupportFragmentManager(), "manage_expenses");

            }
        });

        setUpChartButtons();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.item_logout:

                SharedPreferences.Editor sharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit();
                sharedPreferences.clear();
                sharedPreferences.apply();

                FirebaseAuth.getInstance().signOut();
                Intent logOutIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(logOutIntent);
                return true;

            case R.id.item_settings:
                Intent preferencesIntent = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(preferencesIntent);
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        applyPrefs();
        adapter.notifyDataSetChanged();
    }

    private void applyPrefs() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String stringAux = sharedPreferences.getString("et_pref_limit", "0").trim();

        float aux;

        if (stringAux.equals("0") || stringAux.equals("")) {
            aux = 0;
        } else {
            aux = Float.parseFloat(stringAux);
        }

        if (aux != limit) {
            budgetViewModel.updateLimit(email, aux);
        }

        RelativeLayout.LayoutParams btnChartSettingsLayoutParams = (RelativeLayout.LayoutParams) btnChartSettings.getLayoutParams();
        RelativeLayout.LayoutParams btnLineChartLayoutParams = (RelativeLayout.LayoutParams) btnLineChart.getLayoutParams();
        RelativeLayout.LayoutParams btnPieChartLayoutParams = (RelativeLayout.LayoutParams) btnPieChart.getLayoutParams();
        RelativeLayout.LayoutParams btnBarChartLayoutParams = (RelativeLayout.LayoutParams) btnBarChart.getLayoutParams();
        RelativeLayout.LayoutParams btnSimulateChartLayoutParams = (RelativeLayout.LayoutParams) btnSimulateChart.getLayoutParams();
        RelativeLayout.LayoutParams btnRealChartLayoutParams = (RelativeLayout.LayoutParams) btnRealChart.getLayoutParams();
        RelativeLayout.LayoutParams btnSimulateRealChartLayoutParams = (RelativeLayout.LayoutParams) btnSimulateRealChart.getLayoutParams();
        CoordinatorLayout.LayoutParams btnAddLayoutParams = (CoordinatorLayout.LayoutParams) buttonAddExpense.getLayoutParams();


        if (sharedPreferences.getBoolean("switch_pref_hand", false)) {
            btnChartSettingsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            btnChartSettingsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            btnChartSettings.setLayoutParams(btnChartSettingsLayoutParams);

            btnLineChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            btnLineChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            btnLineChart.setLayoutParams(btnLineChartLayoutParams);

            btnPieChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            btnPieChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            btnPieChart.setLayoutParams(btnPieChartLayoutParams);

            btnBarChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            btnBarChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            btnBarChart.setLayoutParams(btnBarChartLayoutParams);

            btnRealChartLayoutParams.addRule(RelativeLayout.START_OF, 0);
            btnRealChartLayoutParams.addRule(RelativeLayout.END_OF, R.id.btn_simulated);
            btnRealChart.setLayoutParams(btnRealChartLayoutParams);

            btnSimulateChartLayoutParams.addRule(RelativeLayout.START_OF, 0);
            btnSimulateChartLayoutParams.addRule(RelativeLayout.END_OF, R.id.btn_chart_options);
            btnSimulateChart.setLayoutParams(btnSimulateChartLayoutParams);

            btnSimulateRealChartLayoutParams.addRule(RelativeLayout.START_OF, 0);
            btnSimulateRealChartLayoutParams.addRule(RelativeLayout.END_OF, R.id.btn_real);
            btnSimulateRealChart.setLayoutParams(btnSimulateRealChartLayoutParams);

            btnAddLayoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
            buttonAddExpense.setLayoutParams(btnAddLayoutParams);


        } else {

            btnChartSettingsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            btnChartSettingsLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            btnChartSettings.setLayoutParams(btnChartSettingsLayoutParams);

            btnLineChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            btnLineChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            btnLineChart.setLayoutParams(btnLineChartLayoutParams);

            btnPieChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            btnPieChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            btnPieChart.setLayoutParams(btnPieChartLayoutParams);

            btnBarChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            btnBarChartLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
            btnBarChart.setLayoutParams(btnBarChartLayoutParams);

            btnSimulateChartLayoutParams.addRule(RelativeLayout.END_OF, 0);
            btnSimulateChartLayoutParams.addRule(RelativeLayout.START_OF, R.id.btn_real);
            btnSimulateChart.setLayoutParams(btnSimulateChartLayoutParams);

            btnRealChartLayoutParams.addRule(RelativeLayout.END_OF, 0);
            btnRealChartLayoutParams.addRule(RelativeLayout.START_OF, R.id.btn_simulated_real);
            btnRealChart.setLayoutParams(btnRealChartLayoutParams);

            btnSimulateRealChartLayoutParams.addRule(RelativeLayout.END_OF, 0);
            btnSimulateRealChartLayoutParams.addRule(RelativeLayout.START_OF, R.id.btn_chart_options);
            btnSimulateRealChart.setLayoutParams(btnSimulateRealChartLayoutParams);

            btnAddLayoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            buttonAddExpense.setLayoutParams(btnAddLayoutParams);

        }

    }

    public String getThisMonth() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        return simpleDateFormat.format(new Date());
    }

    public String getThisInstant() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-HH-mm-ss");
        return simpleDateFormat.format(new Date());
    }

    public void setUpChartButtons() {

        btnChartSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


                int direction;

                if (sharedPreferences.getBoolean("switch_pref_hand", false)) {
                    direction = Gravity.START;
                } else {
                    direction = Gravity.END;
                }
                Transition transition = new Fade();
                transition.setDuration(200);
                transition.addTarget(R.id.btn_line_chart);
                transition.addTarget(R.id.btn_pie_chart);
                transition.addTarget(R.id.btn_bar_chart);
                transition.addTarget(R.id.btn_real);
                transition.addTarget(R.id.btn_simulated);
                transition.addTarget(R.id.btn_simulated_real);

                TransitionManager.beginDelayedTransition(chartsLayout, transition);

                if (chartSettingsOpen) {

                    btnLineChart.setVisibility(View.VISIBLE);
                    btnPieChart.setVisibility(View.VISIBLE);
                    btnBarChart.setVisibility(View.VISIBLE);
                    btnSimulateRealChart.setVisibility(View.VISIBLE);
                    btnRealChart.setVisibility(View.VISIBLE);
                    btnSimulateChart.setVisibility(View.VISIBLE);

                    chartSettingsOpen = false;

                } else {

                    btnLineChart.setVisibility(View.GONE);
                    btnPieChart.setVisibility(View.GONE);
                    btnBarChart.setVisibility(View.GONE);
                    btnSimulateRealChart.setVisibility(View.GONE);
                    btnRealChart.setVisibility(View.GONE);
                    btnSimulateChart.setVisibility(View.GONE);

                    chartSettingsOpen = true;
                }
            }
        });


        btnLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mpPieChart.getVisibility() == View.VISIBLE) {
                    mpPieChart.setVisibility(View.GONE);
                } else if (mpBarChart.getVisibility() == View.VISIBLE) {
                    mpBarChart.setVisibility(View.GONE);
                }

                mpLineChart.setVisibility(View.VISIBLE);
                chartInUse = LINEAR_CHART;
                setUpChartGeneral(chartInUse);

            }
        });

        btnPieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mpLineChart.getVisibility() == View.VISIBLE) {
                    mpLineChart.setVisibility(View.GONE);
                } else if (mpBarChart.getVisibility() == View.VISIBLE) {
                    mpBarChart.setVisibility(View.GONE);
                }

                mpPieChart.setVisibility(View.VISIBLE);
                chartInUse = PIE_CHART;
                setUpChartGeneral(chartInUse);
            }
        });

        btnBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mpPieChart.getVisibility() == View.VISIBLE) {
                    mpPieChart.setVisibility(View.GONE);
                } else if (mpLineChart.getVisibility() == View.VISIBLE) {
                    mpLineChart.setVisibility(View.GONE);
                }

                mpBarChart.setVisibility(View.VISIBLE);
                chartInUse = BAR_CHART;
                setUpChartGeneral(chartInUse);
            }
        });

        btnSimulateChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedChart = "s";

                setUpChartGeneral(chartInUse);
            }
        });

        btnRealChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedChart = "r";

                setUpChartGeneral(chartInUse);
            }
        });

        btnSimulateRealChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedChart = "sr";

                setUpChartGeneral(chartInUse);

            }
        });


    }

    public void setUpChartGeneral(int chartType) {

        if (innerChart != null) {
            chartsLayout.removeView(findViewById(fID));
        }

        switch (chartType) {
            case PIE_CHART:
                setUpPieChart();
                break;
            case LINEAR_CHART:
                setUpLinearChart();
                break;
            case BAR_CHART:
                setUpBarChart();
                break;
        }
    }

    public void setUpLinearChart() {

        if (mpLineChart.getData() != null)
            mpLineChart.getData().clearValues();

        mpLineChart.clear();

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        if (selectedChart.equals("sr")) {
            LineDataSet lineDataSetS = new LineDataSet(fillDataValuesLineChart("s"), "Data Set 1");
            LineDataSet lineDataSetR = new LineDataSet(fillDataValuesLineChart("r"), "Data Set 2");

            lineDataSetS.setColor(Color.parseColor("#a53eee"));
            lineDataSetR.setColor(Color.parseColor("#2da8eb"));

            lineDataSetS.setLineWidth(3f);
            lineDataSetR.setLineWidth(3f);
            lineDataSetS.setValueTextColor(getResources().getColor(R.color.black));
            lineDataSetR.setValueTextColor(getResources().getColor(R.color.black));
            lineDataSetS.setValueTextSize(10f);
            lineDataSetR.setValueTextSize(10f);

            dataSets.add(lineDataSetS);
            dataSets.add(lineDataSetR);
        } else {
            LineDataSet lineDataSetSelected = new LineDataSet(fillDataValuesLineChart(selectedChart), "Data Set 1");
            lineDataSetSelected.setLineWidth(3f);
            lineDataSetSelected.setValueTextColor(getResources().getColor(R.color.black));
            lineDataSetSelected.setValueTextSize(10f);

            if (selectedChart.equals("s")) {
                lineDataSetSelected.setColor(Color.parseColor("#a53eee"));
            } else {
                lineDataSetSelected.setColor(Color.parseColor("#2da8eb"));
            }

            dataSets.add(lineDataSetSelected);
        }

        LineData data = new LineData(dataSets);
        mpLineChart.setData(data);

        if (limit != 0) {
            LimitLine ll = new LimitLine(limit);
            mpLineChart.getAxisLeft().addLimitLine(ll);
        }

        mpLineChart.setDrawBorders(true);
        mpLineChart.setBorderColor(getResources().getColor(R.color.black));
        mpLineChart.setDrawGridBackground(false);

        mpLineChart.getDescription().setEnabled(false);
        mpLineChart.getLegend().setEnabled(false);

        mpLineChart.getXAxis().setAxisMaximum((float) maxChartNumber(getThisMonth()));
        mpLineChart.getXAxis().setAxisMinimum(1);
        mpLineChart.getAxisLeft().setDrawGridLines(false);
        mpLineChart.getAxisLeft().setDrawLabels(false);
        mpLineChart.getAxisLeft().setDrawAxisLine(false);
        mpLineChart.getXAxis().setTextSize(10f);
        mpLineChart.getXAxis().setTextColor(getResources().getColor(R.color.black));

        mpLineChart.getXAxis().setDrawGridLines(false);
        mpLineChart.getXAxis().setDrawAxisLine(false);

        mpLineChart.getAxisRight().setAxisMinimum(0);
        mpLineChart.getAxisLeft().setAxisMinimum(0);

        mpLineChart.getAxisRight().setDrawGridLines(false);
        mpLineChart.getAxisRight().setDrawLabels(false);
        mpLineChart.getAxisRight().setDrawAxisLine(false);
        mpLineChart.animateX(200, Easing.Linear);


        mpLineChart.invalidate();
    }

    private ArrayList<Entry> fillDataValuesLineChart(String singlechart) {
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        double sum = 0;
        ArrayList<Expense> singleChartExpenses = new ArrayList<>();

        boolean dayOneExists = false;

        for (Expense expense : expensesChartList) {
            if (expense.getChart().equals(singlechart) || expense.getChart().equals("sr")) {
                singleChartExpenses.add(expense);
            }
            if (Integer.parseInt(expense.getDate().substring(0, 2)) == 1) {
                dayOneExists = true;
            }

        }

        if (!dayOneExists) {
            dataVals.add(new Entry(1, 0));
        }

        for (int i = singleChartExpenses.size() - 1; i >= 0; i--) {

            sum = sum + singleChartExpenses.get(i).getCost();

            if (i > 0) {
                if (Integer.parseInt(singleChartExpenses.get(i).getDate().substring(0, 2)) != Integer.parseInt(singleChartExpenses.get(i - 1).getDate().substring(0, 2))) {
                    dataVals.add(new Entry(Integer.parseInt(singleChartExpenses.get(i).getDate().substring(0, 2)), (float) sum));

                }
            } else {
                dataVals.add(new Entry(Integer.parseInt(singleChartExpenses.get(i).getDate().substring(0, 2)), (float) sum));
            }

        }

        Collections.sort(dataVals, new EntryXComparator());

        return dataVals;
    }


    public void setUpBarChart() {

        if (!mpBarChart.isEmpty()) {
            mpBarChart.clearValues();
        }
        mpBarChart.clear();

        String days[] = new String[maxChartNumber(getThisMonth()) + 1];

        for (int i = 0; i <= maxChartNumber(getThisMonth()); i++) {
            days[i] = String.valueOf(i);
        }

        XAxis xAxis = mpBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setTextColor(getResources().getColor(R.color.black));

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        mpBarChart.getAxisLeft().setDrawGridLines(false);
        mpBarChart.getAxisRight().setDrawGridLines(false);
        mpBarChart.getAxisLeft().setDrawLabels(false);
        mpBarChart.getAxisRight().setDrawLabels(false);

        mpBarChart.getDescription().setEnabled(false);
        mpBarChart.setDragEnabled(true);



        xAxis.setAxisMaximum(maxChartNumber(getThisMonth()) + 1);

        if (selectedChart.equals("sr")) {
            xAxis.setAxisMinimum(1);
            xAxis.setCenterAxisLabels(true);
            List<BarEntry> entriesGroup1 = fillDataValuesBarChart("s");
            List<BarEntry> entriesGroup2 = fillDataValuesBarChart("r");

            BarDataSet set1 = new BarDataSet(entriesGroup1, "Group 1");
            set1.setColors(Color.parseColor("#a53eee"));
            set1.setValueTextColor(getResources().getColor(R.color.black));
            set1.setValueTextSize(10f);
            BarDataSet set2 = new BarDataSet(entriesGroup2, "Group 2");
            set2.setColors(Color.parseColor("#2da8eb"));
            set2.setValueTextSize(10f);
            set2.setValueTextColor(getResources().getColor(R.color.black));

            BarData data = new BarData(set1, set2);
            mpBarChart.setData(data);

            float barSpace = 0.05f;
            float groupSpace = 0.3f;

            data.setBarWidth(0.3f);

            mpBarChart.groupBars(1, groupSpace, barSpace);
        } else {
            xAxis.setCenterAxisLabels(false);
            xAxis.setAxisMinimum(0.5f);
            xAxis.setAxisMaximum(maxChartNumber(getThisMonth()) + 0.5f);
            List<BarEntry> entriesGroup1 = fillDataValuesBarChart(selectedChart);
            BarDataSet set1 = new BarDataSet(entriesGroup1, "Group 1");
            set1.setValueTextColor(getResources().getColor(R.color.black));
            set1.setValueTextSize(10f);
            int color;

            if (selectedChart.equals("s")) {
                color = Color.parseColor("#a53eee");
            } else {
                color = Color.parseColor("#2da8eb");
            }

            set1.setColors(color);
            BarData data = new BarData(set1);
            mpBarChart.setData(data);
        }

        mpBarChart.setVisibleXRangeMaximum(3);
        mpBarChart.getLegend().setEnabled(false);
        mpBarChart.animateY(500, Easing.Linear);

        mpBarChart.invalidate();
    }

    private ArrayList<BarEntry> fillDataValuesBarChart(String singlechart) {
        ArrayList<BarEntry> dataVals = new ArrayList<>();
        double sum = 0;
        ArrayList<Expense> singleChartExpenses = new ArrayList<>();

        ArrayList<Integer> daysUsed = new ArrayList<>();

        for (Expense expense : expensesChartList) {
            if (expense.getChart().equals(singlechart) || expense.getChart().equals("sr")) {
                singleChartExpenses.add(expense);

                if (!daysUsed.contains(Integer.parseInt(expense.getDate().substring(0, 2)))) {
                    daysUsed.add(Integer.parseInt(expense.getDate().substring(0, 2)));
                }

            }
        }

        for (int i: daysUsed){
            Log.e("DAYSUSED", String.valueOf(i));
        }

        for (int i = 1; i <= maxChartNumber(getThisMonth()); i++) {
            if (!daysUsed.contains(i)) {
                dataVals.add(new BarEntry(i, 0));
            }

        }

        for (int i = singleChartExpenses.size() - 1; i >= 0; i--) {

            sum = sum + singleChartExpenses.get(i).getCost();


            if (i > 0) {
                if (Integer.parseInt(singleChartExpenses.get(i).getDate().substring(0, 2)) != Integer.parseInt(singleChartExpenses.get(i - 1).getDate().substring(0, 2))) {
                    dataVals.add(new BarEntry(Integer.parseInt(singleChartExpenses.get(i).getDate().substring(0, 2)), (float) sum));
                    sum = 0;

                }
            } else {
                dataVals.add(new BarEntry(Integer.parseInt(singleChartExpenses.get(i).getDate().substring(0, 2)), (float) sum));
            }

        }

        Collections.sort(dataVals, new EntryXComparator());

        return dataVals;
    }

    public int maxChartNumber(String date) {
        int auxMonth = Integer.parseInt(getThisMonth().substring(getThisMonth().length() - 2));

        int daysCount = 0;

        switch (auxMonth) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                daysCount = 31;
                break;
            case 2:
                if (isLeap(Integer.parseInt(getThisMonth().substring(0, 4)))) {
                    daysCount = 29;
                } else {
                    daysCount = 28;
                }
                break;
            case 4:
            case 6:
            case 9:
            case 11:
            case 13:
                daysCount = 30;
                break;
        }

        return daysCount;
    }

    public boolean isLeap(int year) {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }

    public PieChart createPieChart(String singlechart) {

        PieChart outerPieChart = new PieChart(this);

        PieDataSet pieDataSet = new PieDataSet(fillDataValuesPieChart(singlechart), "Data");

        int color1 = Color.parseColor("#70ccff");
        int color2 = Color.parseColor("#2da8eb");
        int color3 = Color.parseColor("#0865ce");

        pieDataSet.setColors(color1, color2, color3);
        pieDataSet.setSliceSpace(5);
        pieDataSet.setDrawValues(true);
        pieDataSet.setValueTextColor(Color.parseColor("#ffffff"));
        pieDataSet.setValueTextSize(10f);

        PieData pieData = new PieData();
        pieData.setDataSet(pieDataSet);
        outerPieChart.setData(pieData);

        outerPieChart.getLegend().setEnabled(false);
        outerPieChart.getDescription().setEnabled(false);
        outerPieChart.setHoleColor(getResources().getColor(R.color.white));
        outerPieChart.animateX(500, Easing.EaseInOutQuad);

        return outerPieChart;

    }

    public void setUpPieChart() {

        if (!mpPieChart.isEmpty()) {
            mpPieChart.clearValues();
        }
        mpPieChart.clear();

        int color1;
        int color2;
        int color3;

        if (selectedChart.equals("sr")) {
            PieDataSet pieDataSet = new PieDataSet(fillDataValuesPieChart("s"), "Data");

            color1 = Color.parseColor("#cd79f5");
            color2 = Color.parseColor("#a53eee");
            color3 = Color.parseColor("#7708ce");

            pieDataSet.setColors(color1, color2, color3);
            pieDataSet.setSliceSpace(5);
            pieDataSet.setDrawValues(true);
            pieDataSet.setValueTextColor(Color.parseColor("#ffffff"));
            pieDataSet.setValueTextSize(10f);

            PieData pieData = new PieData();
            pieData.setDataSet(pieDataSet);
            mpPieChart.setData(pieData);

            mpPieChart.getLegend().setEnabled(false);
            mpPieChart.getDescription().setEnabled(false);

            addInnerPieChart();

        } else {
            PieDataSet pieDataSet = new PieDataSet(fillDataValuesPieChart(selectedChart), "Data");

            if (selectedChart.equals("r")) {
                color1 = Color.parseColor("#70ccff");
                color2 = Color.parseColor("#2da8eb");
                color3 = Color.parseColor("#0865ce");
            } else {
                color1 = Color.parseColor("#cd79f5");
                color2 = Color.parseColor("#a53eee");
                color3 = Color.parseColor("#7708ce");
            }

            pieDataSet.setColors(color1, color2, color3);
            pieDataSet.setSliceSpace(5);
            pieDataSet.setDrawValues(true);
            pieDataSet.setValueTextColor(Color.parseColor("#ffffff"));
            pieDataSet.setValueTextSize(10f);

            PieData pieData = new PieData();

            pieData.setDataSet(pieDataSet);
            mpPieChart.setData(pieData);

            mpPieChart.getLegend().setEnabled(false);
            mpPieChart.getDescription().setEnabled(false);
        }

        mpPieChart.setHoleColor(getResources().getColor(R.color.white));
        mpPieChart.setEntryLabelColor(Color.parseColor("#ffffff"));
        mpPieChart.animateX(500, Easing.EaseInOutQuad);

    }

    public void addInnerPieChart() {
        mpPieChart.setHoleRadius(60f);
        mpPieChart.setTransparentCircleRadius(60f);
        mpPieChart.invalidate();

        int width = (int) (mpPieChart.getWidth() * 0.6f);
        int height = (int) (mpPieChart.getHeight() * 0.6f);

        innerChart = createPieChart("r");
        chartsLayout.addView(innerChart, 3);

        innerChart.setId(fID);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) innerChart.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.width = width;
        params.height = height;
        innerChart.setLayoutParams(params);

    }

    public int findUnusedId() {
        int i = 0;
        while (findViewById(++i) != null) ;
        return i;
    }

    private ArrayList<PieEntry> fillDataValuesPieChart(String singlechart) {

        ArrayList<PieEntry> dataVals = new ArrayList<>();
        double sum1 = 0;
        double sum2 = 0;
        double sum3 = 0;


        for (Expense expense : expensesChartList) {
            if (expense.getChart().equals(singlechart) || expense.getChart().equals("sr")) {

                if (expense.getType().equals("basic")) {
                    sum1 = sum1 + expense.getCost();
                } else if (expense.getType().equals("leisure")) {
                    sum2 = sum2 + expense.getCost();
                } else {
                    sum3 = sum3 + expense.getCost();
                }

            }
        }

        dataVals.add(new PieEntry((float) sum1, getResources().getString(R.string.option_1)));
        dataVals.add(new PieEntry((float) sum2, getResources().getString(R.string.option_2)));
        dataVals.add(new PieEntry((float) sum3, getResources().getString(R.string.option_3) + "s"));

        return dataVals;

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}