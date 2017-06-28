package com.blacksparrowgames.simplenote.fragments;


import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blacksparrowgames.simplenote.R;
import com.blacksparrowgames.simplenote.models.Note;
import com.blacksparrowgames.simplenote.provider.MySQLiteHelper;
import com.blacksparrowgames.simplenote.provider.NoteContentProvider;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;

public class NoteDetailFragment extends Fragment {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_GALLERY = 1;
    private static final int SELECT_PHOTO_RC = 2;

    private final static String ARG_ISTWOPANE = "isTwoPane";
    private final static String ARG_NOTE_ID = "noteId";

    private long actualNoteId = -1;
    private EditText title_textView;
    private EditText comment_textView;
    private ImageView cover_imageView;

    private Note selectedNote;
    private boolean isTwoPane;
    private boolean modoEdicion;
    private String image_full_path;

    public static NoteDetailFragment newInstance (long id, boolean isTwoPane){
        Bundle args = new Bundle();
        args.putBoolean(ARG_ISTWOPANE, isTwoPane);
        args.putLong(ARG_NOTE_ID, id);

        NoteDetailFragment mNoteDetailFragment = new NoteDetailFragment();
        mNoteDetailFragment.setArguments(args);
        return  mNoteDetailFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle parametros = getArguments(); //Aqui vienen los parametro de este Layout
        if(parametros != null) {
            isTwoPane = parametros.getBoolean(ARG_ISTWOPANE);
            setActualNoteId(parametros.getLong(ARG_NOTE_ID));
        }

        if (savedInstanceState != null) {
            setActualNoteId(savedInstanceState.getLong(ARG_NOTE_ID));
        }

        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.detail_fragment, container, false);

        title_textView = (EditText) view.findViewById(R.id.title_textView);

        comment_textView = (EditText)view.findViewById(R.id.comment_textView);

        cover_imageView = (ImageView) view.findViewById(R.id.cover_imageView);
        cover_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modoEdicion) {
                    getReadPermissions();
                }
            }
        });

        actualizaDatosItemSelected(actualNoteId);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_NOTE_ID, actualNoteId);
    }

    private void setActualNoteId(long id){
        this.actualNoteId = id;
    }


    private Note getFirstItem() {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(NoteContentProvider.CONTENT_URI, MySQLiteHelper.PROJECTION, null, null, MySQLiteHelper.TITLES_SORT_ORDER);
        Note firstNote = null;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            firstNote = Note.cursorToNote(cursor);
            setActualNoteId(firstNote.getId());
        }
        return firstNote;
    }

    private Note getItemWithID() {
        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = ContentUris.withAppendedId(NoteContentProvider.CONTENT_URI, this.actualNoteId);

        Cursor cursor = cr.query(uri, MySQLiteHelper.PROJECTION, null, null, MySQLiteHelper.DEFAULT_SORT_ORDER);
        Note firstNote;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            firstNote = Note.cursorToNote(cursor);
            return firstNote;
        }
        return null;
    }

    public void actualizaDatosItemSelected(long id){

        setActualNoteId(id);

        if(isTwoPane && modoEdicion){
            modoEdicion = false;
            title_textView.setEnabled(false);
            comment_textView.setEnabled(false);
        }

        if (actualNoteId < 0) {
            selectedNote = getFirstItem();
        } else {
            selectedNote = getItemWithID();
        }

        if (selectedNote != null){
            title_textView.setText(selectedNote.getTitle());
            comment_textView.setText(selectedNote.getComment());
            initImageCover();
        } else {
            title_textView.setText(R.string.empty_label);
            comment_textView.setText(R.string.empty_label);
            cover_imageView.setImageResource(R.drawable.noitemsfound);
        }
    }



    private void initImageCover() {

        image_full_path = selectedNote.getImagePath();

        if (image_full_path == null){
            cover_imageView.setImageResource(R.drawable.no_image_icon_10);

        } else {
            if (Build.VERSION.SDK_INT >= 23) {

                if (ContextCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_GALLERY);
                } else{
                    loadImageGallery();
                }
            } else {
                loadImageGallery();
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.setGroupVisible(R.id.group_fragment, true);
        if(!modoEdicion)
            menu.findItem(R.id.menu_save).setVisible(false);

        if (!isTwoPane) {
            menu.setGroupVisible(R.id.group_main, false);
        }
    }

    public void editNote(){
        modoEdicion = true;
        title_textView.setEnabled(true);
        comment_textView.setEnabled(true);
    }

    public boolean saveNote(){
        String textoTitulo = title_textView.getText().toString().trim();

        if (textoTitulo.equals(""))
            Toast.makeText(getActivity().getApplicationContext(), R.string.no_empty_title, Toast.LENGTH_SHORT).show();

        else {
            title_textView.setEnabled(false);
            comment_textView.setEnabled(false);

            editNoteContent();

            Toast.makeText(getActivity().getApplicationContext(), R.string.data_saved_ok, Toast.LENGTH_SHORT).show();
            modoEdicion = false;
            return true;
        }

        return false;
    }



    private void editNoteContent(){
        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = ContentUris.withAppendedId(NoteContentProvider.CONTENT_URI, actualNoteId);

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TITLE, title_textView.getText().toString());
        values.put(MySQLiteHelper.COLUMN_COMMENT, comment_textView.getText().toString());
        values.put(MySQLiteHelper.COLUMN_IMAGE_PATH, image_full_path);

        cr.update(uri, values, null, null);
    }


    //************* image cover

    public void getReadPermissions(){

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_GALLERY);
            } else {
                lanzarGalleryActivity();
            }
        } else {
            lanzarGalleryActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_FOR_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    lanzarGalleryActivity();
                } else {
                    Toast.makeText(getActivity(), R.string.deny_label, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }



    private void lanzarGalleryActivity() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, SELECT_PHOTO_RC);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_PHOTO_RC:

                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    image_full_path = getPathFromUri(imageUri);
                }

                loadImageGallery();

                break;
            default:
        }
    }


    private void loadImageGallery(){

        Bitmap galleryPic = scaleBitmap(100);

        if (galleryPic == null) {
            if(image_full_path != null) {
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.image_failure_label, Toast.LENGTH_SHORT).show();
            }
            image_full_path = null;
            cover_imageView.setImageResource(R.drawable.no_image_icon_10);
        } else {
            cover_imageView.setImageBitmap(galleryPic);
        }
    }


    private String getPathFromUri(Uri uri) {
        String path = "";
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);

        try {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            if (cursor.moveToFirst())
                path = cursor.getString(column_index);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        cursor.close();
        return path;
    }



    private Bitmap scaleBitmap(int maxDimension) {
        Bitmap scaledBitmap;

        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true; // return dimensions
        scaledBitmap = BitmapFactory.decodeFile(image_full_path, op);

        if ((maxDimension < op.outHeight) || (maxDimension < op.outWidth)) {
            op.inSampleSize = Math.round(Math.max((float) op.outHeight / (float) maxDimension,(float) op.outWidth / (float) maxDimension));
        }

        op.inJustDecodeBounds = false;
        scaledBitmap = BitmapFactory.decodeFile(image_full_path, op);

        return scaledBitmap;
    }
}