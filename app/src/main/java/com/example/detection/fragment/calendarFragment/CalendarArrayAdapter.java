package com.example.detection.fragment.calendarFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
        String[] strSplit =str.split(":");
        // Lookup view for data population
        TextView text_subject = (TextView) convertView.findViewById(R.id.calendar_list_view_component_text_subject);
        TextView text_date = (TextView) convertView.findViewById(R.id.calendar_list_view_component_text_date);
        TextView text_duringtime = (TextView) convertView.findViewById(R.id.calendar_list_view_component_text_duringtime);

        //text.setText(str);
        text_subject.setText(strSplit[0]);
        text_date.setText(strSplit[1]);
        text_duringtime.setText(strSplit[2]);
         //Populate the data into the template view using the data object

        ImageView imageView = (ImageView)convertView.findViewById(R.id.calendar_list_view_component_img);
        if(strSplit[4].equals("0"))
           imageView.setImageResource(R.drawable.schedule_component);
        else if(strSplit[4].equals("1"))
           imageView.setImageResource(R.drawable.schedule_completed_component);


        return convertView;
    }

}


