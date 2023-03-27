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
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ImportAdapter extends RecyclerView.Adapter<ImportAdapter.ImportViewHolder> {

    private List<Import> mImportList;

    private IClickListener mIClickListener;

    public interface IClickListener{
        void onClickEditItem(Import import_t);
        void onClickDeleteItem(Import import_t);
    };
    public ImportAdapter(List<Import> unitList, IClickListener iClickListener) {
        mImportList = unitList;
        mIClickListener = iClickListener;
    }

    @NonNull
    @Override
    public ImportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.import_item, parent, false);
        return new ImportViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImportViewHolder holder, int position) {
        Import import_t = mImportList.get(position);
        if(import_t == null){
            return;
        }
        holder.bind(import_t);
    }

    @Override
    public int getItemCount() {
        if(mImportList != null){
            return  mImportList.size();
        }
        return 0;
    }

    public class ImportViewHolder extends RecyclerView.ViewHolder {

        private ImageView importItemImage;
        private TextView importItemName;
        private TextView importItemUnit;
        private TextView importItemQuantity;
        private TextView importItemUnitPrice;
        private TextView importItemTotalPrice;

        private Button edit, delete;

        private Drink drink;

        public ImportViewHolder(View itemView) {
            super(itemView);
            importItemImage = itemView.findViewById(R.id.importItemImage);
            importItemName = itemView.findViewById(R.id.importItemName);
            importItemUnit = itemView.findViewById(R.id.importItemUnit);
            importItemQuantity = itemView.findViewById(R.id.importItemQuantity);
            importItemUnitPrice = itemView.findViewById(R.id.importItemUnitPrice);
            importItemTotalPrice = itemView.findViewById(R.id.importItemTotalPrice);
            drink = new Drink();

            edit = itemView.findViewById(R.id.btnImportItemEdit);
            delete = itemView.findViewById(R.id.btnImportItemDelete);
        }

        public void bind(Import import_t) {
            DatabaseReference drinkRef = FirebaseDatabase.getInstance().getReference("drinks").child(import_t.getIdDrink());

            drinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Drink drink_temp = dataSnapshot.getValue(Drink.class);
                    importItemImage.setImageBitmap(getImportImage(drink_temp.getImage()));
                    importItemName.setText(drink_temp.getName());
                    importItemUnit.setText(drink_temp.getUnit());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            importItemQuantity.setText(String.valueOf(import_t.getQuantity()));
            importItemUnitPrice.setText(String.valueOf(import_t.getUnitPrice())+" 000 VNĐ");
            importItemTotalPrice.setText(String.valueOf(import_t.getTotalPrice())+" 000 VNĐ");

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIClickListener.onClickEditItem(import_t);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIClickListener.onClickDeleteItem(import_t);
                }
            });

        }

        private Bitmap getImportImage(String encodedImage){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
