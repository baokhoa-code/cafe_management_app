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
import com.example.cafemanagementapp.Models.Import;
import com.example.cafemanagementapp.Models.Sell;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class SellFragAdapter extends RecyclerView.Adapter<SellFragAdapter.SellFragViewHolder> {

    private List<Sell> mSellFragList;

    private IClickListener mIClickListener;

    public interface IClickListener{
        void onClickEditItem(Sell sell_t);
        void onClickDeleteItem(Sell sell_t);
    };
    public SellFragAdapter(List<Sell> unitList, IClickListener iClickListener) {
        mSellFragList = unitList;
        mIClickListener = iClickListener;
    }

    @NonNull
    @Override
    public SellFragViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sellfrag_item, parent, false);
        return new SellFragViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SellFragViewHolder holder, int position) {
        Sell sell_t = mSellFragList.get(position);
        if(sell_t == null){
            return;
        }
        holder.bind(sell_t);
    }

    @Override
    public int getItemCount() {
        if(mSellFragList != null){
            return  mSellFragList.size();
        }
        return 0;
    }

    public class SellFragViewHolder extends RecyclerView.ViewHolder {

        private ImageView sellFragItemImage;
        private TextView sellFragItemDate;
        private TextView sellFragItemName;
        private TextView sellFragItemUnit;
        private TextView sellFragItemQuantity;
        private TextView sellFragItemUnitPrice;
        private TextView sellFragItemTotalPrice;

        private Button edit, delete;

        private Drink drink;

        public SellFragViewHolder(View itemView) {
            super(itemView);
            sellFragItemDate = itemView.findViewById(R.id.sellFragItemDate);
            sellFragItemImage = itemView.findViewById(R.id.sellFragItemImage);
            sellFragItemName = itemView.findViewById(R.id.sellFragItemName);
            sellFragItemUnit = itemView.findViewById(R.id.sellFragItemUnit);
            sellFragItemQuantity = itemView.findViewById(R.id.sellFragItemQuantity);
            sellFragItemUnitPrice = itemView.findViewById(R.id.sellFragItemUnitPrice);
            sellFragItemTotalPrice = itemView.findViewById(R.id.sellFragItemTotalPrice);
            drink = new Drink();

            edit = itemView.findViewById(R.id.btnSellFragItemEdit);
            delete = itemView.findViewById(R.id.btnSellFragItemDelete);
        }

        public void bind(Sell sell_t) {
            DatabaseReference drinkRef = FirebaseDatabase.getInstance().getReference("drinks").child(sell_t.getIdDrink());

            drinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Drink drink_temp = dataSnapshot.getValue(Drink.class);
                    sellFragItemImage.setImageBitmap(getSellFragImage(drink_temp.getImage()));
                    sellFragItemName.setText(drink_temp.getName());
                    sellFragItemUnit.setText(drink_temp.getUnit());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            sellFragItemDate.setText(sell_t.getSellDate());
            sellFragItemQuantity.setText(String.valueOf(sell_t.getQuantity()));
            sellFragItemUnitPrice.setText(String.valueOf(sell_t.getUnitPrice())+" 000 VNĐ");
            sellFragItemTotalPrice.setText(String.valueOf(sell_t.getTotalPrice())+" 000 VNĐ");

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIClickListener.onClickEditItem(sell_t);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIClickListener.onClickDeleteItem(sell_t);
                }
            });

        }

        private Bitmap getSellFragImage(String encodedImage){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
