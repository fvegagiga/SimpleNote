package com.blacksparrowgames.simplenote.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.blacksparrowgames.simplenote.R;
import com.blacksparrowgames.simplenote.adapters.MyCursorAdapter;
import com.blacksparrowgames.simplenote.dialogs.EditNameDialog;
import com.blacksparrowgames.simplenote.models.Note;
import com.blacksparrowgames.simplenote.provider.MySQLiteHelper;
import com.blacksparrowgames.simplenote.provider.NoteContentProvider;

import static com.blacksparrowgames.simplenote.provider.NoteContentProvider.*;

public class DBListFragment extends CustomListFragment implements
        SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final static String ARG_ISTWOPANE = "isTwoPane";

    private MyCursorAdapter adapter;
    private Note selectedNote = null;
    private SearchView mSearchView;
    private String mCurFilter = null;
    private OnHeadlineSelectedListener mCallback;

    private boolean isTwoPane;
    private Note mSelectedNote;


    public static DBListFragment newInstance(boolean isTwoPane) {
        Bundle args = new Bundle();
        args.putInt("listLayoutId", R.layout.list_fragment);
        args.putInt("emptyViewId", android.R.id.empty);
        args.putBoolean(ARG_ISTWOPANE, isTwoPane);

        DBListFragment dbListFragment = new DBListFragment();
        dbListFragment.setArguments(args);
        return  dbListFragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MyCursorAdapter(getActivity(), R.layout.row, null,
                new String[]{MySQLiteHelper.COLUMN_LAST_TIME, MySQLiteHelper.COLUMN_TITLE},
                new int[]{R.id.txtTime, R.id.txtTitle},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);

        registerForContextMenu(getListView());
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle parametros = getArguments(); //Aqui vienen los parametro de este Layout
        if(parametros != null) {
            isTwoPane = parametros.getBoolean(ARG_ISTWOPANE);
        }
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener in Activity");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    public Note getNote() {
        if (selectedNote != null)
            return selectedNote;
        return null;
    }

    public void addNote() {
        mSelectedNote = null;
        showEditDialog();
    }

    public void deleteNote(int position) {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = adapter.getCursor();

        int colId = cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID);
        long id = cursor.getLong(colId);

        Uri uri = ContentUris.withAppendedId(CONTENT_URI,id);

        cr.delete(uri, null, null);
        getActivity().getLoaderManager().restartLoader(0, null, this);

        mCallback.onItemDeleted();
    }

    public int getItemCount() {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(CONTENT_URI, MySQLiteHelper.PROJECTION, null, null, MySQLiteHelper.TITLES_SORT_ORDER);
        return cursor.getCount(); // devuelve el número de elementos en la lista
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        index = position;
        itemId = id;
        if (isResumed()) {
            Cursor cursor = (Cursor) adapter.getItem(index);
            selectedNote = Note.cursorToNote(cursor);
            mCallback.onItemSelected(id);
        }
    }

    public void showEditDialog() {
        FragmentManager fm = getFragmentManager();
        EditNameDialog editNameDialog = new EditNameDialog();
        editNameDialog.show(fm, "fragment_edit_name");
    }

    public String getNameToEdit() {
        String name = null;
        selectedNote = null;

        if (selectedNote != null)
            name = selectedNote.getTitle();
        return name;
    }

    public void onFinishEditDialog(boolean result, String editedName) {

        if (result) {
            if (selectedNote == null) {
                ContentResolver cr = getActivity().getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MySQLiteHelper.COLUMN_TITLE, editedName);

                cr.insert(CONTENT_URI, values);
                getActivity().getLoaderManager().restartLoader(0, null, this);

                if (isTwoPane) {
                    mCallback.onItemSelected(lastItemInserted());
                }
            }
        }
        selectedNote = null;
    }

    private long lastItemInserted() {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(CONTENT_URI, MySQLiteHelper.PROJECTION, null, null, MySQLiteHelper.DEFAULT_SORT_ORDER);
        Note firstHoja = null;

        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            firstHoja = Note.cursorToNote(cursor);
        }
        return firstHoja.getId();
    }

    public void updateListView(){       // le llamamos cuando hemos modificado un registro, para que se refleje
        getActivity().getLoaderManager().restartLoader(0, null, this);
    }


    //------------------------------Search and filter note
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.setGroupVisible(R.id.group_main, true);

        // Place an action bar item for searching.
        final MenuItem item = menu.findItem(R.id.menu_search);

        mSearchView = (SearchView) item.getActionView();
        mSearchView.setOnQueryTextListener(this);
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchView.setQuery(null, true);
                return true; // collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // TODO Auto-generated method stub
                return true;
            }
        });
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;

        if (mCurFilter == null && newFilter == null) {
            return true;
        }
        if (mCurFilter != null && mCurFilter.equals(newFilter)) {
            return true;
        }

        mCurFilter = newFilter;
        getLoaderManager().restartLoader(0, null, this);

        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;
    }



    //---------------------------ACTUACION DEL LoaderCallbacks<Cursor>


    //Todo esto lo hace cada vez que llamamos por getActivity().getLoaderManager().restartLoader(0, null, this);
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String select = null;
        if (mCurFilter != null && !mCurFilter.trim().equals("")) { // search filter
            select = MySQLiteHelper.COLUMN_TITLE + " LIKE " + '"'+ mCurFilter + '%' + '"';
        }
        return new CursorLoader(getActivity(),
                CONTENT_URI, PROJECTION, select, null, TIME_SORT_ORDER);
    }

    //Cuando termina de recorrer la base de datos
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Save the new note to the database
        adapter.swapCursor(data);
        adapter.notifyDataSetChanged();//update list
    }

    //Si se hace reset y se recarga la lista
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }




    public interface OnHeadlineSelectedListener {
        void onItemSelected(long noteId);
        void onItemDeleted();
    }
}