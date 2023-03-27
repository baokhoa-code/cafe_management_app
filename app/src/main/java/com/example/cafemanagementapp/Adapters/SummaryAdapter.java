package com.example.cafemanagementapp.Adapters;

import android.annotation.SuppressLint;
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
import com.example.cafemanagementapp.Models.Summary;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder> {

    private List<Summary> mSummaryList;

    public SummaryAdapter(List<Summary> unitList) {
        mSummaryList = unitList;
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.summary_item, parent, false);
        return new SummaryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        Summary summary_t = mSummaryList.get(position);
        if(summary_t == null){
            return;
        }
        holder.bind(summary_t, position);
    }

    @Override
    public int getItemCount() {
        if(mSummaryList != null){
            return  mSummaryList.size();
        }
        return 0;
    }

    public class SummaryViewHolder extends RecyclerView.ViewHolder {

        private TextView summaryItemNo;
        private TextView summaryItemName;
        private TextView summaryItemQuantity;
        private TextView summaryItemTotalPrice;
        private View view;
        public SummaryViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            summaryItemNo = itemView.findViewById(R.id.summaryItemNo);
            summaryItemName = itemView.findViewById(R.id.summaryItemName);
            summaryItemQuantity = itemView.findViewById(R.id.summaryItemQuantity);
            summaryItemTotalPrice = itemView.findViewById(R.id.summaryItemTotalPrice);
        }
        @SuppressLint("ResourceAsColor")
        public void bind(Summary summary_t, int position) {

            DatabaseReference drinkRef = FirebaseDatabase.getInstance().getReference("drinks").child(summary_t.getIdDrink());

            drinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Drink drink_temp = dataSnapshot.getValue(Drink.class);
                    summaryItemName.setText(drink_temp.getName());
                    summaryItemQuantity.setText(String.valueOf(summary_t.getQuantity()) + " " + drink_temp.getUnit());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            int realPosition = position + 1;
            summaryItemNo.setText(String.valueOf(realPosition));
            summaryItemTotalPrice.setText(String.valueOf(summary_t.getTotalPrice()) + " 000 VNĐ");
            if(summary_t.getTotalPrice()<0){
                summaryItemTotalPrice.setTextColor(view.getResources().getColor(R.color.total_price_lo));
                summaryItemTotalPrice.setText("- " + String.valueOf(-summary_t.getTotalPrice()) + " 000 VNĐ");
            }else{
                summaryItemTotalPrice.setTextColor(view.getResources().getColor(R.color.total_price_loi));
                summaryItemTotalPrice.setText("+ " + String.valueOf(summary_t.getTotalPrice()) + " 000 VNĐ");
            }

        }

    }
}
