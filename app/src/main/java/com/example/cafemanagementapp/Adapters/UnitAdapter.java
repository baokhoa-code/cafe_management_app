package com.example.cafemanagementapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cafemanagementapp.R;

import java.util.List;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> {

    private List<String> mUnitList;

    private IClickListener mIClickListener;

    public interface IClickListener{
        void onClickEditItem(String s);
        void onClickDeleteItem(String s);
    };
    public UnitAdapter(List<String> unitList, IClickListener iClickListener) {
        mUnitList = unitList;
        mIClickListener = iClickListener;
    }

    @NonNull
    @Override
    public UnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.unit_item, parent, false);
        return new UnitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UnitViewHolder holder, int position) {
        String unit = mUnitList.get(position);
        if(unit == null){
            return;
        }
        holder.bind(unit);
    }

    @Override
    public int getItemCount() {
        if(mUnitList != null){
            return  mUnitList.size();
        }
        return 0;
    }

    public class UnitViewHolder extends RecyclerView.ViewHolder {

        private TextView unitItemName;

        private Button edit, delete;

        public UnitViewHolder(View itemView) {
            super(itemView);
            unitItemName = itemView.findViewById(R.id.unitItemName);
            edit = itemView.findViewById(R.id.btnUnitItemEdit);
            delete = itemView.findViewById(R.id.btnUnitItemDelete);
        }

        public void bind(String unit) {

            unitItemName.setText(unit);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIClickListener.onClickEditItem(unit);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mIClickListener.onClickDeleteItem(unit);
                }
            });

        }
    }
}
