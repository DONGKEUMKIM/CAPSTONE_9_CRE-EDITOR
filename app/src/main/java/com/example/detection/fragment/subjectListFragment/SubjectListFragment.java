package com.example.detection.fragment.subjectListFragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.detection.MainActivity;
import com.example.detection.R;
import com.example.detection.db.SQLiteManager;
import com.example.detection.db.SubjectData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SubjectListFragment extends Fragment {
    private ArrayList<String> idArray;
    private Button addNewSubjectBut;
    private SwipeMenuListView mListView;
    private AppAdapter mAdapter;
    ArrayList<SubjectListViewComponent> items;
    SQLiteManager dbManager;
    private FloatingActionButton subjectFloatingActionButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        // Add Random Rows to the List
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        addNewSubjectBut = v.findViewById(R.id.add_new_subject_subject_list);
        idArray = ((MainActivity) getActivity()).getIdArray();
        mListView = v.findViewById(R.id.subject_listView);
        items = new ArrayList<SubjectListViewComponent>();
        addItems();
        subjectFloatingActionButton = v.findViewById(R.id.subjectfloatingActionButton);
        mAdapter = new AppAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(getContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Edit");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        subjectFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).addNewSubject();
            }
        });
        addNewSubjectBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).addNewSubject();
            }
        });


        mListView.setMenuCreator(creator);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                SubjectListViewComponent item = items.get(position);
                switch (index) {
                    case 0:
                        // open
                        open(item);
                        break;
                    case 1:
                        // delete
					    delete(item);
                        items.remove(position);
                        mAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });

        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        // set MenuStateChangeListener
        mListView.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            }

            @Override
            public void onMenuClose(int position) {
            }
        });

        // other setting
//		listView.setCloseInterpolator(new BounceInterpolator());

        // test item long click
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                Toast.makeText(getContext(), position + " long click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }
    private void addItems(){
        //TODO
        List<SubjectData> subjectData = ((MainActivity)getActivity()).getSubjectDataArray();
        for(int i=0;i<subjectData.size();i++){
            items.add(new SubjectListViewComponent(subjectData.get(i).getName(),Integer.toString(subjectData.get(i).getPriority()),((MainActivity)getActivity()).getTestTimeData(subjectData.get(i).getID()).getDate(),subjectData.get(i).getID()));
        }
        //mAdapter.notifyDataSetChanged();
    }

    private void delete(SubjectListViewComponent item) {
        SQLiteManager.sqLiteManager.deleteSubjectData(item.getId());
        SQLiteManager.sqLiteManager.deleteTestTimeData(item.getId());
        //TODO
    }

    private void open(SubjectListViewComponent item) {
        //TODO
        ((MainActivity)getActivity()).editSubject(item.getId());

    }
    class AppAdapter extends BaseSwipListAdapter {

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public SubjectListViewComponent getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(),
                        R.layout.subject_listview_component, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            SubjectListViewComponent item = getItem(position);
            holder.setSubjectName.setText(item.getSubjectName());
            holder.setPriority.setText(item.getPriority());
            holder.setTestTime.setText(item.getTestDate());
            if(position%4==0){
                holder.imgView.setImageResource(R.drawable.subject_card1);
            }
            else if(position%4==1){
                holder.imgView.setImageResource(R.drawable.subject_card2);
            }else if(position%4==2){
                holder.imgView.setImageResource(R.drawable.subject_card3);
            }else if(position%4==3){
                holder.imgView.setImageResource(R.drawable.subject_card4);
            }


            return convertView;
        }

        class ViewHolder {
            TextView setSubjectName;
            TextView setPriority;
            TextView setTestTime;
            ImageView imgView;


            public ViewHolder(View view) {
                setSubjectName = (TextView) view.findViewById(R.id.set_slc_subjectName);
                setPriority = (TextView) view.findViewById(R.id.set_slc_priority);
                setTestTime = (TextView) view.findViewById(R.id.set_slc_testTime);
                imgView = (ImageView) view.findViewById(R.id.subject_listView_comp_imgview);

                view.setTag(this);
            }
        }

        @Override
        public boolean getSwipEnableByPosition(int position) {
            if(position % 2 == 0){
                return false;
            }
            return true;
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}







