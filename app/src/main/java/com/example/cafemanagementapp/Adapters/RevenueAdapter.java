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
import com.example.cafemanagementapp.Models.Revenue;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RevenueAdapter extends RecyclerView.Adapter<RevenueAdapter.RevenueViewHolder> {

    private List<Revenue> mRevenueList;

    public RevenueAdapter(List<Revenue> unitList) {
        mRevenueList = unitList;
    }

    @NonNull
    @Override
    public RevenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.revenue_item, parent, false);
        return new RevenueViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RevenueViewHolder holder, int position) {
        Revenue revenue_t = mRevenueList.get(position);
        if(revenue_t == null){
            return;
        }
        holder.bind(revenue_t, position);
    }

    @Override
    public int getItemCount() {
        if(mRevenueList != null){
            return  mRevenueList.size();
        }
        return 0;
    }

    public class RevenueViewHolder extends RecyclerView.ViewHolder {

        private TextView revenueItemNo;
        private TextView revenueItemName;
        private TextView revenueItemQuantity;
        private TextView revenueItemTotalPrice;
        public RevenueViewHolder(View itemView) {
            super(itemView);
            revenueItemNo = itemView.findViewById(R.id.revenueItemNo);
            revenueItemName = itemView.findViewById(R.id.revenueItemName);
            revenueItemQuantity = itemView.findViewById(R.id.revenueItemQuantity);
            revenueItemTotalPrice = itemView.findViewById(R.id.revenueItemTotalPrice);
        }
        public void bind(Revenue revenue_t, int position) {

            DatabaseReference drinkRef = FirebaseDatabase.getInstance().getReference("drinks").child(revenue_t.getIdDrink());

            drinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Drink drink_temp = dataSnapshot.getValue(Drink.class);
                    revenueItemName.setText(drink_temp.getName());
                    revenueItemQuantity.setText(String.valueOf(revenue_t.getQuantity()) + " " + drink_temp.getUnit());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            int realPosition = position + 1;
            revenueItemNo.setText(String.valueOf(realPosition));
            revenueItemTotalPrice.setText("+ " + String.valueOf(revenue_t.getTotalPrice()) + " 000 VNĐ");
        }

    }
}
