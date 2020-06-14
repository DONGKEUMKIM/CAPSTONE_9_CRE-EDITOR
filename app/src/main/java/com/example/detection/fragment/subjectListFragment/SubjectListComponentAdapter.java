package com.example.detection.fragment.subjectListFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.detection.R;

import java.util.ArrayList;

public class SubjectListComponentAdapter extends ArrayAdapter implements View.OnClickListener {

    private ArrayList<SubjectListViewComponent> listViewComponents = new ArrayList<SubjectListViewComponent>();

    public interface ListBtnClickListener {
        void onListBtnClick(int position);
    }

    int resourceId;
    private ListBtnClickListener listBtnClickListener;

    public SubjectListComponentAdapter(Context context, int resource, ArrayList<SubjectListViewComponent> list, ListBtnClickListener clickListener) {
        super(context, resource, list);
        this.resourceId = resource;
        this.listBtnClickListener = clickListener;
    }

    ;

    @Override
    public int getCount() {
        return listViewComponents.size();
    }

    @Override
    public Object getItem(int i) {
        return listViewComponents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();


        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.subject_listview_component, parent, false);
        }


        TextView setSubjectName = (TextView) convertView.findViewById(R.id.set_slc_subjectName);
        ImageView setPriority = (ImageView) convertView.findViewById(R.id.set_slc_priority);
        TextView setTestTime = (TextView) convertView.findViewById(R.id.set_slc_testTime);

        SubjectListViewComponent listViewItem = (SubjectListViewComponent) getItem(position);

        setSubjectName.setText(listViewItem.getSubjectName());
        if(Integer.parseInt(listViewItem.getPriority())==0){
            setPriority.setImageResource(R.drawable.priority_0);
        }else if(Integer.parseInt(listViewItem.getPriority())==1){
            setPriority.setImageResource(R.drawable.priority_1);
        }else{
            setPriority.setImageResource(R.drawable.priority_2);
        }
        //setPriority.setText(listViewItem.getPriority());
        setTestTime.setText(listViewItem.getTestDate());




        return convertView;
    }

    @Override
    public void onClick(View view) {
        if (this.listBtnClickListener != null) {
            this.listBtnClickListener.onListBtnClick((int) view.getTag());
        }
    }

    public void add(String subjectName, String priority, String testTime, int id) {
        SubjectListViewComponent item = new SubjectListViewComponent(subjectName, priority, testTime,id);
        listViewComponents.add(item);
    }
}
