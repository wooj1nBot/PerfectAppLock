package com.release.perfectapplock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.IconCompat;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import in.myinnos.library.AppIconNameChanger;

public class IconActivity extends AppCompatActivity {

    private MaterialCardView[] cardViews;
    private TextView[] textViews;
    public static final String[] names = {"com.release.perfectapplock.IconActivityDefault", "com.release.perfectapplock.IconActivityMaps",
            "com.release.perfectapplock.IconActivityCloud", "com.release.perfectapplock.IconActivityVideo"};

    public static final String[] appTitles = {"Perfect Applock", "PA Maps",
            "PA Cloud", "PA Video"};
    public static final int[] resIDs = {R.drawable.logo, R.drawable.pa_maps, R.drawable.pa_cloud, R.drawable.pa_video};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon);

        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ImageView more = findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupMenu = new PopupMenu(getApplicationContext(),view);
                getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            createShortCut();
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        cardViews = new MaterialCardView[4];
        textViews = new TextView[4];

        cardViews[0] = findViewById(R.id.ok_1);
        cardViews[1] = findViewById(R.id.ok_2);
        cardViews[2] = findViewById(R.id.ok_3);
        cardViews[3] = findViewById(R.id.ok_4);

        textViews[0] = findViewById(R.id.tv_1);
        textViews[1] = findViewById(R.id.tv_2);
        textViews[2] = findViewById(R.id.tv_3);
        textViews[3] = findViewById(R.id.tv_4);

        clear();

        int m = Util.getLauncher(IconActivity.this);
        cardViews[m].setStrokeColor(Color.parseColor("#186599"));
        cardViews[m].setCardBackgroundColor(Color.WHITE);
        cardViews[m].setStrokeWidth(2);
        textViews[m].setText(R.string.used);
        textViews[m].setTextColor(Color.parseColor("#186599"));


        for(int i = 0; i < 4; i++){
            cardViews[i].setTag(i);
            cardViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int tag = (int) view.getTag();
                    clear();
                    cardViews[tag].setStrokeColor(Color.parseColor("#186599"));
                    cardViews[tag].setCardBackgroundColor(Color.WHITE);
                    cardViews[tag].setStrokeWidth(2);
                    textViews[tag].setText(R.string.used);
                    textViews[tag].setTextColor(Color.parseColor("#186599"));
                    change(tag);
                }
            });
        }
    }

    public void createShortCut() throws ClassNotFoundException {
        int m = Util.getLauncher(IconActivity.this);
        String activeName = names[m];

        ShortcutManager shortcutManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager = getSystemService(ShortcutManager.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (shortcutManager.isRequestPinShortcutSupported()) {

                    shortcutManager.disableShortcuts(Collections.singletonList(Util.getShortCut(IconActivity.this)));
                    String name = "applock" + System.currentTimeMillis();
                    Util.setShortCut(IconActivity.this, name);
                    ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(getApplicationContext(), name)
                            .setShortLabel(appTitles[m])
                            .setLongLabel(appTitles[m])
                            .setIcon(Icon.createWithResource(IconActivity.this, resIDs[m]))
                            .setIntent(new Intent(IconActivity.this, getClassLoader().loadClass(activeName)).setAction(Intent.ACTION_MAIN)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    ).build();

                    shortcutManager.requestPinShortcut(pinShortcutInfo,
                            null);
                }
            }
        }






        //on Home screen
        Intent shortcutIntent = new Intent(this, getClassLoader().loadClass(activeName));
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appTitles[m]);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this,
                        resIDs[m]));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
    }

    public void change(int pos){
        ShortcutManager shortcutManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            shortcutManager = getSystemService(ShortcutManager.class);
            shortcutManager.disableShortcuts(Collections.singletonList(Util.getShortCut(IconActivity.this)));
        }

        String activeName = names[pos];

// Disable alias names
        List<String> disableNames = new ArrayList<String>(Arrays.asList(names));
        disableNames.remove(names[pos]);
        disableNames.add("com.release.perfectapplock.MainActivity");

// Initiate App Icon Name Changer
        new AppIconNameChanger.Builder(IconActivity.this)
                .activeName(activeName) // String
                .disableNames(disableNames) // List<String>
                .packageName(BuildConfig.APPLICATION_ID)
                .build()
                .setNow();

        Util.setLauncher(IconActivity.this, pos);

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    public void clear(){
        for(int i = 0; i < 4; i++){
            cardViews[i].setCardBackgroundColor(Color.parseColor("#7CA9C8"));
            cardViews[i].setStrokeWidth(0);
            textViews[i].setText(R.string.change);
            textViews[i].setTextColor(Color.WHITE);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}