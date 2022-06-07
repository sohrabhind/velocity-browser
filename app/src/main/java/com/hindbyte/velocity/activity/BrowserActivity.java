package com.hindbyte.velocity.activity;

import static com.hindbyte.velocity.util.Variables.firstStart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.hindbyte.velocity.R;
import com.hindbyte.velocity.fragment.BrowserInterface;
import com.hindbyte.velocity.fragment.EditTextFragment;
import com.hindbyte.velocity.fragment.HistoryFragment;
import com.hindbyte.velocity.home.HomeAdapter;
import com.hindbyte.velocity.home.HomeData;
import com.hindbyte.velocity.home.HomeModel;
import com.hindbyte.velocity.intent.ClearData;
import com.hindbyte.velocity.tabs.TabListManager;
import com.hindbyte.velocity.tabs.TabManager;
import com.hindbyte.velocity.util.AnimatedProgressBar;
import com.hindbyte.velocity.util.ToastWindow;
import com.hindbyte.velocity.util.URLUtils;
import com.hindbyte.velocity.view.DialogAdapter;
import com.hindbyte.velocity.webview.MyDownloadListener;
import com.hindbyte.velocity.webview.MyWebView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class BrowserActivity extends AppCompatActivity implements BrowserInterface {

    public TabManager currentTabManager;
    private boolean quit = false;
    private boolean close = false;
    public TextView inputBox;
    private ScrollView switcherScroller;
    RelativeLayout mainBox;
    Button addTab;
    ImageView moreMenu;
    HomeAdapter homeAdapter;
    SharedPreferences sp;
    SharedPreferences pref;
    RecyclerView recyclerView;
    AdView adView;
    AdRequest adRequest;
    LinearLayout mainView;
    LinearLayout switcherContainer;
    LinearLayout findInPagePanel;
    EditText findInPageInputBox;
    TextView tabMenu;
    AnimatedProgressBar progressBar;
    Bitmap mDefaultVideoPoster;
    FrameLayout contentFrame;
    NestedScrollView homeShortcuts;
    final Handler handler = new Handler(Looper.getMainLooper());
    boolean startFindPanel = false;
    HomeData homeDB;
    TabListManager tabListManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        MobileAds.initialize(this, initializationStatus -> {
        });
        mainView = findViewById(R.id.main_view);
        contentFrame = findViewById(R.id.main_content);
        homeShortcuts = findViewById(R.id.home_shortcuts);
        switcherScroller = findViewById(R.id.switcher_scroller);
        switcherContainer = findViewById(R.id.switcher_container);
        mainBox = findViewById(R.id.main_input_box);
        inputBox = findViewById(R.id.main_input_box_input);
        tabMenu = findViewById(R.id.tabMenu);
        progressBar = findViewById(R.id.main_progress_bar);
        addTab = findViewById(R.id.add_tab);
        moreMenu = findViewById(R.id.more);
        adView = findViewById(R.id.adView);
        recyclerView = findViewById(R.id.recyclerView);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        pref = getSharedPreferences("MY_PREF", 0);
        firstStart = false;
        adRequest = new AdRequest.Builder().build();
        inflater = (LayoutInflater) BrowserActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customMenu = inflater.inflate(R.layout.menu_layout, findViewById(R.id.layout123));
        actionRefresh = customMenu.findViewById(R.id.action_refresh);
        actionCancel = customMenu.findViewById(R.id.action_cancel);

        tabListManager = new TabListManager();
        onNewIntent(getIntent());

        homeDB = new HomeData(BrowserActivity.this);
        initHome();

        if (tabListManager.getSize() < 1) {
            addTab("about:blank", true);
        }
        initToolbar();
    }

    LayoutInflater inflater;
    View customMenu;
    ImageView actionRefresh;
    ImageView actionCancel;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_WEB_SEARCH)) {
            addTab(intent.getStringExtra(SearchManager.QUERY), true);
        } else if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            Uri data = intent.getData();
            assert data != null;
            String link = data.toString();
            addTab(link, true);
        } else if (filePathCallback != null) {
            filePathCallback = null;
            getIntent().setAction("");
        }
    }

        @Override
    public void onResume() {
        super.onResume();
        onNewIntent(getIntent());
        MyWebView myWebView = (MyWebView) currentTabManager;
        myWebView.initPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        MyWebView myWebView = (MyWebView) currentTabManager;
        myWebView.pauseTimers();
        myWebView.onPause();
        myWebView.onResume();
        myWebView.resumeTimers();
        if (getCurrentFocus() != null) {
            hideSoftInput(getCurrentFocus());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeApp();
    }

    public void closeApp() {
        tabListManager.clear();
        boolean clear_data_on_exit = sp.getBoolean("SP_CLEAR_DATA_EXIT", false);
        if (clear_data_on_exit) {
            ClearData clearData = new ClearData(this);
            clearData.clearCaches();
        }
        finishAffinity();
    }

    public void onItemClick(int position) {
        List<HomeModel> itemList = homeDB.getItemDetails();
        final HomeModel item = itemList.get(position);
        updateTab(item.getUrl());
    }

    public void onItemLongClick(int position) {
        final List<String> list = new ArrayList<>();
        list.add("Edit");
        list.add("Delete");

        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.setCancelable(true);
        builder.setContentView(R.layout.dialog_list);
        ListView listView = builder.findViewById(R.id.dialog_list);
        DialogAdapter adapter = new DialogAdapter(this, R.layout.dialog_item, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        builder.show();

        listView.setOnItemClickListener((parent, view, position1, id) -> {
            List<HomeModel> itemList = homeDB.getItemDetails();
            final HomeModel item = itemList.get(position);
            String s = list.get(position1);
            switch (s) {
                case "Edit":
                    AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                    builder3.setCancelable(true);
                    @SuppressLint("InflateParams") LinearLayout signInLayout2 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_add_to_home, null, false);
                    final EditText userEdit2 = signInLayout2.findViewById(R.id.dialog_sign_in_username);
                    userEdit2.setText(item.getTitle());
                    final EditText passEdit2 = signInLayout2.findViewById(R.id.dialog_sign_in_password);
                    passEdit2.setText(item.getUrl());
                    builder3.setView(signInLayout2);
                    builder3.setPositiveButton(R.string.dialog_button_positive, (dialog2, which) -> {
                        String user = userEdit2.getText().toString().trim();
                        String pass = passEdit2.getText().toString().trim();
                        item.setTitle(user);
                        item.setUrl(pass);
                        homeDB.updateItem(item);
                        initHome();
                    });
                    builder3.setNegativeButton(R.string.dialog_button_negative, (dialog2, which) -> dialog2.dismiss());
                    builder3.create().show();
                    break;
                case "Delete":
                    homeDB.deleteItem(item.getId());
                    initHome();
                    break;
            }
            builder.dismiss();
        });
    }

    private void initHome() {
        List<HomeModel> itemList = homeDB.getItemDetails();
        int numberOfColumns = 4;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        homeAdapter = new HomeAdapter(this, itemList);
        recyclerView.setAdapter(homeAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }


    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n", "AddJavascriptInterface"})
    public void initiatePopupWindow() {
        try {
            PopupWindow pw = new PopupWindow(customMenu, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            pw.showAsDropDown(inputBox, 0, 0);//y = inputBox.getHeight()

            pw.setOutsideTouchable(true);
            pw.setFocusable(true);
            pw.getContentView().setFocusableInTouchMode(true);

            customMenu.setOnTouchListener((v1, event) -> {
                pw.dismiss();
                return true;
            });

            pw.getContentView().setOnKeyListener((v1, keyCode, event) -> {
                pw.dismiss();
                return true;
            });

            MyWebView myWebView = (MyWebView) currentTabManager;
            ImageView actionHome = customMenu.findViewById(R.id.action_home);
            ImageView actionHistory = customMenu.findViewById(R.id.action_history);
            ImageView actionSettings = customMenu.findViewById(R.id.action_settings);
            TextView actionForward = customMenu.findViewById(R.id.action_forward);
            TextView actionPrivate = customMenu.findViewById(R.id.action_private);
            TextView actionDesktop = customMenu.findViewById(R.id.action_desktop);
            TextView actionAddToHome = customMenu.findViewById(R.id.action_add_to_home);
            TextView actionShareUrl = customMenu.findViewById(R.id.action_share_url);
            TextView actionFind = customMenu.findViewById(R.id.action_find);
            LinearLayout actionMovePage = customMenu.findViewById(R.id.action_move_page);
            ImageView actionUp = customMenu.findViewById(R.id.action_up);
            ImageView actionDown = customMenu.findViewById(R.id.action_down);
            TextView actionExit = customMenu.findViewById(R.id.action_exit);
            TextView actionSource = customMenu.findViewById(R.id.action_view_source);

            actionCancel.setOnClickListener(v -> {
                pw.dismiss();
                myWebView.stopLoading();
            });

            actionHistory.setOnClickListener(view -> {
                pw.dismiss();
                Fragment historyFragment = new HistoryFragment(this);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.mainFrame, historyFragment);
                fragmentTransaction.commit();
            });

            actionAddToHome.setOnClickListener(view -> {
                pw.dismiss();
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setCancelable(true);
                @SuppressLint("InflateParams") LinearLayout signInLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_add_to_home, null, false);
                final EditText userEdit = signInLayout.findViewById(R.id.dialog_sign_in_username);
                final EditText passEdit = signInLayout.findViewById(R.id.dialog_sign_in_password);
                if (myWebView.getUrl() != null && myWebView.getUrl().equals("about:blank")) {
                    userEdit.setText("");
                    passEdit.setText("");
                } else {
                    if (myWebView.getTitle() != null) {
                        userEdit.setText(myWebView.getTitle());
                    }
                    if (myWebView.getUrl() != null) {
                        passEdit.setText(myWebView.getUrl());
                    }
                }
                builder2.setView(signInLayout);
                builder2.setPositiveButton(R.string.dialog_button_positive, (dialog2, which) -> {
                    String user = userEdit.getText().toString().trim();
                    String pass = passEdit.getText().toString().trim();
                    homeDB.addItem(user, pass);
                    initHome();
                });
                builder2.setNegativeButton(R.string.dialog_button_negative, (dialog2, which) -> dialog2.dismiss());
                builder2.create().show();
            });


            actionHome.setOnClickListener(view -> {
                pw.dismiss();
                myWebView.loadUrl("about:blank");
            });

            if (myWebView.canGoForward()) {
                actionForward.setVisibility(View.VISIBLE);
                actionForward.setOnClickListener(view -> {
                    pw.dismiss();
                    if (myWebView.getUrl() == null) {
                        contentFrame.setVisibility(View.VISIBLE);
                        homeShortcuts.setVisibility(View.GONE);
                        //adView.setVisibility(View.GONE);
                    }
                    myWebView.stopLoading();
                    myWebView.goForward();
                });
            } else {
                actionForward.setVisibility(View.GONE);
            }
            if (myWebView.getUrl() != null && myWebView.getUrl().equals("about:blank")) {
                actionFind.setVisibility(View.GONE);
                actionMovePage.setVisibility(View.GONE);
                actionSource.setVisibility(View.GONE);
                actionShareUrl.setText("Share app");
                actionShareUrl.setOnClickListener(view -> {
                    pw.dismiss();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Download Velocity Browser.\nhttps://play.google.com/store/apps/details?id="+ getPackageName());
                    startActivity(Intent.createChooser(sendIntent, "Share via"));
                });
            } else {
                actionFind.setVisibility(View.VISIBLE);
                actionMovePage.setVisibility(View.VISIBLE);
                actionShareUrl.setText("Share page");

                if (myWebView.getUrl() != null && !myWebView.getUrl().equals("file:///android_asset/source.html")) {
                    actionSource.setVisibility(View.VISIBLE);
                    actionSource.setOnClickListener(view -> {
                        pw.dismiss();
                        String command = "(document.documentElement.outerHTML)";
                        myWebView.evaluateJavascript("(function() { return " + command + "; })();", html -> {
                            MyWebView sourceView = new MyWebView(this, this);
                            sourceView.loadDataWithBaseURL("file:///android_asset/source.html", decode(html),  "text",null, "file:///android_asset/source.html");
                            addTab(null, true, sourceView);
                        });
                    });
                } else {
                    actionSource.setVisibility(View.GONE);
                }

                actionFind.setOnClickListener(view -> {
                    pw.dismiss();
                    if (currentTabManager instanceof MyWebView) {
                        if (!startFindPanel) {
                            initFindInPage();
                            startFindPanel = true;
                        }
                        showSearchPanel();
                    }
                });

                actionUp.setOnClickListener(view -> {
                    pw.dismiss();
                    myWebView.pageUp(true);
                });

                actionDown.setOnClickListener(view -> {
                    pw.dismiss();
                    myWebView.pageDown(true);
                });

                actionShareUrl.setOnClickListener(view -> {
                    pw.dismiss();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, myWebView.getUrl());
                    startActivity(Intent.createChooser(shareIntent, "Share Link"));
                });
            }

            if (pref.getBoolean("key_private_mode", false)) {
                actionPrivate.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_box_black_24dp, null), null, null, null);
                actionPrivate.setOnClickListener(view -> {
                    pw.dismiss();
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("key_private_mode", false);
                    editor.apply();
                    myWebView.initPreferences();
                });
            } else {
                actionPrivate.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_box_outline_blank_black_24dp, null), null, null, null);
                actionPrivate.setOnClickListener(view -> {
                    pw.dismiss();
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("key_private_mode", true);
                    editor.apply();
                    myWebView.initPreferences();
                });
            }

            if (pref.getBoolean("key_desktop_mode", false)) {
                actionDesktop.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_box_black_24dp, null), null, null, null);
                actionDesktop.setOnClickListener(view -> {
                    pw.dismiss();
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("key_desktop_mode", false);
                    editor.apply();
                    myWebView.initPreferences();
                    myWebView.reload();
                });
            } else {
                actionDesktop.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_check_box_outline_blank_black_24dp, null), null, null, null);
                actionDesktop.setOnClickListener(view -> {
                    pw.dismiss();
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("key_desktop_mode", true);
                    editor.apply();
                    myWebView.initPreferences();
                    myWebView.reload();
                });
            }

            actionSettings.setOnClickListener(view -> {
                pw.dismiss();
                Intent intent = new Intent(BrowserActivity.this, SettingActivity.class);
                startActivity(intent);
            });

            actionExit.setOnClickListener(view -> {
                pw.dismiss();
                closeApp();
            });

            actionRefresh.setOnClickListener(view -> {
                pw.dismiss();
                myWebView.stopLoading();
                myWebView.loadUrl(myWebView.getUrl());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String decode(String escaped) {
        escaped = escaped.replaceAll("^.|.$", "");
        StringBuilder processed= new StringBuilder();
        int position=escaped.indexOf("\\u");
        while(position!=-1) {
            if(position!=0) {
                processed.append(escaped.substring(0, position));
            }
            String token=escaped.substring(position+2,position+6);
            escaped=escaped.substring(position+6);
            processed.append((char) Integer.parseInt(token, 16));
            position=escaped.indexOf("\\u");
        }
        processed.append(escaped);
        return processed.toString().replace("\\n", "\n").replace("\\\"", "\"");
    }


    public void initToolbar() {
        moreMenu.setOnClickListener(view -> initiatePopupWindow());
        inputBox.setOnClickListener(view -> {
            MyWebView myWebView = (MyWebView) currentTabManager;
            String inputBoxUrl = "";
            if (myWebView.getUrl() != null && !myWebView.getUrl().equals("about:blank")) {
                inputBoxUrl = myWebView.getUrl();
            }
            Fragment homeFragment = new EditTextFragment(inputBoxUrl, this);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.mainFrame, homeFragment);
            fragmentTransaction.commit();
        });

        addTab.setOnClickListener(v -> {
            mainView.setVisibility(View.VISIBLE);
            addTab("about:blank", true);
        });

        tabMenu.setOnClickListener(v -> mainView.setVisibility(View.GONE));
    }

    private void initFindInPage() {
        findInPagePanel = findViewById(R.id.layout_findInPage);
        findInPageInputBox = findViewById(R.id.input_box_findInPage);
        ImageView findInPre = findViewById(R.id.pre_findInPage);
        ImageView findInPageNext = findViewById(R.id.next_findInPage);
        ImageView findInPageCancel = findViewById(R.id.cancel_findInPage);

        findInPageInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (currentTabManager instanceof MyWebView) {
                    ((MyWebView) currentTabManager).findAllAsync(s.toString());
                }
            }
        });

        findInPre.setOnClickListener(v -> {
            hideSoftInput(findInPageInputBox);
            if (currentTabManager instanceof MyWebView) {
                ((MyWebView) currentTabManager).findNext(false);
            }
        });

        findInPageNext.setOnClickListener(v -> {
            hideSoftInput(findInPageInputBox);
            if (currentTabManager instanceof MyWebView) {
                ((MyWebView) currentTabManager).findNext(true);
            }
        });

        findInPageCancel.setOnClickListener(v ->
                hideSearchPanel());
    }

    private void hideSearchPanel() {
        hideSoftInput(findInPageInputBox);
        findInPageInputBox.setText("");
        findInPagePanel.setVisibility(View.GONE);
        mainBox.setVisibility(View.VISIBLE);
    }

    private void showSearchPanel() {
        mainBox.setVisibility(View.GONE);
        findInPagePanel.setVisibility(View.VISIBLE);
        showSoftInput(findInPageInputBox);
    }

    @SuppressLint("SetTextI18n")
    public void updateInputBox(String query) {
        if (query != null && !query.equals("about:blank")) {
            URLUtils urlUtils = new URLUtils();
            inputBox.setText(Html.fromHtml(urlUtils.urlWrapper(query)), EditText.BufferType.SPANNABLE);
            contentFrame.setVisibility(View.VISIBLE);
            homeShortcuts.setVisibility(View.GONE);
            //adView.setVisibility(View.GONE);
        } else {
            inputBox.setText("");
            contentFrame.setVisibility(View.GONE);
            homeShortcuts.setVisibility(View.VISIBLE);
            //adView.setVisibility(View.VISIBLE);
        }
    }

    public void updateProgress(int progress) {
        MyWebView myWebView = (MyWebView) currentTabManager;
        if (myWebView.getUrl() != null && myWebView.getUrl().equals("about:blank")) {
            progress = 100;
            progressBar.setVisibilityImmediately(View.GONE);
        } else {
            progressBar.setProgress(progress);
            progressBar.setVisibility(progress == 100 ? View.GONE : View.VISIBLE);
        }
        actionCancel.setVisibility(progress == 100 ? View.GONE : View.VISIBLE);
        actionRefresh.setVisibility(progress == 100 ? View.VISIBLE : View.GONE);
    }

    public synchronized void addTab(String url, boolean expand) {
        addTab(url, expand, null);
    }

    public synchronized void addTab(String url, boolean expand, MyWebView webView) {
        if (url != null) {
            webView = new MyWebView(this, this);
            webView.loadUrl(url);
        } else if (webView == null) {
            return;
        }
        View tabView = webView.getTabView();
        if (expand) {
            tabListManager.add(webView);
            contentFrame.addView(webView);
            showTab(webView, true);
            switcherContainer.addView(tabView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            Animation in  = AnimationUtils.loadAnimation(BrowserActivity.this, R.anim.zoom_in);
            tabMenu.startAnimation(in);
            handler.postDelayed(() -> {
                Animation out = AnimationUtils.loadAnimation(BrowserActivity.this, R.anim.zoom_out);
                tabMenu.startAnimation(out);
                switcherScroller.smoothScrollTo(0, currentTabManager.getTabView().getTop());
            }, 200);
        }  else {
            tabListManager.add(webView);
            contentFrame.addView(webView);
            contentFrame.bringChildToFront((View) currentTabManager);
            switcherContainer.addView(tabView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }
        tabMenu.setText(String.valueOf(tabListManager.getSize()));
    }

    public synchronized void showTab(TabManager controller, boolean expand) {
        if (controller == null || controller == currentTabManager) {
            mainView.setVisibility(View.VISIBLE);
            return;
        }
        if (currentTabManager != null) {
            currentTabManager.deactivate();
        }
        progressBar.setVisibilityImmediately(View.GONE);
        actionCancel.setVisibility(View.GONE);
        actionRefresh.setVisibility(View.VISIBLE);
        contentFrame.bringChildToFront((View) controller);
        currentTabManager = controller;
        controller.activate();
        MyWebView myWebView = (MyWebView) currentTabManager;
        if (myWebView.getUrl() != null && myWebView.getUrl().equals("about:blank")) {
            adView.loadAd(adRequest);
        }

        updateInputBox(myWebView.getUrl());
        if (expand) {
            mainView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateTab(String url) {
        updateInputBox(url);
        MyWebView myWebView = (MyWebView) currentTabManager;
        myWebView.loadUrl(url);
    }



    public synchronized void removeTab(TabManager controller) {
        if (currentTabManager == null || tabListManager.getSize() <= 1) {
            contentFrame.removeView((View) controller);
            switcherContainer.removeView(controller.getTabView());
            tabListManager.remove(controller);
            addTab("about:blank", true);
        } else if (controller != currentTabManager) {
            contentFrame.removeView((View) controller);
            switcherContainer.removeView(controller.getTabView());
            tabListManager.remove(controller);
        } else {
            contentFrame.removeView((View) controller);
            switcherContainer.removeView(controller.getTabView());
            int index = tabListManager.getItemPosition(controller);
            tabListManager.remove(controller);
            if (index == 0) {
                index = 1;
            }
            showTab(tabListManager.getController(index - 1), false);
            Animation in  = AnimationUtils.loadAnimation(BrowserActivity.this, R.anim.zoom_in);
            tabMenu.startAnimation(in);
            handler.postDelayed(() -> {
                Animation out = AnimationUtils.loadAnimation(BrowserActivity.this, R.anim.zoom_out);
                tabMenu.startAnimation(out);
            }, 200);
        }
        tabMenu.setText(String.valueOf(tabListManager.getSize()));
    }


    private ValueCallback<Uri[]> filePathCallback = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    public final int INPUT_FILE_REQUEST_CODE = 45738;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, intent);
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                if (dataString != null) results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
    }

    public void showFileChooser(ValueCallback<Uri[]> filePathCallback) {
        if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
        mFilePathCallback = filePathCallback;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        //noinspection deprecation
        startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
    }


    public Bitmap getDefaultVideoPoster() {
        if (mDefaultVideoPoster == null) {
            mDefaultVideoPoster = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo);
        }
        return mDefaultVideoPoster;
    }

    private void setCustomFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        }
        getWindow().setAttributes(attrs);
    }

    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;
    public void onHideCustomView() {
        ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
        this.mCustomView = null;
        getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
        setRequestedOrientation(this.mOriginalOrientation);
        this.mCustomViewCallback.onCustomViewHidden();
        this.mCustomViewCallback = null;
        mainBox.setVisibility(View.VISIBLE);
        setCustomFullscreen(false);
    }

    public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
        if (this.mCustomView != null) {
            onHideCustomView();
            return;
        }
        mainBox.setVisibility(View.GONE);
        setCustomFullscreen(true);
        this.mCustomView = paramView;
        this.mOriginalSystemUiVisibility = this.getWindow().getDecorView().getSystemUiVisibility();
        this.mOriginalOrientation = getRequestedOrientation();
        this.mCustomViewCallback = paramCustomViewCallback;
        ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
        getWindow().getDecorView().setSystemUiVisibility(View.GONE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }

    public void onLongPress(String url) {
        WebView.HitTestResult result;
        if (!(currentTabManager instanceof MyWebView)) {
            return;
        }
        result = ((MyWebView) currentTabManager).getHitTestResult();
        final List<String> list = new ArrayList<>();
        list.add(getString(R.string.main_menu_new_tab));
        list.add(getString(R.string.main_menu_new_back));
        if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            list.add(getString(R.string.main_menu_copy_link));
        }
        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            list.add(getString(R.string.main_menu_copy_image_link));
        }
        if (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            list.add(getString(R.string.main_menu_save));
        }

        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.setCancelable(true);
        builder.setContentView(R.layout.dialog_list);
        ListView listView = builder.findViewById(R.id.dialog_list);
        DialogAdapter adapter = new DialogAdapter(this, R.layout.dialog_item, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (url != null || result.getExtra() != null) {
            if (url == null) {
                url = result.getExtra();
            }
            builder.show();
        }
        String mimeType = "image/*";
        String finalUrl = url;
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String s = list.get(position);
            if (s.equals(getString(R.string.main_menu_new_tab))) {
                addTab(finalUrl, true);
            } else if (s.equals(getString(R.string.main_menu_new_back))) {
                addTab(finalUrl, false);
            } else if (s.equals(getString(R.string.main_menu_copy_link))) {
                setClipboardText(BrowserActivity.this, finalUrl);
            } else if (s.equals(getString(R.string.main_menu_copy_image_link))) {
                if (result.getExtra() != null) {
                    setClipboardText(BrowserActivity.this, result.getExtra());
                } else {
                    setClipboardText(BrowserActivity.this, finalUrl);
                }
            } else if (s.equals(getString(R.string.main_menu_save))) {
                MyDownloadListener myDownloadListener = new MyDownloadListener(this, this);
                if (result.getExtra() != null) {
                    myDownloadListener.download(result.getExtra(), null, mimeType);
                } else {
                    myDownloadListener.download(finalUrl, null, mimeType);
                }
            }
            builder.dismiss();
        });
    }

    public String url;
    public String contentDisposition;
    public String mimeType;

    public void setStrings(String url, String contentDisposition, String mimeType) {
        this.url = url;
        this.contentDisposition = contentDisposition;
        this.mimeType = mimeType;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 777) {
            String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                MyDownloadListener myDownloadListener = new MyDownloadListener(this, this);
                myDownloadListener.download(url, contentDisposition, mimeType);
            }
        }
    }

    public void setClipboardText(Context context, CharSequence txt) {
        if (txt != null) {
            ClipData.Item clipItem = new ClipData.Item(txt);
            String[] mineType = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipData clipData = new ClipData("text_data", mineType, clipItem);
            ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (manager != null) {
                manager.setPrimaryClip(clipData);
            }
        }
    }

    private void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideSoftInput(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void doubleTapsQuit() {
        ToastWindow toastWindow = new ToastWindow();
        if (!quit) {
            toastWindow.makeText(this, "Press again to exit", 2000);
            quit = true;
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    quit = false;
                    timer.cancel();
                }
            }, 2000);
        } else {
            closeApp();
        }
    }

    private void doubleTapsToClose() {
        if (!close) {
            ToastWindow toastWindow = new ToastWindow();
            toastWindow.makeText(this, "Press again to close tab", 2000);
            close = true;
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    close = false;
                    timer.cancel();
                }
            }, 2000);
        } else {
            removeTab(currentTabManager);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && onKeyCodeBack();
    }

    private boolean onKeyCodeBack() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainFrame);
        if (currentFragment instanceof EditTextFragment) {
            EditTextFragment fragment = (EditTextFragment) getSupportFragmentManager().findFragmentById(R.id.mainFrame);
            Objects.requireNonNull(fragment).onBackPress();
        } else if (currentFragment instanceof HistoryFragment) {
            HistoryFragment fragment = (HistoryFragment) getSupportFragmentManager().findFragmentById(R.id.mainFrame);
            Objects.requireNonNull(fragment).onBackPress();
        } else if (mCustomView != null) {
            onHideCustomView();
        } else if (startFindPanel && findInPagePanel.getVisibility() == View.VISIBLE) {
            hideSearchPanel();
        } else if (mainView.getVisibility() == View.GONE) {
            mainView.setVisibility(View.VISIBLE);
        } else {
            MyWebView myWebView = (MyWebView) currentTabManager;
            if (myWebView.canGoBack()) {
                myWebView.stopLoading();
                myWebView.goBack();
            } else if (tabListManager.getSize() > 1 && myWebView.getProgress() == 100) {
                doubleTapsToClose();
            } else if (myWebView.getProgress() == 100){
                doubleTapsQuit();
            }
        }
        return true;
    }

}