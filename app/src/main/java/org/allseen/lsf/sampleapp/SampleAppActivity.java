/*
 * Copyright (c) AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.allseen.lsf.sampleapp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import org.allseen.lsf.sdk.AllLightingItemListener;
import org.allseen.lsf.sdk.Color;
import org.allseen.lsf.sdk.Controller;
import org.allseen.lsf.sdk.DeletableItem;
import org.allseen.lsf.sdk.ErrorCode;
import org.allseen.lsf.sdk.Group;
import org.allseen.lsf.sdk.Lamp;
import org.allseen.lsf.sdk.LampAbout;
import org.allseen.lsf.sdk.LightingController;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.LightingItem;
import org.allseen.lsf.sdk.LightingItemErrorEvent;
import org.allseen.lsf.sdk.LightingSystemQueue;
import org.allseen.lsf.sdk.MasterScene;
import org.allseen.lsf.sdk.MutableColorItem;
import org.allseen.lsf.sdk.Preset;
import org.allseen.lsf.sdk.PulseEffect;
import org.allseen.lsf.sdk.ResponseCode;
import org.allseen.lsf.sdk.Scene;
import org.allseen.lsf.sdk.SceneElement;
import org.allseen.lsf.sdk.SceneItem;
import org.allseen.lsf.sdk.SceneV1;
import org.allseen.lsf.sdk.SceneV2;
import org.allseen.lsf.sdk.TrackingID;
import org.allseen.lsf.sdk.TransitionEffect;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class SampleAppActivity extends FragmentActivity implements
        ActionBar.TabListener,
        PopupMenu.OnMenuItemClickListener,
        AllLightingItemListener {
    public static final String TAG = "LSFSampleApp";
    public static final String TAG_TRACE = "LSFSampleApp########";
    public static final String LANGUAGE = "en";

    public static final boolean ERROR_CODE_ENABLED = false; // enables all error dialogs
    public static final boolean ERROR_CODE_VERBOSE = true; // when set to false enables only dependency errors

    public static final long STATE_TRANSITION_DURATION = 100;
    public static final long FIELD_TRANSITION_DURATION = 0;
    public static final long FIELD_CHANGE_HOLDOFF = 25;

    private static final String CONTROLLER_ENABLED = "CONTROLLER_ENABLED_KEY";

    private static long lastFieldChangeMillis = 0;

    public enum Type {
        LAMP, GROUP, SCENE, ELEMENT
    }

    // Loosely coupled modules and plugins
    public BasicSceneV1ModuleProxy basicSceneV1Module;

    private SampleAppViewPager viewPager;
    public Handler handler;

    public volatile boolean isForeground;
    public volatile Queue<Runnable> runInForeground;

    private LightingController controllerService;
    private boolean controllerClientConnected;
    private boolean controllerServiceEnabled;
    private volatile boolean controllerServiceStarted;

    public PageFrameParentFragment pageFrameParent;

    private AlertDialog wifiDisconnectAlertDialog;
    private AlertDialog errorCodeAlertDialog;
    private String errorCodeAlertDialogMessage;

    private MenuItem addActionMenuItem;
    private MenuItem nextActionMenuItem;
    private MenuItem doneActionMenuItem;
    private MenuItem settingsActionMenuItem;

    private String popupItemID;
    private String popupSubItemID;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_app);

        toast = Toast.makeText(this, "", Toast.LENGTH_LONG);

        viewPager = (SampleAppViewPager) findViewById(R.id.sampleAppViewPager);
        viewPager.setActivity(this);

        handler = new Handler(Looper.getMainLooper());
        runInForeground = new LinkedList<Runnable>();

        // Setup localized strings in data models
        Controller.setDefaultName(this.getString(R.string.default_controller_name));

        LampAbout.setDataNotFound(this.getString(R.string.data_not_found));

        Lamp.setDefaultName(this.getString(R.string.default_lamp_name));
        Group.setDefaultName(this.getString(R.string.default_group_name));
        Preset.setDefaultName(this.getString(R.string.default_preset_name));
        TransitionEffect.setDefaultName(this.getString(R.string.default_transition_effect_name));
        PulseEffect.setDefaultName(this.getString(R.string.default_pulse_effect_name));
        SceneElement.setDefaultName(this.getString(R.string.default_scene_element_name));
        SceneV1.setDefaultName(this.getString(R.string.default_basic_scene_name));
        SceneV2.setDefaultName(this.getString(R.string.default_basic_scene_name));
        MasterScene.setDefaultName(this.getString(R.string.default_master_scene_name));

        // Start up the LightingSystemManager
        Log.d(SampleAppActivity.TAG, "===========================================");
        Log.d(SampleAppActivity.TAG, "Creating LightingSystemManager");

        LightingDirector.get().addListener(this);
        LightingDirector.get().start(
            "SampleApp",
            new LightingSystemQueue() {
                @Override
                public void post(Runnable r) {
                    handler.post(r);
                }

                @Override
                public void postDelayed(Runnable r, int delay) {
                    handler.postDelayed(r, delay);
                }

                @Override
                public void stop() {
                    // Currently nothing to do
                }
            });

        // Handle plugins and modules (optional features)
        // We initialize the dashboard plugin first to avoid an apparent
        // race condition with processing about announcements.
        initDashboard();
        basicSceneV1Module = new BasicSceneV1ModuleProxy();

        // Handle wifi connect and disconnect
        initWifiMonitoring();

        // Controller service support
        controllerServiceEnabled = getSharedPreferences("PREFS_READ", Context.MODE_PRIVATE).getBoolean(CONTROLLER_ENABLED, true);
        controllerService = LightingController.get();
        controllerService.init(new SampleAppControllerConfiguration(
                getApplicationContext().getFileStreamPath("").getAbsolutePath(), getApplicationContext()));
    }

    @Override
    protected void onDestroy() {
        LightingDirector.get().stop();

        setControllerServiceStarted(false);

        super.onDestroy();
    }

    public boolean isControllerServiceStarted() {
        return controllerServiceStarted;
    }

    private void setControllerServiceStarted(final boolean startControllerService) {
        if (controllerService != null) {
            if (startControllerService) {
                if (!controllerServiceStarted) {
                    controllerServiceStarted = true;
                    controllerService.start();
                }
            } else {
                controllerService.stop();
                controllerServiceStarted = false;
            }
        }
    }

    public boolean isControllerServiceEnabled() {
        return controllerServiceEnabled;
    }

    public void setControllerServiceEnabled(final boolean enableControllerService) {

        if (enableControllerService != controllerServiceStarted) {
            SharedPreferences prefs = getSharedPreferences("PREFS_READ", Context.MODE_PRIVATE);
            Editor e = prefs.edit();
            e.putBoolean(CONTROLLER_ENABLED, enableControllerService);
            e.commit();

            setControllerServiceStarted(enableControllerService);
        }

        controllerServiceEnabled = enableControllerService;
    }

    protected boolean isWifiConnected() {
        NetworkInfo wifiNetworkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // determine if wifi AP mode is on
        boolean isWifiApEnabled = false;
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // need reflection because wifi ap is not in the public API
        try {
            Method isWifiApEnabledMethod = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            isWifiApEnabled = (Boolean) isWifiApEnabledMethod.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(SampleAppActivity.TAG, "Connectivity state " + wifiNetworkInfo.getState().name() + " - connected:" + wifiNetworkInfo.isConnected() + " hotspot:" + isWifiApEnabled);

        return wifiNetworkInfo.isConnected() || isWifiApEnabled;
    }

    private boolean actionBarHasAdd() {
        boolean hasAdd = false;
        int tabIndex = viewPager.getCurrentItem();

        if (tabIndex != 0) {
            if (tabIndex == 1) {
                // Groups tab
                hasAdd = (LightingDirector.get().getGroupCount() < LightingDirector.MAX_GROUPS);
            } else if (tabIndex == 2) {
                // Scenes tab
                hasAdd =
                    (LightingDirector.get().getSceneCount() < LightingDirector.MAX_SCENES) ||
                    (LightingDirector.get().getMasterSceneCount() < LightingDirector.MAX_MASTER_SCENES);
            }
        }

        return hasAdd;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        addActionMenuItem = menu.findItem(R.id.action_add);
        nextActionMenuItem = menu.findItem(R.id.action_next);
        doneActionMenuItem = menu.findItem(R.id.action_done);
        settingsActionMenuItem  = menu.findItem(R.id.action_settings);

        if (pageFrameParent == null) {
            updateActionBar(actionBarHasAdd(), false, false, true);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        isForeground = true;
    }

    @Override
    public void onResumeFragments() {
        super.onResumeFragments();

        // run everything that was queued up whilst in the background
        Log.d(SampleAppActivity.TAG, "Clearing foreground runnable queue");
        while (!runInForeground.isEmpty()) {
            handler.post(runInForeground.remove());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        isForeground = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                if (pageFrameParent != null) {
                    onBackPressed();
                }
                return true;
            case R.id.action_add:
                if (pageFrameParent != null) {
                    pageFrameParent.onActionAdd();
                } else if (viewPager.getCurrentItem() == 1) {
                    doAddGroup((GroupsPageFragment)(getSupportFragmentManager().findFragmentByTag(GroupsPageFragment.TAG)));
                } else {
                    showSceneAddPopup(findViewById(R.id.action_add));
                }
                return true;
            case R.id.action_next:
                if (pageFrameParent != null) pageFrameParent.onActionNext();
                return true;
            case R.id.action_done:
                if (pageFrameParent != null) pageFrameParent.onActionDone();
                return true;
            case R.id.action_settings:
                showSettingsFragment();
                return true;
            case R.id.action_dashboard:
                showDashboard();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        viewPager.setCurrentItem(tab.getPosition());

        updateActionBar(actionBarHasAdd(), false, false, true);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void postOnBackPressed() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        }, 5);
    }

    @Override
    public void onBackPressed() {
        int backStackCount = pageFrameParent != null ? pageFrameParent.onBackPressed() : 0;

        if (backStackCount == 1) {
            onClearBackStack();
        } else if (backStackCount == 0) {
            super.onBackPressed();
        }
    }

    public void postInForeground(final Runnable r) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isForeground) {
                    Log.d(SampleAppActivity.TAG, "Foreground runnable running now");
                    handler.post(r);
                } else {
                    Log.d(SampleAppActivity.TAG, "Foreground runnable running later");
                    runInForeground.add(r);
                }
            }
        });
    }

    public void onClearBackStack() {
        pageFrameParent = null;
        resetActionBar();
    }

    public void onItemButtonMore(PageFrameParentFragment parent, Type type, View button, String itemID, String subItemID, boolean enabled) {
        switch (type) {
            case LAMP:
                showInfoFragment(parent, itemID);
                return;
            case GROUP:
                showGroupMorePopup(button, itemID);
                return;
            case SCENE:
                showSceneMorePopup(button, itemID);
                return;
            case ELEMENT:
                showSceneElementMorePopup(button, itemID, subItemID, enabled);
                return;
        }
    }

    private void showInfoFragment(PageFrameParentFragment parent, String itemID) {
        pageFrameParent = parent;

        parent.showInfoChildFragment(itemID);
    }

    public boolean applySceneElement(String sceneElementID) {
        return applySceneItem(LightingDirector.get().getSceneElement(sceneElementID), R.string.toast_scene_element_apply);
    }

    public boolean applyBasicScene(String basicSceneID) {
        return applySceneItem(LightingDirector.get().getScene(basicSceneID), R.string.toast_basic_scene_apply);
    }

    public boolean applyMasterScene(String masterSceneID) {
        return applySceneItem(LightingDirector.get().getMasterScene(masterSceneID), R.string.toast_master_scene_apply);
    }

    private boolean applySceneItem(SceneItem sceneItem, int messageID) {
        boolean doApply = sceneItem != null;

        if (doApply) {
            sceneItem.apply();

            showToast(String.format(this.getString(messageID), sceneItem.getName()));
        }

        return doApply;
    }

    private void initWifiMonitoring() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            initWifiMonitoringApi14();
        } else {
            initWifiMonitoringApi21();
        }
    }

    private void initWifiMonitoringApi14() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                wifiConnectionStateUpdate(isWifiConnected());
            }
        }, filter);
    }

    @SuppressLint("NewApi")
    private void initWifiMonitoringApi21() {
        // Set the initial wifi state
        wifiConnectionStateUpdate(isWifiConnected());

        // Listen for wifi state changes
        NetworkRequest networkRequest = (new NetworkRequest.Builder()).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                wifiConnectionStateUpdate(true);
            }

            @Override
            public void onLost(Network network) {
                wifiConnectionStateUpdate(false);
            }
        });
    }

    private void wifiConnectionStateUpdate(boolean connected) {
        final SampleAppActivity activity = this;

        postUpdateControllerDisplay();

        if (connected) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(SampleAppActivity.TAG, "wifi connected");

                    postInForeground(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(SampleAppActivity.TAG_TRACE, "Starting system");

                            LightingDirector.get().setNetworkConnectionStatus(true);

                            if (isControllerServiceEnabled()) {
                                Log.d(SampleAppActivity.TAG_TRACE, "Starting bundled controller service");
                                setControllerServiceStarted(true);
                            }

                            if (wifiDisconnectAlertDialog != null) {
                                wifiDisconnectAlertDialog.dismiss();
                                wifiDisconnectAlertDialog = null;
                            }
                        }
                    });
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(SampleAppActivity.TAG, "wifi disconnected");

                    postInForeground(new Runnable() {
                        @Override
                        public void run() {
                            if (wifiDisconnectAlertDialog == null) {
                                Log.d(SampleAppActivity.TAG, "Stopping system");

                                LightingDirector.get().setNetworkConnectionStatus(false);

                                setControllerServiceStarted(false);

                                View view = activity.getLayoutInflater().inflate(R.layout.view_loading, null);
                                ((TextView) view.findViewById(R.id.loadingText1)).setText(activity.getText(R.string.no_wifi_message));
                                ((TextView) view.findViewById(R.id.loadingText2)).setText(activity.getText(R.string.searching_wifi));

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                alertDialogBuilder.setView(view);
                                alertDialogBuilder.setTitle(R.string.no_wifi);
                                alertDialogBuilder.setCancelable(false);
                                wifiDisconnectAlertDialog = alertDialogBuilder.create();
                                wifiDisconnectAlertDialog.show();
                            }
                        }
                    });
                }
            });
        }
    }

    private void showErrorResponseCode(Enum code, String source, final boolean dependency) {
        final SampleAppActivity activity = this;
        // creates a message about the error
        StringBuilder sb = new StringBuilder();

        if (dependency) {
            // dependency error
            sb.append(this.getString(R.string.error_dependency));
        } else {
            String name = code.name();

            // default error message
            sb.append(this.getString(R.string.error_code));
            sb.append(" ");
            sb.append(name != null ? name : code.ordinal());
            sb.append(source != null ? " - " + source : "");
        }

        final String message = sb.toString();

        Log.w(SampleAppActivity.TAG, message);

        if (ERROR_CODE_ENABLED) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                    alertDialogBuilder.setTitle(R.string.error_title);
                    alertDialogBuilder.setPositiveButton(R.string.dialog_ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            errorCodeAlertDialog = null;
                            errorCodeAlertDialogMessage = null;
                            dialog.cancel();
                        }
                    });

                    errorCodeAlertDialog = alertDialogBuilder.create();
                    if (ERROR_CODE_VERBOSE || (!ERROR_CODE_VERBOSE && dependency)) {
                        if (errorCodeAlertDialogMessage == null) {
                            errorCodeAlertDialogMessage = message;
                            errorCodeAlertDialog.setMessage(errorCodeAlertDialogMessage);
                            errorCodeAlertDialog.show();
                        } else if (!errorCodeAlertDialogMessage.contains(message)) {
                            errorCodeAlertDialogMessage += System.getProperty("line.separator") + message;
                            errorCodeAlertDialog.setMessage(errorCodeAlertDialogMessage);
                            errorCodeAlertDialog.show();
                        }
                    }
                }
            });
        }
    }

    public void showItemNameDialog(int titleID, ItemNameAdapter adapter) {
        if (adapter != null) {
            View view = getLayoutInflater().inflate(R.layout.view_dialog_item_name, null, false);
            EditText nameText = (EditText)view.findViewById(R.id.itemNameText);
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(titleID)
                .setView(view)
                .setPositiveButton(R.string.dialog_ok, adapter)
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }})
                .create();

            nameText.addTextChangedListener(new ItemNameDialogTextWatcher(alertDialog, nameText));
            nameText.setText(adapter.getCurrentName());

            alertDialog.show();
        }
    }

    private void showConfirmDeleteItemDialog(final DeletableItem item, final List<DeletableItem> components, int confirmTitleID, int confirmLableID, int errorTitleID, int errorMessageID) {
        if (item != null) {
            final String itemName = item.getName();
            final LightingItem[] dependents = item.getDependents();

            if (dependents.length == 0) {
                showConfirmDeleteDialog(confirmTitleID, confirmLableID, itemName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(SampleAppActivity.TAG, "Delete item: " + itemName);

                        item.delete();

                        if (components != null) {
                            for (DeletableItem component : components) {
                                component.delete();
                            }
                        }
                    }});
            } else {
                String memberNames = MemberNamesString.format(this, dependents, MemberNamesOptions.en, 3, "");
                String message = String.format(getString(errorMessageID), itemName, memberNames);

                showPositiveErrorDialog(errorTitleID, message);
            }
        }
    }

    private void showConfirmDeleteSceneElementDialog(final String sceneElementID) {
        if (sceneElementID != null) {
            showConfirmDeleteItemDialog(
                LightingDirector.get().getSceneElement(sceneElementID),
                null,
                R.string.menu_scene_element_delete,
                R.string.label_scene_element,
                R.string.error_dependency_scene_element_title,
                R.string.error_dependency_scene_element_text);
        }
    }

    private void showConfirmDeleteBasicSceneDialog(final String basicSceneID) {
        Scene basicScene = LightingDirector.get().getScene(basicSceneID);
        List<DeletableItem> sceneComponents = new ArrayList<DeletableItem>();

        if (basicScene instanceof SceneV2) {
            for (SceneElement sceneElement : ((SceneV2)basicScene).getSceneElements()) {
                sceneComponents.add(sceneElement);
                sceneComponents.add(sceneElement.getEffect());
            }
        }

        if (basicSceneID != null) {
            showConfirmDeleteItemDialog(
                basicScene,
                sceneComponents,
                R.string.menu_basic_scene_delete,
                R.string.label_basic_scene,
                R.string.error_dependency_scene_title,
                R.string.error_dependency_scene_text);
        }
    }

    private void doDeleteSceneElement(String basicSceneID, String elementID ) {
        if (BasicSceneV2InfoFragment.pendingSceneV2 != null) {
            BasicSceneV2InfoFragment.pendingSceneV2.doDeleteSceneElement(elementID);
        }

        basicSceneV1Module.doDeleteSceneElement(elementID);

        refreshScene(LightingDirector.get().getScene(basicSceneID));
    }

    private void showConfirmDeleteMasterSceneDialog(final String masterSceneID) {
        if (masterSceneID != null) {
            showConfirmDeleteItemDialog(
                LightingDirector.get().getMasterScene(masterSceneID),
                null,
                R.string.menu_master_scene_delete,
                R.string.label_master_scene,
                0,
                0);
        }
    }

    private void showConfirmDeleteGroupDialog(final String groupID) {
        if (groupID != null) {
            showConfirmDeleteItemDialog(
                LightingDirector.get().getGroup(groupID),
                null,
                R.string.menu_group_delete,
                R.string.label_group,
                R.string.error_dependency_group_title,
                R.string.error_dependency_group_text);
        }
    }

    private void showConfirmDeletePresetDialog(final String presetID) {
        if (presetID != null) {
            showConfirmDeleteItemDialog(
                LightingDirector.get().getPreset(presetID),
                null,
                R.string.menu_preset_delete,
                R.string.label_preset,
                R.string.error_dependency_preset_title,
                R.string.error_dependency_preset_text);
        }
    }

    private void showConfirmDeleteTransitionEffectDialog(final String transitionEffectID) {
        if (transitionEffectID != null) {
            showConfirmDeleteItemDialog(
                LightingDirector.get().getTransitionEffect(transitionEffectID),
                null,
                R.string.menu_preset_delete,
                R.string.label_transition_effect,
                R.string.error_dependency_transition_effect_title,
                R.string.error_dependency_transition_effect_text);
        }
    }

    private void showConfirmDeletePulseEffectDialog(final String pulseEffectID) {
        if (pulseEffectID != null) {
            showConfirmDeleteItemDialog(
                LightingDirector.get().getPulseEffect(pulseEffectID),
                null,
                R.string.menu_pulse_effect_delete,
                R.string.label_pulse_effect,
                R.string.error_dependency_pulse_effect_title,
                R.string.error_dependency_pulse_effect_text);
        }
    }

    private void showConfirmDeleteDialog(int titleID, int labelID, String itemName, DialogInterface.OnClickListener onOKListener) {
        View view = getLayoutInflater().inflate(R.layout.view_dialog_confirm_delete, null, false);

        String format = getResources().getString(R.string.dialog_text_delete);
        String label = getResources().getString(labelID);
        String text = String.format(format, label, itemName);

        ((TextView)view.findViewById(R.id.confirmDeleteText)).setText(text);

        new AlertDialog.Builder(this)
            .setTitle(titleID)
            .setView(view)
            .setPositiveButton(R.string.dialog_ok, onOKListener)
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }})
            .create()
            .show();
    }

    private void showPositiveErrorDialog(int titleID, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(titleID);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(R.string.dialog_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            };
        });
        alertDialogBuilder.show();
    }

    private void showSceneInfo(ScenesPageFragment.Mode mode) {
        ScenesPageFragment scenesPageFragment = (ScenesPageFragment)getSupportFragmentManager().findFragmentByTag(ScenesPageFragment.TAG);
        scenesPageFragment.setMode(mode);

        if (scenesPageFragment.isBasicModeV1()) {
            // Copy the selected scene into the pending state
            basicSceneV1Module.resetPendingScene(popupItemID);
        } else if (scenesPageFragment.isBasicModeV2()) {
            Scene scene = LightingDirector.get().getScene(popupItemID);

            if (scene instanceof SceneV2) {
                BasicSceneV2InfoFragment.pendingSceneV2 = new PendingSceneV2((SceneV2)scene);
            } else {
                Log.e(SampleAppActivity.TAG, "Invalid scene type for ID " + popupItemID);
            }
        }

        showInfoFragment(scenesPageFragment, popupItemID);
    }

    public void showLampDetailsFragment(LampsPageFragment parent, String key) {
        pageFrameParent = parent;
        parent.showDetailsChildFragment(key);
    }

    public void doAddGroup(GroupsPageFragment parent) {
        if (parent != null) {
            pageFrameParent = parent;
            parent.showEnterNameChildFragment();
        }
    }

    public void showGroupMorePopup(View anchor, String groupID) {
        Group group = LightingDirector.get().getGroup(groupID);
        popupItemID = groupID;

        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.group_more);
        popup.getMenu().findItem(R.id.group_delete).setEnabled(!group.isAllLampsGroup());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public boolean isSwipeable() {
        return (pageFrameParent == null);
    }

    public void showSceneMorePopup(View anchor, String sceneItemID) {
        LightingDirector director = LightingDirector.get();
        boolean isControllerServiceLeaderV1 = director.isControllerServiceLeaderV1();
        boolean isSceneCreationUIAvailable = !isControllerServiceLeaderV1 || basicSceneV1Module.isModuleInstalled();
        int menuID = 0;

        if (LightingDirector.get().getSceneElement(sceneItemID) != null) {
            menuID = R.menu.scene_element_more;
        } else if (LightingDirector.get().getScene(sceneItemID) != null) {
            menuID = R.menu.basic_scene_more;
        } else if (LightingDirector.get().getMasterScene(sceneItemID) != null) {
            menuID = R.menu.master_scene_more;
        } else {
            Log.e(SampleAppActivity.TAG, "showSceneMorePopup() failed: Invalid ID: " + sceneItemID);
        }

        popupItemID = sceneItemID;

        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(menuID);

        if (menuID == R.menu.basic_scene_more) {
            popup.getMenu().findItem(R.id.basic_scene_info).setEnabled(isSceneCreationUIAvailable);
        }

        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void showSceneAddPopup(View anchor) {
        LightingDirector director = LightingDirector.get();
        boolean isControllerServiceLeaderV1 = director.isControllerServiceLeaderV1();
        boolean isSceneCreationUIAvailable = !isControllerServiceLeaderV1 || basicSceneV1Module.isModuleInstalled();
        int sceneCount = director.getSceneCount();
        int masterSceneCount = director.getMasterSceneCount();

        popupItemID = null;

        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.scene_add);
        popup.getMenu().findItem(R.id.scene_add_basic).setEnabled(isSceneCreationUIAvailable && sceneCount < LightingDirector.MAX_SCENES);
        popup.getMenu().findItem(R.id.scene_add_master).setEnabled(masterSceneCount < LightingDirector.MAX_MASTER_SCENES);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void showSceneElementMorePopup(View anchor, String itemID, String subItemID, boolean enabled) {
        popupItemID = itemID;
        popupSubItemID = subItemID;

        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.basic_scene_element_more);
        popup.getMenu().findItem(R.id.basic_scene_element_delete).setEnabled(enabled);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void showPresetMorePopup(View anchor, String itemID) {
        popupItemID = itemID;
        popupSubItemID = null;

        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.preset_more);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void showTransitionEffectMorePopup(View anchor, String itemID) {
        popupItemID = itemID;
        popupSubItemID = null;

        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.transition_effect_more);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void showPulseEffectMorePopup(View anchor, String itemID) {
        popupItemID = itemID;
        popupSubItemID = null;

        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.pulse_effect_more);
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    protected void showSettingsFragment() {
        if (pageFrameParent == null) {
            int pageIndex = viewPager.getCurrentItem();
            String pageTag;

            if (pageIndex == 0) {
                pageTag = LampsPageFragment.TAG;
            } else if (pageIndex == 1) {
                pageTag = GroupsPageFragment.TAG;
            } else {
                pageTag = ScenesPageFragment.TAG;
            }

            pageFrameParent = (PageFrameParentFragment)getSupportFragmentManager().findFragmentByTag(pageTag);
        }

        pageFrameParent.showSettingsChildFragment("");
    }

    protected void initDashboard() {
        try {
            // Use reflection for loose coupling with the Dashboard plugin
            Class<?> dashboardPluginClass = Class.forName("org.alljoyn.dashboard.plugin.DashboardPlugin");

            if (dashboardPluginClass != null) {
                Method getDashboardInstanceMethod = dashboardPluginClass.getMethod("getInstance");

                if (getDashboardInstanceMethod != null) {
                    Method initDashboardMethod = dashboardPluginClass.getMethod("init", android.content.Context.class);

                    if (initDashboardMethod != null) {
                        Log.d(SampleAppActivity.TAG_TRACE, "initializing dashboard");
                        initDashboardMethod.invoke(getDashboardInstanceMethod.invoke(null), this);
                    } else {
                        Log.d(SampleAppActivity.TAG_TRACE, "dashboard init() not found");
                    }
                } else {
                    Log.d(SampleAppActivity.TAG_TRACE, "dashboard getInstance() not found");
                }
            } else {
                Log.d(SampleAppActivity.TAG_TRACE, "dashboard class not found");
            }
        } catch (Exception e) {
            Log.d(SampleAppActivity.TAG_TRACE, "dashboard init failed: " + e.toString());
        }
    }

    protected void showDashboard() {
        boolean success = false;

        try {
            // Use reflection for loose coupling with the Dashboard plugin
            Class<?> dashboardPluginClass = Class.forName("org.alljoyn.dashboard.plugin.DashboardPlugin");

            if (dashboardPluginClass != null) {
                Method getDashboardInstanceMethod = dashboardPluginClass.getMethod("getInstance");

                if (getDashboardInstanceMethod != null) {
                    Method showDashboardMethod = dashboardPluginClass.getMethod("showInitialActivity");

                    if (showDashboardMethod != null) {
                        Log.d(SampleAppActivity.TAG_TRACE, "showing dashboard");
                        showDashboardMethod.invoke(getDashboardInstanceMethod.invoke(null));
                        success = true;
                    } else {
                        Log.d(SampleAppActivity.TAG_TRACE, "dashboard show...() not found");
                    }
                } else {
                    Log.d(SampleAppActivity.TAG_TRACE, "dashboard getInstance() not found");
                }
            } else {
                Log.d(SampleAppActivity.TAG_TRACE, "dashboard class not found");
            }
        } catch (Exception e) {
            Log.d(SampleAppActivity.TAG_TRACE, "dashboard show...() failed: " + e.toString());
        }

        if (!success) {
            showPositiveErrorDialog(R.string.error_dashboard, getString(R.string.error_dashboard_text));
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Log.d(SampleAppActivity.TAG, "onMenuItemClick(): " + item.getItemId());
        boolean result = true;

        switch (item.getItemId()) {
            case R.id.group_info:
                showInfoFragment((GroupsPageFragment)(getSupportFragmentManager().findFragmentByTag(GroupsPageFragment.TAG)), popupItemID);
                break;
            case R.id.group_delete:
                showConfirmDeleteGroupDialog(popupItemID);
                break;
            case R.id.scene_element_apply:
                applySceneElement(popupItemID);
                break;
            case R.id.scene_element_delete:
                showConfirmDeleteSceneElementDialog(popupItemID);
                break;
            case R.id.basic_scene_info:
                showSceneInfo(ScenesPageFragment.Mode.BASIC);
                break;
            case R.id.basic_scene_apply:
                applyBasicScene(popupItemID);
                break;
            case R.id.basic_scene_delete:
                showConfirmDeleteBasicSceneDialog(popupItemID);
                break;
            case R.id.basic_scene_element_delete:
                doDeleteSceneElement(popupItemID, popupSubItemID);
                break;
            case R.id.master_scene_info:
                showSceneInfo(ScenesPageFragment.Mode.MASTER);
                break;
            case R.id.master_scene_apply:
                applyMasterScene(popupItemID);
                break;
            case R.id.master_scene_delete:
                showConfirmDeleteMasterSceneDialog(popupItemID);
                break;
            case R.id.preset_delete:
                showConfirmDeletePresetDialog(popupItemID);
                break;
            case R.id.transition_effect_delete:
                showConfirmDeleteTransitionEffectDialog(popupItemID);
                break;
            case R.id.pulse_effect_delete:
                showConfirmDeletePulseEffectDialog(popupItemID);
                break;
            case R.id.scene_add_basic:
                doAddScene((ScenesPageFragment)(getSupportFragmentManager().findFragmentByTag(ScenesPageFragment.TAG)), ScenesPageFragment.Mode.BASIC);
                break;
            case R.id.scene_add_master:
                doAddScene((ScenesPageFragment)(getSupportFragmentManager().findFragmentByTag(ScenesPageFragment.TAG)), ScenesPageFragment.Mode.MASTER);
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    public void doAddScene(ScenesPageFragment parent, ScenesPageFragment.Mode mode) {
        if (parent != null) {
            basicSceneV1Module.resetPendingEffects();

            BasicSceneV2InfoFragment.pendingSceneV2 = new PendingSceneV2();

            pageFrameParent = parent;
            parent.setMode(mode);

            if (parent.isBasicMode()) {
                basicSceneV1Module.resetPendingScene(null);

                // Create a dummy scene so that we can momentarily display
                // the info fragment. This makes sure the info fragment is
                // on the back stack so that we can more easily support the
                // scene V1 creation workflow. Note that if the user backs out
                // of the scene V1 creation process, we have to skip over the
                // dummy info fragment (see ScenesPageFragment.onBackPressed())
                parent.showInfoChildFragment(null);
            }

            parent.showEnterNameChildFragment();
        }
    }

    public void resetActionBar() {
        updateActionBar(null, true, actionBarHasAdd(), false, false, true);
    }

    public void updateActionBar(int titleID, boolean tabs, boolean add, boolean next, boolean done, boolean settings) {
        updateActionBar(getResources().getString(titleID), tabs, add, next, done, settings);
    }

    protected void updateActionBar(String title, boolean tabs, boolean add, boolean next, boolean done, boolean settings) {
        Log.d(SampleAppActivity.TAG, "Updating action bar to " + title);
        ActionBar actionBar = getActionBar();

        actionBar.setTitle(title != null ? title : getTitle());
        actionBar.setNavigationMode(tabs ? ActionBar.NAVIGATION_MODE_TABS : ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayHomeAsUpEnabled(!tabs);

        updateActionBar(add, next, done, settings);
    }

    protected void updateActionBar(boolean add, boolean next, boolean done, boolean settings) {
        if (addActionMenuItem != null) {
            addActionMenuItem.setVisible(add);
        }

        if (nextActionMenuItem != null) {
            nextActionMenuItem.setVisible(next);
        }

        if (doneActionMenuItem != null) {
            doneActionMenuItem.setVisible(done);
        }

        if (settingsActionMenuItem != null) {
            settingsActionMenuItem.setVisible(settings);
        }
    }

    public void setActionBarNextEnabled(boolean isEnabled) {
        if (nextActionMenuItem != null) {
            nextActionMenuItem.setEnabled(isEnabled);
        }
    }

    public void setActionBarDoneEnabled(boolean isEnabled) {
        if (doneActionMenuItem != null) {
            doneActionMenuItem.setEnabled(isEnabled);
        }
    }

    private MutableColorItem getMutableColorItem(Type type, String itemID) {
        MutableColorItem colorItem;

        if (type == Type.LAMP) {
            colorItem = LightingDirector.get().getLamp(itemID);
        } else if (type == Type.GROUP) {
            colorItem = LightingDirector.get().getGroup(itemID);
        } else {
            Log.e(SampleAppActivity.TAG, "getMutableColorItem() failed: unsupported type: " + type.name());
            colorItem = null;
        }

        return colorItem;
    }

    public void togglePower(SampleAppActivity.Type type, String itemID) {
        MutableColorItem colorItem = getMutableColorItem(type, itemID);

        if (colorItem != null) {
            if (colorItem.isOff() && colorItem.getColor().getBrightness() == 0) {
                // Raise brightness to 25% if needed
                Color color = colorItem.getColor();

                color.setBrightness(25);
                colorItem.setColor(color);
            }

            Log.d(SampleAppActivity.TAG, "Toggle power for " + colorItem.getName());

            colorItem.togglePower();
        }
    }

    private boolean allowFieldChange() {
        boolean allow = false;
        long currentTimeMillis = Calendar.getInstance().getTimeInMillis();

        if (currentTimeMillis - lastFieldChangeMillis > FIELD_CHANGE_HOLDOFF) {
            lastFieldChangeMillis = currentTimeMillis;
            allow = true;
        }

        return allow;
    }

    public void setBrightness(SampleAppActivity.Type type, String itemID, int newViewBrightness) {
        if (allowFieldChange()) {
            MutableColorItem colorItem = getMutableColorItem(type, itemID);

            if (colorItem != null) {
                Color color = colorItem.getColor();
                int oldViewBrightness = color.getBrightness();

                Log.d(SampleAppActivity.TAG, "Set brightness for " + colorItem.getName() + " to " + newViewBrightness);

                colorItem.setBrightness(newViewBrightness);

                if (newViewBrightness == 0) {
                    // Setting brightness to zero forces the power off
                    colorItem.turnOff();
                } else if (oldViewBrightness == 0 && colorItem.isOff()) {
                    // Raising the brightness on a dark item forces the power on
                    colorItem.turnOn();
                }
            }
        }
    }

    public void setHue(SampleAppActivity.Type type, String itemID, int viewHue) {
        if (allowFieldChange()) {
            MutableColorItem colorItem = getMutableColorItem(type, itemID);

            if (colorItem != null) {
                Log.d(SampleAppActivity.TAG, "Set hue for " + colorItem.getName() + " to " + viewHue);

                colorItem.setHue(viewHue);
            }
        }
    }

    public void setSaturation(SampleAppActivity.Type type, String itemID, int viewSaturation) {
        if (allowFieldChange()) {
            MutableColorItem colorItem = getMutableColorItem(type, itemID);

            if (colorItem != null) {
                Log.d(SampleAppActivity.TAG, "Set saturation for " + colorItem.getName() + " to " + viewSaturation);

                colorItem.setSaturation(viewSaturation);
            }
        }
    }

    public void setColorTemp(SampleAppActivity.Type type, String itemID, int viewColorTemp) {
        if (allowFieldChange()) {
            MutableColorItem colorItem = getMutableColorItem(type, itemID);

            if (colorItem != null) {
                Log.d(SampleAppActivity.TAG, "Set color temp for " + colorItem.getName() + " to " + viewColorTemp);

                colorItem.setColorTemperature(viewColorTemp);
            }
        }
    }

    public void createLostConnectionErrorDialog(String name) {
        pageFrameParent.clearBackStack();
        showPositiveErrorDialog(R.string.error_connection_lost_dialog_text, String.format(getString(R.string.error_connection_lost_dialog_text), name));
    }

    public void setTabTitles() {
        Log.d(SampleAppActivity.TAG, "setTabTitles()");
        ActionBar actionBar = getActionBar();
        for (int i = 0; i < actionBar.getTabCount(); i++) {
            actionBar.getTabAt(i).setText(getPageTitle(i));
        }
    }

    public CharSequence getPageTitle(int index) {
        LightingDirector director = LightingDirector.get();
        Controller controller = director.getLeadController();
        Locale locale = Locale.ENGLISH;
        CharSequence title;

        boolean connected = controller != null ? controller.isConnected() : false;

        if (index == 0) {
            int lampCount = director.getLampCount();
            title = getString(R.string.title_tab_lamps, connected ? lampCount : 0).toUpperCase(locale);
        } else if (index == 1) {
            int groupCount = director.getGroupCount();
            title = getString(R.string.title_tab_groups, connected ? groupCount : 0).toUpperCase(locale);
        } else if (index == 2) {
            int basicSceneCount = director.getSceneCount();
            int masterSceneCount =  director.getMasterSceneCount();
            title = getString(R.string.title_tab_scenes, connected ? basicSceneCount + masterSceneCount : 0).toUpperCase(locale);
        } else {
            title = null;
        }

        return title;
    }

    public void showToast(int resId){

        toast = Toast.makeText(this, resId, Toast.LENGTH_LONG);
        toast.show();
    }

    public void showToast(String text){

        toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    public Toast getToast(){
        return toast;
    }

    @Override
    public void onLampInitialized(final Lamp lamp) {
        // used; intentionally left blank
    }

    @Override
    public void onLampChanged(final Lamp lamp) {
        Log.d(SampleAppActivity.TAG, "onLampChanged() " + lamp.getName());

        Fragment lampsPageFragment = getSupportFragmentManager().findFragmentByTag(LampsPageFragment.TAG);

        if (lampsPageFragment != null) {
            LampsTableFragment tableFragment = (LampsTableFragment)lampsPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE);

            if (tableFragment != null) {
                // Call addLamp() rather than addItem() to update the color indicator
                tableFragment.addLamp(lamp);
            }

            LampInfoFragment infoFragment = (LampInfoFragment)lampsPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_INFO);

            if (infoFragment != null) {
                infoFragment.updateInfoFields(lamp);
            }

            LampDetailsFragment detailsFragment = (LampDetailsFragment)lampsPageFragment.getChildFragmentManager().findFragmentByTag(LampsPageFragment.CHILD_TAG_DETAILS);

            if (detailsFragment != null) {
                detailsFragment.updateDetailFields(lamp);
            }
        }

        if (LightingDirector.get().isControllerServiceLeaderV1()) {
            refreshScene(null);
        } else {
            updateDependentScenesV2(lamp);
        }
    }

    @Override
    public void onLampRemoved(final Lamp lamp) {
        final String lampID = lamp.getId();

        Log.d(SampleAppActivity.TAG, "onLampRemoved() " + lampID);

        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(LampsPageFragment.TAG);

        if (pageFragment != null) {
            LampsTableFragment tableFragment = (LampsTableFragment) pageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE);

            if (tableFragment != null) {
                tableFragment.removeElement(lampID);

                if (LightingDirector.get().getLampCount() == 0) {
                    tableFragment.updateLoading();
                }
            }
        }

        if (LightingDirector.get().isControllerServiceLeaderV1()) {
            refreshScene(null);
        } else {
            updateDependentScenesV2(lamp);
        }
    }

    @Override
    public void onLampError(final LightingItemErrorEvent error) {
        Log.d(SampleAppActivity.TAG, "onLampError()");

        handler.post(new Runnable() {
            @Override
            public void run() {
                showErrorResponseCode(error.responseCode, error.name, error.responseCode == ResponseCode.ERR_DEPENDENCY);
            }
        });
    }

    @Override
    public void onGroupInitialized(final TrackingID trackingID, Group group) {
        // used; intentionally left blank
    }

    @Override
    public void onGroupChanged(Group group) {
        Log.d(SampleAppActivity.TAG, "onGroupChanged() " + group.getName());

        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(GroupsPageFragment.TAG);

        if (pageFragment != null) {
            GroupsTableFragment tableFragment = (GroupsTableFragment)pageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE);

            if (tableFragment != null) {
                tableFragment.addItem(group);

                if (isSwipeable()) {
                    resetActionBar();
                }
            }

            GroupInfoFragment infoFragment = (GroupInfoFragment)pageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_INFO);

            if (infoFragment != null) {
                infoFragment.updateInfoFields(group);
            }
        }

        if (LightingDirector.get().isControllerServiceLeaderV1()) {
            refreshScene(null);
        } else {
            updateDependentScenesV2(group);
        }
    }

    @Override
    public void onGroupRemoved(Group group) {
        String groupID = group.getId();

        Log.d(SampleAppActivity.TAG, "onGroupRemoved() " + groupID);

        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(GroupsPageFragment.TAG);
        FragmentManager childManager = pageFragment != null ? pageFragment.getChildFragmentManager() : null;
        GroupsTableFragment tableFragment = childManager != null ? (GroupsTableFragment)childManager.findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE) : null;
        PageFrameChildFragment infoFragment = childManager != null ? (PageFrameChildFragment)childManager.findFragmentByTag(PageFrameParentFragment.CHILD_TAG_INFO) : null;

        if (tableFragment != null) {
            tableFragment.removeElement(groupID);

            if (isSwipeable()) {
                resetActionBar();
            }
        }

        if ((infoFragment != null) && (infoFragment.key.equals(groupID))) {
            createLostConnectionErrorDialog(group.getName());
        }
    }

    @Override
    public void onGroupError(final LightingItemErrorEvent error) {
        Log.d(SampleAppActivity.TAG, "onGroupError()");

        handler.post(new Runnable() {
            @Override
            public void run() {
                showErrorResponseCode(error.responseCode, error.name, error.responseCode == ResponseCode.ERR_DEPENDENCY);
            }
        });
    }

    @Override
    public void onPresetInitialized(final TrackingID trackingID, Preset preset) {
        // used; intentionally left blank
    }

    @Override
    public void onPresetChanged(Preset preset) {
        String presetID = preset.getId();

        Log.d(SampleAppActivity.TAG, "onPresetChanged() " + presetID);

        updatePresetFragment(LampsPageFragment.TAG);
        updatePresetFragment(GroupsPageFragment.TAG);
        updatePresetFragment(ScenesPageFragment.TAG);

        updateInfoFragmentPresetFields(LampsPageFragment.TAG, PageFrameParentFragment.CHILD_TAG_INFO);
        updateInfoFragmentPresetFields(GroupsPageFragment.TAG, PageFrameParentFragment.CHILD_TAG_INFO);
        updateInfoFragmentPresetFields(ScenesPageFragment.TAG, ScenesPageFragment.CHILD_TAG_CONSTANT_EFFECT);
        updateInfoFragmentPresetFields(ScenesPageFragment.TAG, ScenesPageFragment.CHILD_TAG_TRANSITION_EFFECT);
        updateInfoFragmentPresetFields(ScenesPageFragment.TAG, ScenesPageFragment.CHILD_TAG_PULSE_EFFECT);

        updateEffect();
    }

    @Override
    public void onPresetRemoved(Preset preset) {
        String presetID = preset.getId();

        Log.d(SampleAppActivity.TAG, "onPresetRemoved() " + presetID);

        removePreset(LampsPageFragment.TAG, presetID);
        removePreset(GroupsPageFragment.TAG, presetID);
        removePreset(ScenesPageFragment.TAG, presetID);

        removeEffect(presetID);
    }

    @Override
    public void onPresetError(final LightingItemErrorEvent error) {
        Log.d(SampleAppActivity.TAG, "onPresetError()");

        handler.post(new Runnable() {
            @Override
            public void run() {
                showErrorResponseCode(error.responseCode, error.name, error.responseCode == ResponseCode.ERR_DEPENDENCY);
            }
        });
    }

    private void updatePresetFragment(String pageFragmentTag) {
        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(pageFragmentTag);

        if (pageFragment != null) {
            FragmentManager childManager = pageFragment.getChildFragmentManager();
            DimmableItemPresetsFragment presetFragment = (DimmableItemPresetsFragment)childManager.findFragmentByTag(PageFrameParentFragment.CHILD_TAG_PRESETS);

            updatePresetFragment(presetFragment);
        }
    }

    private void updatePresetFragment(DimmableItemPresetsFragment presetFragment) {
        if (presetFragment != null) {
            presetFragment.onUpdateView();
        }
    }

    private void updateEffect() {
        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(ScenesPageFragment.TAG);

        if (pageFragment != null) {
            FragmentManager childManager = pageFragment.getChildFragmentManager();
            SceneElementV2SelectEffectFragment effectsFragment = (SceneElementV2SelectEffectFragment)childManager.findFragmentByTag(ScenesPageFragment.CHILD_TAG_SELECT_EFFECT);

            if (effectsFragment != null) {
                effectsFragment.onUpdateView();
            }
        }
    }

    private void removeEffect(String effectID) {
        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(ScenesPageFragment.TAG);

        if (pageFragment != null) {
            FragmentManager childManager = pageFragment.getChildFragmentManager();
            SceneElementV2SelectEffectFragment effectsFragment = (SceneElementV2SelectEffectFragment)childManager.findFragmentByTag(ScenesPageFragment.CHILD_TAG_SELECT_EFFECT);

            if (effectsFragment != null) {
                effectsFragment.removeElement(effectID);
            }
        }
    }

    private void updateInfoFragmentPresetFields(String pageFragmentTag, String infoFragmentTag) {
        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(pageFragmentTag);

        if (pageFragment != null) {
            updateinfoFragmentPresetFields((DimmableItemInfoFragment)pageFragment.getChildFragmentManager().findFragmentByTag(infoFragmentTag));
        }
    }

    private void updateinfoFragmentPresetFields(DimmableItemInfoFragment infoFragment) {
        if (infoFragment != null) {
            infoFragment.updatePresetFields();
        }
    }

    private void removePreset(String pageFragmentTag, String presetID) {
        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(pageFragmentTag);

        if (pageFragment != null) {
            FragmentManager childManager = pageFragment.getChildFragmentManager();
            DimmableItemPresetsFragment presetFragment = (DimmableItemPresetsFragment)childManager.findFragmentByTag(PageFrameParentFragment.CHILD_TAG_PRESETS);

            removePreset(presetFragment, presetID);
        }
    }

    private void removePreset(DimmableItemPresetsFragment presetFragment, String presetID) {
        if (presetFragment != null) {
            presetFragment.removeElement(presetID);
        }
    }

    private void updateDependentScenesV2(final Lamp lamp) {
        Scene[] scenes = LightingDirector.get().getScenes();

        for (Scene scene : scenes) {
            SceneElement[] sceneElements = ((SceneV2)scene).getSceneElements();
            boolean found = false;

            for (int sceneElementIndex = 0; !found && sceneElementIndex < sceneElements.length; sceneElementIndex++) {
                found = sceneElements[sceneElementIndex].hasLamp(lamp);
            }

            if (found) {
                refreshScene(scene);
            }
        }
    }

    private void updateDependentScenesV2(final Group group) {
        Scene[] scenes = LightingDirector.get().getScenes();

        for (Scene scene : scenes) {
            SceneElement[] sceneElements = ((SceneV2)scene).getSceneElements();
            boolean found = false;

            for (int sceneElementIndex = 0; !found && sceneElementIndex < sceneElements.length; sceneElementIndex++) {
                found = sceneElements[sceneElementIndex].hasGroup(group);
            }

            if (found) {
                refreshScene(scene);
            }
        }
    }

    @Override
    public void onSceneInitialized(final TrackingID trackingID, Scene scene) {
        // used; intentionally left blank
    }

    @Override
    public void onSceneChanged(Scene basicScene) {
        Log.d(SampleAppActivity.TAG, "onSceneChanged() " + basicScene.getName());

        refreshScene(basicScene);
    }

    @Override
    public void onSceneRemoved(Scene basicScene) {
        Log.d(SampleAppActivity.TAG, "onSceneRemoved() " + basicScene.getName());

        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(ScenesPageFragment.TAG);
        FragmentManager childManager = pageFragment != null ? pageFragment.getChildFragmentManager() : null;
        ScenesTableFragment tableFragment = childManager != null ? (ScenesTableFragment)childManager.findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE) : null;
        PageFrameChildFragment infoFragment = childManager != null ? (PageFrameChildFragment)childManager.findFragmentByTag(PageFrameParentFragment.CHILD_TAG_INFO) : null;

        if (tableFragment != null) {
            tableFragment.removeElement(basicScene.getId());

            if (isSwipeable()) {
                resetActionBar();
            }
        }

        if ((infoFragment != null) && (infoFragment.key.equals(basicScene.getId()))) {
            createLostConnectionErrorDialog(basicScene.getName());
        }
    }

    @Override
    public void onSceneError(final LightingItemErrorEvent error) {
        Log.d(SampleAppActivity.TAG, "onSceneError()");

        handler.post(new Runnable() {
            @Override
            public void run() {
                showErrorResponseCode(error.responseCode, error.name, error.responseCode == ResponseCode.ERR_DEPENDENCY);
            }
        });
    }

    private void refreshScene(Scene basicScene) {
        ScenesPageFragment scenesPageFragment = (ScenesPageFragment)getSupportFragmentManager().findFragmentByTag(ScenesPageFragment.TAG);

        if (scenesPageFragment != null) {
            ScenesTableFragment basicSceneTableFragment = (ScenesTableFragment)scenesPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE);

            if (basicSceneTableFragment != null && basicScene != null) {
                basicSceneTableFragment.addBasicScene(this, basicScene);

                if (isSwipeable()) {
                    resetActionBar();
                }
            }

            if (scenesPageFragment.isBasicMode()) {
                SceneItemInfoFragment basicSceneInfoFragment = (SceneItemInfoFragment)scenesPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_INFO);

                if (basicSceneInfoFragment != null) {
                    basicSceneInfoFragment.updateInfoFields();
                }
            }
        }
    }

    @Override
    public void onMasterSceneInitialized(final TrackingID trackingID, MasterScene masterScener) {
        // used; intentionally left blank
    }

    @Override
    public void onMasterSceneChanged(MasterScene masterScene) {
        Log.d(SampleAppActivity.TAG, "onMasterSceneChanged() " + masterScene.getName());

        ScenesPageFragment scenesPageFragment = (ScenesPageFragment)getSupportFragmentManager().findFragmentByTag(ScenesPageFragment.TAG);

        if (scenesPageFragment != null) {
            ScenesTableFragment scenesTableFragment = (ScenesTableFragment)scenesPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE);

            if (scenesTableFragment != null) {
                scenesTableFragment.addMasterScene(this, masterScene);

                if (isSwipeable()) {
                    resetActionBar();
                }
            }

            if (scenesPageFragment.isMasterMode()) {
                MasterSceneInfoFragment masterSceneInfoFragment = (MasterSceneInfoFragment)scenesPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_INFO);

                if (masterSceneInfoFragment != null) {
                    masterSceneInfoFragment.updateInfoFields();
                }
            }
        }
    }

    @Override
    public void onMasterSceneRemoved(MasterScene masterScene) {
        Log.d(SampleAppActivity.TAG, "onMasterSceneRemoved() " + masterScene.getName());

        Fragment pageFragment = getSupportFragmentManager().findFragmentByTag(ScenesPageFragment.TAG);
        FragmentManager childManager = pageFragment != null ? pageFragment.getChildFragmentManager() : null;
        ScenesTableFragment tableFragment = childManager != null ? (ScenesTableFragment)childManager.findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE) : null;
        PageFrameChildFragment infoFragment = childManager != null ? (PageFrameChildFragment)childManager.findFragmentByTag(PageFrameParentFragment.CHILD_TAG_INFO) : null;

        if (tableFragment != null) {
            tableFragment.removeElement(masterScene.getId());

            if (isSwipeable()) {
                resetActionBar();
            }
        }

        if ((infoFragment != null) && (infoFragment.key.equals(masterScene.getId()))) {
            createLostConnectionErrorDialog(masterScene.getName());
        }
    }

    @Override
    public void onMasterSceneError(final LightingItemErrorEvent error) {
        Log.d(SampleAppActivity.TAG, "onMasterSceneError()");

        handler.post(new Runnable() {
            @Override
            public void run() {
                showErrorResponseCode(error.responseCode, error.name, error.responseCode == ResponseCode.ERR_DEPENDENCY);
            }
        });
    }

    @Override
    public void onLeaderChange(Controller leader) {
        Log.d(SampleAppActivity.TAG, "Leader changed: name " + leader.getName() + ", version " + leader.getVersion());

        controllerClientConnected = leader.isConnected();
        postUpdateControllerDisplay();
    }

    public boolean isControllerConnected() {
        return controllerClientConnected;
    }

    @Override
    public void onControllerErrors(final LightingItemErrorEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (event.errorCodes != null) {
                    for (ErrorCode ec : event.errorCodes) {
                        if (!ec.equals(ErrorCode.NONE)) {
                            showErrorResponseCode(ec, event.name, false);
                        }
                    }
                }
            }
        });
    }

    protected void postUpdateControllerDisplay() {
        postInForeground(new Runnable() {
            @Override
            public void run() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                PageFrameParentFragment lampsPageFragment = (PageFrameParentFragment)fragmentManager.findFragmentByTag(LampsPageFragment.TAG);
                PageFrameParentFragment groupsPageFragment = (PageFrameParentFragment)fragmentManager.findFragmentByTag(GroupsPageFragment.TAG);
                PageFrameParentFragment scenesPageFragment = (PageFrameParentFragment)fragmentManager.findFragmentByTag(ScenesPageFragment.TAG);
                Fragment settingsFragment = null;

                if (lampsPageFragment != null) {
                    ScrollableTableFragment tableFragment = (ScrollableTableFragment) lampsPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE);

                    if (controllerClientConnected) {
                        lampsPageFragment.clearBackStack();
                    }

                    if (tableFragment != null) {
                        tableFragment.updateLoading();
                    }

                    if (settingsFragment == null) {
                        settingsFragment = lampsPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_SETTINGS);
                    }
                }

                if (groupsPageFragment != null) {
                    ScrollableTableFragment tableFragment = (ScrollableTableFragment) groupsPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE);

                    if (controllerClientConnected) {
                        groupsPageFragment.clearBackStack();
                    }

                    if (tableFragment != null) {
                        tableFragment.updateLoading();
                    }

                    if (settingsFragment == null) {
                        settingsFragment = groupsPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_SETTINGS);
                    }
                }

                if (scenesPageFragment != null) {
                    ScrollableTableFragment tableFragment = (ScrollableTableFragment) scenesPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_TABLE);

                    if (controllerClientConnected) {
                        scenesPageFragment.clearBackStack();
                    }

                    if (tableFragment != null) {
                        tableFragment.updateLoading();
                    }

                    if (settingsFragment == null) {
                        settingsFragment = scenesPageFragment.getChildFragmentManager().findFragmentByTag(PageFrameParentFragment.CHILD_TAG_SETTINGS);
                    }
                }

                if (settingsFragment != null) {
                    ((SettingsFragment)settingsFragment).onUpdateView();
                }
            }
        });
    }

    @Override
    public void onTransitionEffectInitialized(final TrackingID trackingID, TransitionEffect effect) {
        // used; intentionally left blank
    }

    @Override
    public void onTransitionEffectChanged(TransitionEffect effect) {
        updateEffect();
    }

    @Override
    public void onTransitionEffectError(final LightingItemErrorEvent error) {
        Log.d(SampleAppActivity.TAG, "onTransitionEffectError() " + error.name);

        handler.post(new Runnable() {
            @Override
            public void run() {
                showErrorResponseCode(error.responseCode, error.name, error.responseCode == ResponseCode.ERR_DEPENDENCY);
            }
        });
    }

    @Override
    public void onTransitionEffectRemoved(TransitionEffect effect) {
        removeEffect(effect.getId());
    }

    @Override
    public void onPulseEffectInitialized(final TrackingID trackingID, PulseEffect effect) {
        // used; intentionally left blank
    }

    @Override
    public void onPulseEffectChanged(PulseEffect effect) {
        updateEffect();
    }

    @Override
    public void onPulseEffectError(final LightingItemErrorEvent error) {
        Log.d(SampleAppActivity.TAG, "onPulseEffectError() " + error.name);

        handler.post(new Runnable() {
            @Override
            public void run() {
                showErrorResponseCode(error.responseCode, error.name, error.responseCode == ResponseCode.ERR_DEPENDENCY);
            }
        });
    }

    @Override
    public void onPulseEffectRemoved(PulseEffect effect) {
        removeEffect(effect.getId());
    }

    @Override
    public void onSceneElementInitialized(final TrackingID trackingID, SceneElement element) {
        // used; intentionally left blank
    }

    @Override
    public void onSceneElementChanged(SceneElement element) {
        Log.d(SampleAppActivity.TAG, "onSceneElementChanged() " + element.getName());

        refreshSceneElement(element);
    }

    @Override
    public void onSceneElementError(final LightingItemErrorEvent error) {
        Log.d(SampleAppActivity.TAG, "onSceneElementError() " + error.name);

        handler.post(new Runnable() {
            @Override
            public void run() {
                showErrorResponseCode(error.responseCode, error.name, error.responseCode == ResponseCode.ERR_DEPENDENCY);
            }
        });
    }

    @Override
    public void onSceneElementRemoved(SceneElement sceneElement) {
        Log.d(SampleAppActivity.TAG, "onSceneElementRemoved() " + sceneElement.getName());

        refreshSceneElement(sceneElement);
    }

    private void refreshSceneElement(SceneElement sceneElement) {
        refreshScene(null);
    }
}
