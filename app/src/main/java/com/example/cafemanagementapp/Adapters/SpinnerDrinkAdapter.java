package com.example.cafemanagementapp.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.cafemanagementapp.Models.Drink;

import java.util.List;

public class SpinnerDrinkAdapter extends ArrayAdapter<Drink> {

    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
    private List<Drink> drinks;

    public SpinnerDrinkAdapter(Context context, int textViewResourceId,
                               List<Drink> drinks) {
        super(context, textViewResourceId, drinks);
        this.context = context;
        this.drinks = drinks;
    }

    @Override
    public int getCount(){
        return drinks.size();
    }

    @Override
    public Drink getItem(int position){
        return drinks.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }


    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(drinks.get(position).getName());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(drinks.get(position).getName());

        return label;
    }
}