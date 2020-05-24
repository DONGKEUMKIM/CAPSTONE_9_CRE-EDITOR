package com.example.detection.fragment.subjectListFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.detection.MainActivity;
import com.example.detection.R;

import java.util.ArrayList;

public class SubjectListFragment extends Fragment {
    private ArrayList<String> idArray;
    private Button addNewSubjectBut;
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
        idArray = ((MainActivity)getActivity()).getIdArray();


        addNewSubjectBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewSubjectButClicked();
            }
        });
    }

    private void addNewSubjectButClicked(){
        ((MainActivity)getActivity()).addNewSubject();
    }
}
