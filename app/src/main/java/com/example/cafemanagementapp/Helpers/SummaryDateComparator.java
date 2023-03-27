package com.example.cafemanagementapp.Helpers;
import com.example.cafemanagementapp.Models.Summary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class SummaryDateComparator implements Comparator<Summary> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    public int compare(Summary s1, Summary s2) {
        try {
            Date date1 = dateFormat.parse(s1.getSellDate());
            Date date2 = dateFormat.parse(s2.getSellDate());
            return date1.compareTo(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
