package com.release.perfectapplock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;

public class CameraActivity extends AppCompatActivity {

    private ImageView tg_camera;
    private Spinner spinner;

    private RecyclerView rc;
    private ImageView spy;
    private ImageView del;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tg_camera = findViewById(R.id.toggle_camera);
        spinner = findViewById(R.id.spinner);
        rc = findViewById(R.id.rc);
        spy = findViewById(R.id.spy);
        del = findViewById(R.id.del);

        boolean b = Util.isSpyCamera(CameraActivity.this);
        tg_camera.setTag(b);
        int pos = Util.getSpyCameraPos(CameraActivity.this);

        String[] arr = getResources().getStringArray(R.array.spinner_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arr);
        spinner.setAdapter(adapter);
        spinner.setSelection(pos);

        if(b){
            tg_camera.setImageResource(R.drawable.toggle_on);
            spinner.setEnabled(true);
        }else{
            tg_camera.setImageResource(R.drawable.toggle_off);
            spinner.setEnabled(false);
        }

        tg_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((boolean) view.getTag()){
                    tg_camera.setImageResource(R.drawable.toggle_off);
                    spinner.setEnabled(false);
                    Util.setSpyCamera(CameraActivity.this, false);
                    tg_camera.setTag(false);
                }else{
                    checkPermission();
                }
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Util.setSpyCameraPos(CameraActivity.this, i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        readPictures();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void readPictures(){
        File file = new File(getFilesDir() + "/pic");

        if (file.exists()){
            spy.setVisibility(View.GONE);
            rc.setVisibility(View.VISIBLE);
            rc.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            File[] files = file.listFiles();
            if(files != null) {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File file, File t1) {
                        return t1.compareTo(file);
                    }
                });
                rc.setAdapter(new GalleryAdopter(files));
                MyListDecoration decoration = new MyListDecoration();
                rc.addItemDecoration(decoration);
                del.setVisibility(View.VISIBLE);
                del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setDirEmpty(file.getPath());
                        spy.setVisibility(View.VISIBLE);
                        rc.setVisibility(View.GONE);
                        del.setVisibility(View.GONE);
                    }
                });
            }else {
                spy.setVisibility(View.VISIBLE);
                rc.setVisibility(View.GONE);
                del.setVisibility(View.GONE);
            }

        }else{
            spy.setVisibility(View.VISIBLE);
            rc.setVisibility(View.GONE);
            del.setVisibility(View.GONE);
        }
    }
    public void setDirEmpty(String path){
        File dir = new File(path);
        File[] childFileList = dir.listFiles();

        if (dir.exists()) {
            if (childFileList != null) {
                for (File childFile : childFileList) {
                    if (childFile.isDirectory()) {
                        setDirEmpty(childFile.getAbsolutePath());    //하위 디렉토리
                    } else {
                        childFile.delete();    //하위 파일
                    }
                }
            }
            dir.delete();
        }
    }

    public void checkPermission(){
        if (ActivityCompat.checkSelfPermission(CameraActivity.this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA},
                    102);
        }else{
            tg_camera.setImageResource(R.drawable.toggle_on);
            spinner.setEnabled(true);
            Util.setSpyCamera(CameraActivity.this, true);
            tg_camera.setTag(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            tg_camera.setImageResource(R.drawable.toggle_on);
            spinner.setEnabled(true);
            Util.setSpyCamera(CameraActivity.this, true);
            tg_camera.setTag(true);
        }
    }

    public class GalleryAdopter extends RecyclerView.Adapter<GalleryAdopter.ViewHolder> {

        File[] files;

        GalleryAdopter(File[] files){
            this.files = files;
        }

        @NonNull
        @Override
        public GalleryAdopter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
            View view = inflater.inflate(R.layout.gallery_list, parent, false) ;
            return new GalleryAdopter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryAdopter.ViewHolder holder, int position) {
            File file = files[position];
            try {
                FileInputStream fis = new FileInputStream(file);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
                Glide.with(CameraActivity.this).load(bitmap).into(holder.imageView);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int getItemCount() {
            return files.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image);
            }
        }
    }

    public class MyListDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

            if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.right = 30;
            }
        }
    }


}