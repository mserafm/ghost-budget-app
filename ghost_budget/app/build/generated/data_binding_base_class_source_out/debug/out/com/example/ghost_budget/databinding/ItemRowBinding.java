// Generated by view binder compiler. Do not edit!
package com.example.ghost_budget.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.ghost_budget.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemRowBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final TextView expenseCost;

  @NonNull
  public final TextView expenseName;

  @NonNull
  public final ImageView sealImage;

  @NonNull
  public final View viewStateExpense;

  private ItemRowBinding(@NonNull CardView rootView, @NonNull TextView expenseCost,
      @NonNull TextView expenseName, @NonNull ImageView sealImage, @NonNull View viewStateExpense) {
    this.rootView = rootView;
    this.expenseCost = expenseCost;
    this.expenseName = expenseName;
    this.sealImage = sealImage;
    this.viewStateExpense = viewStateExpense;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemRowBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemRowBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
      boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_row, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemRowBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.expense_cost;
      TextView expenseCost = ViewBindings.findChildViewById(rootView, id);
      if (expenseCost == null) {
        break missingId;
      }

      id = R.id.expense_name;
      TextView expenseName = ViewBindings.findChildViewById(rootView, id);
      if (expenseName == null) {
        break missingId;
      }

      id = R.id.seal_image;
      ImageView sealImage = ViewBindings.findChildViewById(rootView, id);
      if (sealImage == null) {
        break missingId;
      }

      id = R.id.view_state_expense;
      View viewStateExpense = ViewBindings.findChildViewById(rootView, id);
      if (viewStateExpense == null) {
        break missingId;
      }

      return new ItemRowBinding((CardView) rootView, expenseCost, expenseName, sealImage,
          viewStateExpense);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
