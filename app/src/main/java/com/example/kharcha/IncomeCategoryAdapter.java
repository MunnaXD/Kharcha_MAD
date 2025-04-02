package com.example.kharcha;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IncomeCategoryAdapter extends RecyclerView.Adapter<IncomeCategoryAdapter.IncomeCategoryViewHolder> {
    private Context context;
    private List<IncomeCategoryModel> categories;
    private double total;

    // Colors for category bars
    private final int[] CATEGORY_COLORS = {
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#8BC34A"), // Light Green
            Color.parseColor("#CDDC39"), // Lime
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#2196F3")  // Blue
    };

    public IncomeCategoryAdapter(Context context, List<IncomeCategoryModel> categories) {
        this.context = context;
        this.categories = categories;
        calculateTotal();
    }

    private void calculateTotal() {
        total = 0;
        for (IncomeCategoryModel category : categories) {
            total += category.getAmount();
        }
    }

    @NonNull
    @Override
    public IncomeCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new IncomeCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeCategoryViewHolder holder, int position) {
        IncomeCategoryModel category = categories.get(position);
        holder.tvCategoryName.setText(category.getName());
        holder.tvAmount.setText("₹" + String.format("%.2f", category.getAmount()));

        // 🔹 Ensure `total` is recalculated before using it
        double totalAmount = 0;
        for (IncomeCategoryModel cat : categories) {
            totalAmount += cat.getAmount();
        }

        // 🔹 Compute percentage safely
        double percentage = (totalAmount > 0) ? ((category.getAmount() / totalAmount) * 100) : 0;
        holder.tvPercentage.setText(String.format("%.1f%%", percentage));

        // 🔹 Update progress bar
        holder.progressBar.setProgress((int) percentage);
        holder.progressBar.setProgressTintList(
                android.content.res.ColorStateList.valueOf(
                        CATEGORY_COLORS[position % CATEGORY_COLORS.length]));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class IncomeCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvAmount, tvPercentage;
        ProgressBar progressBar;

        public IncomeCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}