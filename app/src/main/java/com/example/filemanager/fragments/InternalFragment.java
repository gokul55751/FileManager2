package com.example.filemanager.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.FileAdapter;
import com.example.filemanager.FileOpener;
import com.example.filemanager.MainActivity;
import com.example.filemanager.OnFileSelectedListener;
import com.example.filemanager.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.DexterBuilder;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InternalFragment extends Fragment implements OnFileSelectedListener {

    View view;
    private RecyclerView recyclerView;
    private List<File> fileList;
    private ImageView img_back;
    private TextView tv_pathHolder;
    File storage;
    private FileAdapter fileAdapter;
    String data;
    String[] items = {"Details", "Rename", "Delete", "Share"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_internal, container,false);
        tv_pathHolder = view.findViewById(R.id.tv_pathHolder);
        img_back = view.findViewById(R.id.img_back);

        String internalStorage = System.getenv("EXTERNAL_STORAGE");
        storage = new File(internalStorage);

        try {
            data = getArguments().getString("path");
            File file = new File(data);
            storage = file;
        }catch (Exception e){}

        tv_pathHolder.setText(storage.getAbsolutePath());
        runtimePermission();

        return view;
    }

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                displayFiles();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    private ArrayList<File> findFiles(File file){
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if(files==null) return arrayList;
        for (File singleFile : files){
            if (singleFile.isDirectory() || !singleFile.isHidden()){
//                arrayList.add(singleFile);
                arrayList.addAll(findFiles(singleFile));
            }
        }
        for (File singleFile : files){
            if (singleFile.getName().toLowerCase().endsWith(".jpeg") || singleFile.getName().toLowerCase().endsWith(".jpg") ||
                    singleFile.getName().toLowerCase().endsWith(".png") || singleFile.getName().toLowerCase().endsWith(".mp3") ||
                    singleFile.getName().toLowerCase().endsWith(".wav") || singleFile.getName().toLowerCase().endsWith(".mp4") ||
                    singleFile.getName().toLowerCase().endsWith(".pdf") || singleFile.getName().toLowerCase().endsWith(".doc") ||
                    singleFile.getName().toLowerCase().endsWith(".apk")){
                arrayList.add(singleFile);
            }
        }
        return arrayList;
    }

    private void displayFiles() {
        recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        fileList = new ArrayList<>();
        fileList.addAll(findFiles(storage));
        fileAdapter = new FileAdapter(getContext(), fileList, this);
        recyclerView.setAdapter(fileAdapter);
    }


    @Override
    public void onFileClicked(File file) {
        try{
            if (file.isDirectory()){
                Bundle bundle = new Bundle();
                bundle.putString("path", file.getAbsolutePath());
                InternalFragment internalFragment = new InternalFragment();
                internalFragment.setArguments(bundle);
                assert getFragmentManager() != null;
                getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        internalFragment).addToBackStack(null).commit();
            }else{
                try {
                    FileOpener.openFile(getContext(), file);
                }catch (Exception e){}
            }
        }catch (Exception e){
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFileLongClicked(File file, int postion) {
        final Dialog optionDialog = new Dialog(getContext());
        optionDialog.setContentView(R.layout.option_dialog);
        optionDialog.setTitle("Select Options.");
        ListView options = (ListView) optionDialog.findViewById(R.id.list);
        CustomAdapter customAdapter = new CustomAdapter();
        options.setAdapter(customAdapter);
        optionDialog.show();
        options.setOnItemClickListener((adapterView, view, i, l) -> {
            String selectedItem = adapterView.getItemAtPosition(i).toString();
            switch (selectedItem){
                case "Details":
                    AlertDialog.Builder detailDialog = new AlertDialog.Builder(getContext());
                    detailDialog.setTitle("Details:");
                    final TextView details = new TextView(getContext());
                    detailDialog.setView(details);
                    Date lastModified = new Date(file.lastModified());
                    SimpleDateFormat format = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
                    String formattedDate = format.format(lastModified);
                    details.setText("File Name" + file.getName() + "\n" + "Size: " +
                            Formatter.formatShortFileSize(getContext(), file.length())
                            + "\n" + "Path: " + file.getAbsolutePath() + "\n" +
                            "last modified: " + formattedDate);

                    detailDialog.setPositiveButton("OK", (dialogInterface, i1) -> {
                        optionDialog.cancel();
                    });
                    AlertDialog alertDialog_details = detailDialog.create();
                    alertDialog_details.show();
                    break;
                case "Rename":
                    AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
                    renameDialog.setTitle("Rename file");
                    final EditText name = new EditText(getContext());
                    renameDialog.setView(name);

                    renameDialog.setPositiveButton("ok", (dialogInterface, i13) -> {
                        String new_name = name.getEditableText().toString();
                        String extention = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                        File current = new File(file.getAbsolutePath());
                        File destination = new File(file.getAbsolutePath().replace(file.getName(), new_name)+extention);
                        if (current.renameTo(destination)){
                            fileList.set(postion, destination);
                            fileAdapter.notifyItemChanged(postion);
                            Toast.makeText(getContext(), "File Renamed.", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        }

                    });

                    renameDialog.setNegativeButton("Cancel", (dialogInterface, i12) -> {
                        optionDialog.cancel();
                    });
                    AlertDialog alertDialog_rename = renameDialog.create();
                    alertDialog_rename.show();
                    break;

                case "Delete":
                    AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                    deleteDialog.setTitle("Delete " + file.getName() + "?");
                    deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try{
                                file.delete();
                                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                Toast.makeText(getContext(), "Couldn't delete!", Toast.LENGTH_SHORT).show();
                            }
                            fileList.remove(postion);
                            fileAdapter.notifyDataSetChanged();
                        }
                    });
                    deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            optionDialog.cancel();
                        }
                    });
                    AlertDialog alertDialog_delete = deleteDialog.create();
                    alertDialog_delete.show();
                    break;
            }
        });
    }
    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return items[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.option_item, null);
            TextView txtOptions = myView.findViewById(R.id.txtOption);
            ImageView imgOptions = myView.findViewById(R.id.imgOption);
            txtOptions.setText(items[i]);
            if (items[i].equals("Details"))
                imgOptions.setImageResource(R.drawable.ic_baseline_info_24);
            if (items[i].equals("Rename"))
                imgOptions.setImageResource(R.drawable.ic_baseline_receipt_24);
            if (items[i].equals("Delete"))
                imgOptions.setImageResource(R.drawable.ic_baseline_delete_24);
            if (items[i].equals("Share"))
                imgOptions.setImageResource(R.drawable.ic_baseline_share_24);
            return myView;
        }
    }
}
