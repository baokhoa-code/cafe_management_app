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

public class SellAdapter extends RecyclerView.Adapter<SellAdapter.SellViewHolder> {

    private List<Sell> mSellList;

    private IClickListener mIClickListener;

    public interface IClickListener{
        void onClickEditItem(Sell sell_t);
        void onClickDeleteItem(Sell sell_t);
    };
    public SellAdapter(List<Sell> unitList, IClickListener iClickListener) {
        mSellList = unitList;
        mIClickListener = iClickListener;
    }

    @NonNull
    @Override
    public SellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sell_item, parent, false);
        return new SellViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SellViewHolder holder, int position) {
        Sell sell_t = mSellList.get(position);
        if(sell_t == null){
            return;
        }
        holder.bind(sell_t);
    }

    @Override
    public int getItemCount() {
        if(mSellList != null){
            return  mSellList.size();
        }
        return 0;
    }

    public class SellViewHolder extends RecyclerView.ViewHolder {

        private ImageView sellItemImage;
        private TextView sellItemName;
        private TextView sellItemUnit;
        private TextView sellItemQuantity;
        private TextView sellItemUnitPrice;
        private TextView sellItemTotalPrice;

        private Button edit, delete;

        private Drink drink;

        public SellViewHolder(View itemView) {
            super(itemView);
            sellItemImage = itemView.findViewById(R.id.sellItemImage);
            sellItemName = itemView.findViewById(R.id.sellItemName);
            sellItemUnit = itemView.findViewById(R.id.sellItemUnit);
            sellItemQuantity = itemView.findViewById(R.id.sellItemQuantity);
            sellItemUnitPrice = itemView.findViewById(R.id.sellItemUnitPrice);
            sellItemTotalPrice = itemView.findViewById(R.id.sellItemTotalPrice);
            drink = new Drink();

            edit = itemView.findViewById(R.id.btnSellItemEdit);
            delete = itemView.findViewById(R.id.btnSellItemDelete);
        }

        public void bind(Sell sell_t) {
            DatabaseReference drinkRef = FirebaseDatabase.getInstance().getReference("drinks").child(sell_t.getIdDrink());

            drinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Drink drink_temp = dataSnapshot.getValue(Drink.class);
                    sellItemImage.setImageBitmap(getSellImage(drink_temp.getImage()));
                    sellItemName.setText(drink_temp.getName());
                    sellItemUnit.setText(drink_temp.getUnit());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            sellItemQuantity.setText(String.valueOf(sell_t.getQuantity()));
            sellItemUnitPrice.setText(String.valueOf(sell_t.getUnitPrice())+" 000 VNĐ");
            sellItemTotalPrice.setText(String.valueOf(sell_t.getTotalPrice())+" 000 VNĐ");

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

        private Bitmap getSellImage(String encodedImage){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
