package com.example.cafemanagementapp.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.Models.Expense;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> mExpenseList;

    public ExpenseAdapter(List<Expense> unitList) {
        mExpenseList = unitList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense_t = mExpenseList.get(position);
        if(expense_t == null){
            return;
        }
        holder.bind(expense_t, position);
    }

    @Override
    public int getItemCount() {
        if(mExpenseList != null){
            return  mExpenseList.size();
        }
        return 0;
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private TextView expenseItemNo;
        private TextView expenseItemName;
        private TextView expenseItemQuantity;
        private TextView expenseItemTotalPrice;
        public ExpenseViewHolder(View itemView) {
            super(itemView);
            expenseItemNo = itemView.findViewById(R.id.expenseItemNo);
            expenseItemName = itemView.findViewById(R.id.expenseItemName);
            expenseItemQuantity = itemView.findViewById(R.id.expenseItemQuantity);
            expenseItemTotalPrice = itemView.findViewById(R.id.expenseItemTotalPrice);
        }
        public void bind(Expense expense_t, int position) {

            DatabaseReference drinkRef = FirebaseDatabase.getInstance().getReference("drinks").child(expense_t.getIdDrink());

            drinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Drink drink_temp = dataSnapshot.getValue(Drink.class);
                    expenseItemName.setText(drink_temp.getName());
                    expenseItemQuantity.setText(String.valueOf(expense_t.getQuantity()) + " " + drink_temp.getUnit());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            int realPosition = position + 1;
            expenseItemNo.setText(String.valueOf(realPosition));
            expenseItemTotalPrice.setText("- " + String.valueOf(expense_t.getTotalPrice()) + " 000 VNĐ");
        }

    }
}
