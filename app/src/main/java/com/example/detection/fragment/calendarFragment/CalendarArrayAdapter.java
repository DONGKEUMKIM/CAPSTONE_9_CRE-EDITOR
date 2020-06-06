package com.example.detection.fragment.calendarFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.detection.R;

import java.util.List;

public class CalendarArrayAdapter extends ArrayAdapter<String> {

    public CalendarArrayAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.calendar_list_view_component, parent, false);
        }

        // Get the data item for this position
        String str = getItem(position);

        // Lookup view for data population
        TextView text = (TextView) convertView.findViewById(R.id.calendar_list_view_component_text);
        text.setText(str);
        // Populate the data into the template view using the data object

        return convertView;
    }

}


