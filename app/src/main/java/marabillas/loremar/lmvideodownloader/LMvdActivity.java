/*
 *     LM videodownloader is a browser app for android, made to easily
 *     download videos.
 *     Copyright (C) 2018 Loremar Marabillas
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package marabillas.loremar.lmvideodownloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import marabillas.loremar.lmvideodownloader.bookmarks_feature.Bookmarks;
import marabillas.loremar.lmvideodownloader.browsing_feature.BrowserManager;
import marabillas.loremar.lmvideodownloader.download_feature.fragments.Downloads;
import marabillas.loremar.lmvideodownloader.history_feature.History;
import marabillas.loremar.lmvideodownloader.utils.Utils;

public class LMvdActivity extends Activity implements TextView.OnEditorActionListener, View.OnClickListener, AdapterView.OnItemClickListener {
    private EditText webBox;
    private BrowserManager browserManager;
    private Uri appLinkData;
    private DrawerLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        webBox = findViewById(R.id.web);
        webBox.setOnEditorActionListener(this);

        ImageButton go = findViewById(R.id.go);
        go.setOnClickListener(this);

        if ((browserManager = (BrowserManager) getFragmentManager().findFragmentByTag("BM")) == null) {
            getFragmentManager().beginTransaction().add(browserManager = new BrowserManager(),
                    "BM").commit();
        }

        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        //String appLinkAction = appLinkIntent.getAction();
        appLinkData = appLinkIntent.getData();

        layout = findViewById(R.id.drawer);
        ImageView menu = findViewById(R.id.menuButton);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(Gravity.START);
            }
        });

        ListView listView = findViewById(R.id.menu);
        String[] menuItems = new String[]{"Home", "Browser", "Downloads", "Bookmarks",
                "History", "About"};
        ArrayAdapter listAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_list_item_1, menuItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        };
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        Switch adBlockerSwitch = findViewById(R.id.adBlockerSwitch);
        final SharedPreferences prefs = getSharedPreferences("settings", 0);
        boolean adBlockOn = prefs.getBoolean(getString(R.string.adBlockON), true);
        adBlockerSwitch.setChecked(adBlockOn);
        adBlockerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(getString(R.string.adBlockON), isChecked).apply();
            }
        });

        RecyclerView videoSites = findViewById(R.id.homeSites);
        videoSites.setAdapter(new VideoStreamingSitesList(this));
        videoSites.setLayoutManager(new LinearLayoutManager(this));


        Intent intent = getIntent();

        // Get the action of the intent
        String action = intent.getAction();
        // Get the type of intent (Text or Image)

        String type = intent.getType();

        // When Intent's action is 'ACTION+SEND' and Tyoe is not null

        if (Intent.ACTION_SEND.equals(action) && type != null) {

            // When tyoe is 'text/plain'

            if ("text/plain".equals(type)) {

                handleSendText(intent); // Handle text being sent

            }

        }


    }


    private void handleSendText(Intent intent) {
        // Get the text from intent
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);

        // When Text is not null
        if (sharedText != null) {

            // Show the text as Toast message
            Toast.makeText(this, sharedText, Toast.LENGTH_LONG).show();
            webBox.setText(sharedText);
        }

    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        new WebConnect(webBox, this).connect();
        return false;
    }

    @Override
    public void onBackPressed() {
        Fragment sourcePage = getFragmentManager().findFragmentByTag("updateSourcePage");
        if (sourcePage != null) {
            getFragmentManager().beginTransaction().remove(sourcePage).commit();
        } else if (layout.isDrawerVisible(Gravity.START)) {
            layout.closeDrawer(Gravity.START);
        } else if (LMvdApp.getInstance().getOnBackPressedListener() != null) {
            LMvdApp.getInstance().getOnBackPressedListener().onBackpressed();
        } else super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (getCurrentFocus() != null) {
            Utils.hideSoftKeyboard(this, getCurrentFocus().getWindowToken());
            new WebConnect(webBox, this).connect();
        }
    }

    private void closeDownloads() {
        Fragment fragment = getFragmentManager().findFragmentByTag("Downloads");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void closeBookmarks() {
        Fragment fragment = getFragmentManager().findFragmentByTag("Bookmarks");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void closeHistory() {
        Fragment fragment = getFragmentManager().findFragmentByTag("History");
        if (fragment != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void homeClicked() {
        browserManager.hideCurrentWindow();
        closeDownloads();
        closeBookmarks();
        closeHistory();
        setOnBackPressedListener(null);
    }

    public void browserClicked() {
        browserManager.unhideCurrentWindow();
        closeDownloads();
        closeBookmarks();
        closeHistory();
    }

    private void downloadsClicked() {
        closeBookmarks();
        closeHistory();
        if (getFragmentManager().findFragmentByTag("Downloads") == null) {
            browserManager.hideCurrentWindow();
            getFragmentManager().beginTransaction().add(R.id.main, new Downloads(),
                    "Downloads").commit();
        }
    }

    private void bookmarksClicked() {
        closeDownloads();
        closeHistory();
        if (getFragmentManager().findFragmentByTag("Bookmarks") == null) {
            browserManager.hideCurrentWindow();
            getFragmentManager().beginTransaction().add(R.id.main, new Bookmarks(), "Bookmarks")
                    .commit();
        }
    }

    private void historyClicked() {
        closeDownloads();
        closeBookmarks();
        if (getFragmentManager().findFragmentByTag("History") == null) {
            browserManager.hideCurrentWindow();
            getFragmentManager().beginTransaction().add(R.id.main, new History(), "History")
                    .commit();
        }
    }

    private void aboutClicked() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.about, ((ViewGroup) this.getWindow().getDecorView()), false);
        new AlertDialog.Builder(this)
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        layout.closeDrawers();
        switch (position) {
            case 0:
                homeClicked();
                break;
            case 1:
                browserClicked();
                break;
            case 2:
                downloadsClicked();
                break;
            case 3:
                bookmarksClicked();
                break;
            case 4:
                historyClicked();
                break;
            case 5:
                aboutClicked();
                break;
        }
    }

    public interface OnBackPressedListener {
        void onBackpressed();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        LMvdApp.getInstance().setOnBackPressedListener(onBackPressedListener);
    }

    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (appLinkData != null) {
            browserManager.newWindow(appLinkData.toString());
        }
        browserManager.updateAdFilters();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResultCallback.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
    }

    private ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback;

    public void setOnRequestPermissionsResultListener(ActivityCompat
                                                              .OnRequestPermissionsResultCallback
                                                              onRequestPermissionsResultCallback) {
        this.onRequestPermissionsResultCallback = onRequestPermissionsResultCallback;
    }

}
