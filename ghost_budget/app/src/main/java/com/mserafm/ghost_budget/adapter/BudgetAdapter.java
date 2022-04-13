package com.mserafm.ghost_budget.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ghost_budget.R;
import com.mserafm.ghost_budget.model.Expense;

import java.util.ArrayList;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetHolder> {

    private List<Expense> expenseList = new ArrayList<>();
    private onItemClickListener clickListener;
    private onItemLongClickListener longClickListener;
    SharedPreferences sharedPreferences;

    public BudgetAdapter(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override
    public BudgetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new BudgetHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetHolder holder, int position) {
        Expense currentExpense = expenseList.get(position);

        holder.tvNameExpense.setText(currentExpense.getName());
        holder.tvCostExpense.setText(String.valueOf(currentExpense.getCost()));

        if (sharedPreferences.getBoolean("switch_pref_hand", false)) {
            holder.tvNameExpense.setGravity(Gravity.END);
            holder.tvCostExpense.setGravity(Gravity.END);
        }else {
            holder.tvNameExpense.setGravity(Gravity.START);
            holder.tvCostExpense.setGravity(Gravity.START);
        }

        if (currentExpense.getChart().equals("s")){
            holder.viewStateExpense.setBackgroundColor(Color.parseColor("#a53eee"));
        }else{
            holder.viewStateExpense.setBackgroundColor(Color.parseColor("#2da8eb"));
        }

        if (currentExpense.getType().equals("basic")){
            holder.sealImage.setImageResource(R.drawable.sello_corazon);
        }else if (currentExpense.getType().equals("leisure")){
            holder.sealImage.setImageResource(R.drawable.sello_feliz);
        }else {
            holder.sealImage.setImageResource(R.drawable.sello_casa);
        }

    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void setExpenseList(List<Expense> expenseList){
        this.expenseList = expenseList;
        notifyDataSetChanged();
    }

    public Expense getExpenseAt(int position){
        return expenseList.get(position);
    }

    class BudgetHolder extends RecyclerView.ViewHolder{

        private TextView tvNameExpense;
        private TextView tvCostExpense;
        private View viewStateExpense;
        private ImageView sealImage;

        public BudgetHolder(@NonNull View itemView) {
            super(itemView);
            tvNameExpense = itemView.findViewById(R.id.expense_name);
            tvCostExpense = itemView.findViewById(R.id.expense_cost);
            viewStateExpense = itemView.findViewById(R.id.view_state_expense);
            sealImage = itemView.findViewById(R.id.seal_image);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(clickListener != null && position != RecyclerView.NO_POSITION){
                        clickListener.onItemCLick(expenseList.get(position));
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (longClickListener != null && position != RecyclerView.NO_POSITION){
                        longClickListener.onItemLongCLick(expenseList.get(position));
                    }
                    return true;
                }
            });
        }
    }


    public interface onItemClickListener {
        void onItemCLick(Expense expense);
    }

    public interface onItemLongClickListener{
        void onItemLongCLick(Expense expense);
    }

    public void setOnItemClickListener(onItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setOnItemLongClickListener(onItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

}
