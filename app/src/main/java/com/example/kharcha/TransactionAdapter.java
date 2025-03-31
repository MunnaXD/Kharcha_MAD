package com.example.kharcha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<TransactionModel> transactions;
    private Context context;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public TransactionAdapter(Context context, List<TransactionModel> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TransactionModel transaction = transactions.get(position);
        holder.tvTitle.setText(transaction.getTitle());
        holder.tvAmount.setText(String.format("₹%.2f", transaction.getAmount()));

        // ✅ Ensure Date Formatting Works
        if (transaction.getDate() != null) {
            holder.tvDate.setText(sdf.format(transaction.getDate()));
        } else {
            holder.tvDate.setText("Unknown Date"); // Fallback if date is null
        }

        // Set text color based on transaction type
        if ("Income".equals(transaction.getType())) {
            holder.tvAmount.setTextColor(0xFF4CAF50); // Green
        } else {
            holder.tvAmount.setTextColor(0xFFF44336); // Red
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateData(List<TransactionModel> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvDate;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvAmount = view.findViewById(R.id.tvAmount);
            tvDate = view.findViewById(R.id.tvDate);
        }
    }
}
