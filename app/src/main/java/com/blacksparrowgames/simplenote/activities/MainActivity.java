package com.blacksparrowgames.simplenote.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import com.blacksparrowgames.simplenote.R;
import com.blacksparrowgames.simplenote.dialogs.EditNameDialog.EditNameDialogListener;
import com.blacksparrowgames.simplenote.fragments.CustomListFragment.ListItemSelectedListener;
import com.blacksparrowgames.simplenote.fragments.DBListFragment;
import com.blacksparrowgames.simplenote.fragments.DBListFragment.OnHeadlineSelectedListener;
import com.blacksparrowgames.simplenote.fragments.NoteDetailFragment;

public class MainActivity extends Activity implements
        EditNameDialogListener, ListItemSelectedListener, OnHeadlineSelectedListener {

    public static final String LIST_NOTE_TAG = "ListNote";
    public static final String DETAIL_NOTE_TAG = "DetailNote";
    private final static String ARG_NOTE_ID = "noteId";
    private final static String ARG_ACTUAL_PANEL = "actualPanel";
    private final static String ARG_MODO_EDICION = "modoEdicion";

    private DBListFragment mListFrag;
    private NoteDetailFragment mDetailFrag;
    private Menu menu;

    private boolean isTwopane = true;
    private boolean actualPanelList = true;
    private boolean modoEdicion;

    private long noteId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null) {
            isTwopane = false;
        }

        if (savedInstanceState != null) {
            noteId = savedInstanceState.getLong(ARG_NOTE_ID);
            actualPanelList = savedInstanceState.getBoolean(ARG_ACTUAL_PANEL);
            modoEdicion = savedInstanceState.getBoolean(ARG_MODO_EDICION);
        } else {
            noteId = -1;
            modoEdicion = false;
        }

        if (isTwopane) {
            createListFragment();
            createDetailFragment(noteId);
        } else {
            if (actualPanelList)
                createListFragment();
            else
                createDetailFragment(noteId);
        }

        getLoaderManager();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(ARG_NOTE_ID, noteId);

        Boolean actualPanelDetail = menu.getItem(0).isVisible();
        outState.putBoolean(ARG_ACTUAL_PANEL, actualPanelDetail);
        outState.putBoolean(ARG_MODO_EDICION, modoEdicion);
    }

    @Override
    protected void onResume() {
        super.onResume();

        FragmentManager fm = getFragmentManager();
        mListFrag = (DBListFragment) fm.findFragmentByTag(LIST_NOTE_TAG);

        if (mListFrag == null) {
            createListFragment();
        }

        if (isTwopane) {
            if (modoEdicion) {
                mDetailFrag = (NoteDetailFragment) fm.findFragmentByTag(DETAIL_NOTE_TAG);
                mDetailFrag.editNote();
            }
        }

    }


    private void createListFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        mListFrag = (DBListFragment) fm.findFragmentByTag(LIST_NOTE_TAG);

        int fragmentLayoutPlaceId = isTwopane ? R.id.listPlace : R.id.fragment_container;

        if (mListFrag == null) {
            mListFrag = DBListFragment.newInstance(isTwopane);
            ft.add(fragmentLayoutPlaceId, mListFrag, LIST_NOTE_TAG);
            ft.commit();
        } else {
            ft.replace(fragmentLayoutPlaceId, mListFrag, LIST_NOTE_TAG);
            ft.commit();
        }
    }

    private void createDetailFragment(long noteId) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        mDetailFrag = (NoteDetailFragment) fm.findFragmentByTag(DETAIL_NOTE_TAG);

        int fragmentLayoutPlaceId = isTwopane ? R.id.detailPlace : R.id.fragment_container;

        if (isTwopane) {
            if (mDetailFrag == null) {
                mDetailFrag = (NoteDetailFragment) newInstanceDetailFragment();
                ft.add(fragmentLayoutPlaceId, mDetailFrag, DETAIL_NOTE_TAG);
                ft.commit();
            }
        } else {
            if (mDetailFrag == null) {
                mDetailFrag = (NoteDetailFragment) newInstanceDetailFragment();
                ft.replace(fragmentLayoutPlaceId, mDetailFrag, DETAIL_NOTE_TAG);
                ft.addToBackStack(null);
                ft.commit();
            }
        }
    }

    public Fragment newInstanceDetailFragment() {
        return NoteDetailFragment.newInstance(noteId, isTwopane);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);

        menu.setGroupVisible(R.id.group_main, true);
        menu.setGroupVisible(R.id.group_fragment, false);

        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                mListFrag.addNote();
                return true;

            case R.id.menu_edit:
                if (mListFrag.getItemCount() > 0) {
                    modoEdicion = true;
                    mDetailFrag.editNote();
                    menu.findItem(R.id.menu_save).setVisible(true);
                }
                return true;

            case R.id.menu_save:
                if (mDetailFrag.saveNote()) {
                    modoEdicion = false;
                    menu.findItem(R.id.menu_save).setVisible(false);
                }
                if (isTwopane) mListFrag.updateListView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mListFrag.deleteNote(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onFinishEditDialog(boolean result, String editedName) {
        mListFrag.onFinishEditDialog(result, editedName);
    }

    @Override
    public String getNameToEdit() {
        return mListFrag.getNameToEdit();
    }


    @Override
    public void onListItemSelected(int index, String tag, long id) {

    }


    @Override
    public void onItemSelected(long id) {
        this.noteId = id;
        modoEdicion = false;

        if (noteId >= 0) {

            if (isTwopane) {
                mDetailFrag.actualizaDatosItemSelected(id);
                menu.findItem(R.id.menu_save).setVisible(false);
            } else {
                createDetailFragment(noteId);
            }
        } else {
            Toast.makeText(this, "Error on load", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemDeleted() {
        if (isTwopane) {
            NoteDetailFragment detailCursorAdapter = (NoteDetailFragment) getFragmentManager().findFragmentByTag(DETAIL_NOTE_TAG);
            detailCursorAdapter.actualizaDatosItemSelected(-1);
        }
    }
}