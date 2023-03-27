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
import com.example.cafemanagementapp.R;

import java.util.List;

public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {

    private List<Drink> mDrinkList;

    private IClickListener mIClickListener;

    public interface IClickListener{
        void onClickEditItem(Drink drink);
        void onClickDeleteItem(Drink drink);
    };
    public DrinkAdapter(List<Drink> unitList, IClickListener iClickListener) {
        mDrinkList = unitList;
        mIClickListener = iClickListener;
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.drink_item, parent, false);
        return new DrinkViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        Drink drink = mDrinkList.get(position);
        if(drink == null){
            return;
        }
        holder.bind(drink);
    }

    @Override
    public int getItemCount() {
        if(mDrinkList != null){
            return  mDrinkList.size();
        }
        return 0;
    }

    public class DrinkViewHolder extends RecyclerView.ViewHolder {

        private ImageView drinkItemImage;
        private TextView drinkItemName;
        private TextView drinkItemUnit;

        private Button edit, delete;

        public DrinkViewHolder(View itemView) {
            super(itemView);
            drinkItemImage = itemView.findViewById(R.id.drinkItemImage);
            drinkItemName = itemView.findViewById(R.id.drinkItemName);
            drinkItemUnit = itemView.findViewById(R.id.drinkItemUnit);
            edit = itemView.findViewById(R.id.btnDrinkItemEdit);
            delete = itemView.findViewById(R.id.btnDrinkItemDelete);
        }

        public void bind(Drink drink) {
            drinkItemImage.setImageBitmap(getDrinkImage(drink.getImage()));
            drinkItemName.setText(drink.getName());
            drinkItemUnit.setText(drink.getUnit());

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIClickListener.onClickEditItem(drink);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIClickListener.onClickDeleteItem(drink);
                }
            });

        }

        private Bitmap getDrinkImage(String encodedImage){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
