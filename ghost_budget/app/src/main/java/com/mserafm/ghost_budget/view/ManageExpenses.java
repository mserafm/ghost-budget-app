package com.mserafm.ghost_budget.view;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ghost_budget.R;
import com.mserafm.ghost_budget.model.Expense;
import com.mserafm.ghost_budget.viewmodel.BudgetViewModel;

public class ManageExpenses extends DialogFragment {

    final static String ARGSThisMonth = "thisMonth";
    final static String ARGSThisInstant = "thisInstant";
    final static String ARGSEmail = "email";
    final static String ARGSItemN = "itemN";
    final static String ARGSKey = "ARGSKey";
    final static String ARGSDate = "ARGSDate";
    final static String ARGSName = "ARGSName";
    final static String ARGSCost = "ARGSCost";
    final static String ARGSChart = "ARGSChart";
    final static String ARGSType = "ARGSType";
    final static String ARGSRepetition = "ARGSRepetition";
    Context context;
    Button btnClose;
    EditText etName;
    EditText etCost;
    RadioGroup radioGroup;
    RadioButton rb1;
    RadioButton rb2;
    RadioButton rb3;
    SwitchCompat switchSimulate;
    SwitchCompat switchRepeat;
    Button btnSaveClose;
    Button btnSaveMore;
    String selectedRbText;
    String stringSimulate;
    boolean booleanRepeat;
    BudgetViewModel budgetViewModel;
    int nItem;
    TextView tvInterval;
    NumberPicker numberPicker;
    Spinner spPeriod;


    public static ManageExpenses newInstance(String email, String instant, String month, int itemN) {
        ManageExpenses fragment = new ManageExpenses();
        Bundle bundle = new Bundle();
        bundle.putString(ARGSEmail, email);
        bundle.putString(ARGSThisMonth, month);
        bundle.putString(ARGSThisInstant, instant);
        bundle.putInt(ARGSItemN, itemN);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ManageExpenses newInstance(String email, String month, String key, String date, String name, double cost, String chart, String type, String repetition) {
        ManageExpenses fragment = new ManageExpenses();
        Bundle args = new Bundle();
        args.putString(ARGSEmail, email);
        args.putString(ARGSThisMonth, month);
        args.putString(ARGSKey, key);
        args.putString(ARGSDate, date);
        args.putString(ARGSName, name);
        args.putDouble(ARGSCost, cost);
        args.putString(ARGSChart, chart);
        args.putString(ARGSType, type);
        args.putString(ARGSRepetition, repetition);

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        budgetViewModel = new ViewModelProvider(getActivity()).get(BudgetViewModel.class);

        View view = inflater.inflate(R.layout.fragment_manage_expenses, container, false);
        btnClose = view.findViewById(R.id.close_button);
        etName = view.findViewById(R.id.et_name);
        etCost = view.findViewById(R.id.et_cost);
        radioGroup = view.findViewById(R.id.radio_group);
        rb1 = view.findViewById(R.id.rb_1);
        rb2 = view.findViewById(R.id.rb_2);
        rb3 = view.findViewById(R.id.rb_3);
        switchSimulate = view.findViewById(R.id.switch_simulate);
        switchRepeat = view.findViewById(R.id.switch_repeat);
        btnSaveClose = view.findViewById(R.id.btn_save_close);
        btnSaveMore = view.findViewById(R.id.btn_save_more);
        tvInterval = view.findViewById(R.id.tv_interval);
        numberPicker = view.findViewById(R.id.number_picker);
        spPeriod = view.findViewById(R.id.sp_period);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(12);


        if (getArguments().getString(ARGSKey) != null) {
            setModifyValues();
        } else {
            setDefaultValues();
            nItem = getArguments().getInt(ARGSItemN);
        }

        setOncheckedChangeListeners();
        setOnClickListeners();


        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void setOncheckedChangeListeners() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_1:
                        selectedRbText = "basic";
                        break;
                    case R.id.rb_2:
                        selectedRbText = "leisure";
                        break;
                    case R.id.rb_3:
                        selectedRbText = "invoice";
                        break;
                }

            }

        });

        switchSimulate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    stringSimulate = "s";
                } else {
                    stringSimulate = "r";
                }
            }
        });

        switchRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    booleanRepeat = true;
                    repeatActivated();
                } else {
                    booleanRepeat = false;
                    repeatInactive();
                }
            }
        });

    }

    public void setOnClickListeners() {
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnSaveClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allDone()) {
                    if (getArguments().getString(ARGSKey) != null) {
                        updateData();
                    } else {
                        saveData();
                    }

                    dismiss();
                } else {
                    makeToast(getString(R.string.no_empty_field));
                }

            }
        });

        btnSaveMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allDone()) {
                    saveData();
                    setDefaultValues();
                    nItem++;
                }
            }
        });
    }

    public void makeToast(String toastText) {
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
    }

    public boolean allDone() {
        if (!"".equals(etName.getText().toString().trim()) && !"".equals(etCost.getText().toString().trim())) {
            if (booleanRepeat && spPeriod.getSelectedItemPosition() != 0) {
                return true;
            } else if(!booleanRepeat){
                return true;
            } else{
                return false;
            }
        } else {
            return false;
        }
    }

    public void saveData() {

        String repeat = "";
        if (booleanRepeat) {
            repeat = String.valueOf(numberPicker.getValue()) + "-" + spPeriod.getSelectedItemPosition();
        } else {
            repeat = String.valueOf(booleanRepeat);
        }

        Expense expense = new Expense(getArguments().getString(ARGSThisInstant),
                etName.getText().toString().trim(),
                Double.parseDouble(etCost.getText().toString().trim()),
                stringSimulate,
                selectedRbText,
                repeat);

        budgetViewModel.addExpense(getArguments().getString(ARGSEmail), getArguments().getString(ARGSThisMonth), expense, nItem);


    }

    public void updateData() {

        String repeat = "";
        if (booleanRepeat) {
            repeat = String.valueOf(numberPicker.getValue()) + "-" + spPeriod.getSelectedItemPosition();
        } else {
            repeat = String.valueOf(booleanRepeat);
        }

        Expense oldExpense = new Expense(getArguments().getString(ARGSDate),
                getArguments().getString(ARGSName),
                getArguments().getDouble(ARGSCost),
                getArguments().getString(ARGSChart),
                getArguments().getString(ARGSType),
                getArguments().getString(ARGSRepetition));

        oldExpense.setKey(getArguments().getString(ARGSKey));

        if (stringSimulate.equals("r")){
            if (getArguments().getString(ARGSChart).equals("sr")){
                stringSimulate="sr";
            }
        }

        Expense updatedExpense = new Expense(getArguments().getString(ARGSDate),
                etName.getText().toString().trim(),
                Double.parseDouble(etCost.getText().toString().trim()),
                stringSimulate,
                selectedRbText,
                repeat);

        budgetViewModel.updateExpense(getArguments().getString(ARGSEmail), getArguments().getString(ARGSThisMonth), oldExpense, updatedExpense);

    }


    public void setDefaultValues() {
        etName.setText("");
        etCost.setText("");
        rb1.setChecked(true);
        selectedRbText = "basic";
        switchSimulate.setChecked(true);
        stringSimulate = "s";
        switchRepeat.setChecked(false);
        booleanRepeat = false;
        repeatInactive();
    }

    public void setModifyValues() {

        btnSaveMore.setVisibility(View.GONE);

        etName.setText(getArguments().getString(ARGSName));
        etCost.setText(String.valueOf(getArguments().getDouble(ARGSCost)));

        String aux = getArguments().getString(ARGSType);

        if (aux.equals("basic")) {
            rb1.setChecked(true);
            selectedRbText = "basic";
        } else if (aux.equals("leisure")) {
            rb2.setChecked(true);
            selectedRbText = "leisure";
        } else {
            rb3.setChecked(true);
            selectedRbText = "invoice";
        }

        stringSimulate = getArguments().getString(ARGSChart);

        if (stringSimulate.equals("s")) {
            switchSimulate.setChecked(true);
        } else {
            switchSimulate.setChecked(false);
        }

        if (getArguments().getString(ARGSRepetition).equals(String.valueOf(false))) {
            switchRepeat.setChecked(false);
            booleanRepeat=false;
            repeatInactive();
        } else {
            if(getArguments().getString(ARGSRepetition).substring(getArguments().getString(ARGSRepetition).length() - 1).equals("1"))
            switchRepeat.setChecked(true);
            booleanRepeat=true;
            if(getArguments().getString(ARGSRepetition).substring(getArguments().getString(ARGSRepetition).length() - 1).equals("1")){
                spPeriod.setSelection(1);
            }else{
                spPeriod.setSelection(2);
            }
            repeatActivated();
        }

    }

    public void repeatActivated() {
        tvInterval.setVisibility(View.VISIBLE);
        numberPicker.setVisibility(View.VISIBLE);
        spPeriod.setVisibility(View.VISIBLE);
    }

    public void repeatInactive() {
        tvInterval.setVisibility(View.GONE);
        numberPicker.setVisibility(View.GONE);
        spPeriod.setVisibility(View.GONE);
    }


}