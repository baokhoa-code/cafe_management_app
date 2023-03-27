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

public class ImportFragAdapter extends RecyclerView.Adapter<ImportFragAdapter.ImportFragViewHolder> {

    private List<Import> mImportFragList;

    private IClickListener mIClickListener;

    public interface IClickListener{
        void onClickEditItem(Import import_t);
        void onClickDeleteItem(Import import_t);
    };
    public ImportFragAdapter(List<Import> unitList, IClickListener iClickListener) {
        mImportFragList = unitList;
        mIClickListener = iClickListener;
    }

    @NonNull
    @Override
    public ImportFragViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.importfrag_item, parent, false);
        return new ImportFragViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImportFragViewHolder holder, int position) {
        Import import_t = mImportFragList.get(position);
        if(import_t == null){
            return;
        }
        holder.bind(import_t);
    }

    @Override
    public int getItemCount() {
        if(mImportFragList != null){
            return  mImportFragList.size();
        }
        return 0;
    }

    public class ImportFragViewHolder extends RecyclerView.ViewHolder {

        private ImageView importFragItemImage;
        private TextView importFragItemDate;
        private TextView importFragItemName;
        private TextView importFragItemUnit;
        private TextView importFragItemQuantity;
        private TextView importFragItemUnitPrice;
        private TextView importFragItemTotalPrice;

        private Button edit, delete;

        private Drink drink;

        public ImportFragViewHolder(View itemView) {
            super(itemView);
            importFragItemDate = itemView.findViewById(R.id.importFragItemDate);
            importFragItemImage = itemView.findViewById(R.id.importFragItemImage);
            importFragItemName = itemView.findViewById(R.id.importFragItemName);
            importFragItemUnit = itemView.findViewById(R.id.importFragItemUnit);
            importFragItemQuantity = itemView.findViewById(R.id.importFragItemQuantity);
            importFragItemUnitPrice = itemView.findViewById(R.id.importFragItemUnitPrice);
            importFragItemTotalPrice = itemView.findViewById(R.id.importFragItemTotalPrice);
            drink = new Drink();

            edit = itemView.findViewById(R.id.btnImportFragItemEdit);
            delete = itemView.findViewById(R.id.btnImportFragItemDelete);
        }

        public void bind(Import import_t) {
            DatabaseReference drinkRef = FirebaseDatabase.getInstance().getReference("drinks").child(import_t.getIdDrink());

            drinkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Drink drink_temp = dataSnapshot.getValue(Drink.class);
                    importFragItemImage.setImageBitmap(getImportFragImage(drink_temp.getImage()));
                    importFragItemName.setText(drink_temp.getName());
                    importFragItemUnit.setText(drink_temp.getUnit());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            importFragItemDate.setText(import_t.getImportDate());
            importFragItemQuantity.setText(String.valueOf(import_t.getQuantity()));
            importFragItemUnitPrice.setText(String.valueOf(import_t.getUnitPrice())+" 000 VNĐ");
            importFragItemTotalPrice.setText(String.valueOf(import_t.getTotalPrice())+" 000 VNĐ");

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

        private Bitmap getImportFragImage(String encodedImage){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
