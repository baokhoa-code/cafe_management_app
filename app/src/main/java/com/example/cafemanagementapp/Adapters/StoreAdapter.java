package com.example.cafemanagementapp.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.InputFilter;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagementapp.Activities.SellActivity;
import com.example.cafemanagementapp.Helpers.InputFilterMinMax;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.Models.Import;
import com.example.cafemanagementapp.Models.Sell;
import com.example.cafemanagementapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private List<Drink> mStoreList;

    private IClickListener mIClickListener;

    public interface IClickListener{
        void onClickDetailItem(Drink Store);
    };
    public StoreAdapter(List<Drink> unitList, IClickListener iClickListener) {
        mStoreList = unitList;
        mIClickListener = iClickListener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_item, parent, false);
        return new StoreViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Drink Store = mStoreList.get(position);
        if(Store == null){
            return;
        }
        holder.bind(Store);
    }

    @Override
    public int getItemCount() {
        if(mStoreList != null){
            return  mStoreList.size();
        }
        return 0;
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder {

        private ImageView storeItemImage;
        private TextView storeItemName;
        private TextView storeItemUnit;
        private TextView storeItemQuantity;
        private Button detail;

        public StoreViewHolder(View itemView) {
            super(itemView);
            storeItemImage = itemView.findViewById(R.id.storeItemImage);
            storeItemName = itemView.findViewById(R.id.storeItemName);
            storeItemUnit = itemView.findViewById(R.id.storeItemUnit);
            storeItemQuantity = itemView.findViewById(R.id.storeItemQuantity);
            detail = itemView.findViewById(R.id.btnStoreItemGoDetail);
        }

        public void bind(Drink Store) {
            storeItemImage.setImageBitmap(getStoreImage(Store.getImage()));
            storeItemName.setText(Store.getName());
            storeItemUnit.setText(Store.getUnit());

            InStockCallback callback = new InStockCallback() {
                @Override
                public void onInStockReceived(int inStock) {
                    storeItemQuantity.setText(String.valueOf(inStock));
                }
            };

            inStock(Store.getId(), callback);

            detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIClickListener.onClickDetailItem(Store);
                }
            });


        }

        private Bitmap getStoreImage(String encodedImage){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }

    private void inStock(String drinkId, final InStockCallback callback) {
        TotalQuantityCallback importCallback = new TotalQuantityCallback() {
            @Override
            public void onTotalQuantityReceived(int importQuantity) {
                TotalQuantityCallback sellCallback = new TotalQuantityCallback() {
                    @Override
                    public void onTotalQuantityReceived(int sellQuantity) {
                        int inStock = importQuantity - sellQuantity;
                        callback.onInStockReceived(inStock);
                    }
                };
                totalSell(drinkId, sellCallback);
            }
        };
        totalImport(drinkId, importCallback);
    }
    public interface TotalQuantityCallback {
        void onTotalQuantityReceived(int totalQuantity);
    }
    public interface InStockCallback {
        void onInStockReceived(int inStock);
    }
    private void totalImport(String drinkId, TotalQuantityCallback callback){
        DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference("imports");
        Query query = importsRef.orderByChild("idDrink").equalTo(drinkId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalQuantity = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Import importData = snapshot.getValue(Import.class);
                    int quantity = importData.getQuantity();
                    totalQuantity += quantity;
                }
                callback.onTotalQuantityReceived(totalQuantity);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void totalSell(String drinkId, TotalQuantityCallback callback){
        DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference("sells");
        Query query = importsRef.orderByChild("idDrink").equalTo(drinkId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalQuantity = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Sell sellData = snapshot.getValue(Sell.class);
                    int quantity = sellData.getQuantity();
                    totalQuantity += quantity;
                }
                callback.onTotalQuantityReceived(totalQuantity);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
