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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private Context context;
    private List<CategoryModel> categories;
    private double total;

    // Colors for category bars
    private final int[] CATEGORY_COLORS = {
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#FFEB3B")  // Yellow
    };

    public CategoryAdapter(Context context, List<CategoryModel> categories) {
        this.context = context;
        this.categories = categories;
        calculateTotal();
    }

    private void calculateTotal() {
        total = 0;
        for (CategoryModel category : categories) {
            total += category.getAmount();
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categories.get(position);
        holder.tvCategoryName.setText(category.getName());
        holder.tvAmount.setText("₹" + String.format("%.2f", category.getAmount()));

        double percentage = category.getPercentage(total);
        holder.tvPercentage.setText(String.format("%.1f%%", percentage));

        holder.progressBar.setProgress((int) percentage);
        holder.progressBar.setProgressTintList(
                android.content.res.ColorStateList.valueOf(
                        CATEGORY_COLORS[position % CATEGORY_COLORS.length]));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvAmount, tvPercentage;
        ProgressBar progressBar;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}