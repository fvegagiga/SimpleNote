package com.blacksparrowgames.simplenote.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class CustomListFragment extends ListFragment {

    protected int index = 0;
    private ListItemSelectedListener selectedListener;
    private int listLayoutId = 0;
    private int emptyViewId = 0;
    private View emptyView = null;
    protected long itemId = -1;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        index = position;
        itemId = id;
        selectedListener.onListItemSelected(index, getTag(), id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle parameters = getArguments();
        if (parameters != null) {
            listLayoutId = parameters.getInt("listLayoutId");
            emptyViewId = parameters.getInt("emptyViewId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragmentView = null;
        try {
            myFragmentView = inflater.inflate(listLayoutId, container, false);
        } catch (Exception e) {
            myFragmentView = inflater.inflate(android.R.layout.list_content, container, false);
        }

        View standardEmptyView = myFragmentView.findViewById(android.R.id.empty);
        if (standardEmptyView != null) {
            standardEmptyView.setId(0);
        }

        return myFragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = getListView();
        emptyView = getActivity().findViewById(emptyViewId);

        if (emptyView != null) {
            listView.setEmptyView(emptyView);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index", 0);
            selectedListener.onListItemSelected(index, getTag(), itemId);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("index", index);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            selectedListener = (ListItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ListItemSelectedListener in Activity");
        }
    }




    public interface ListItemSelectedListener {
        public void onListItemSelected(int index, String tag, long id);
    }

}
