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

import org.allseen.lsf.sdk.Controller;
import org.allseen.lsf.sdk.LightingController;
import org.allseen.lsf.sdk.LightingDirector;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingsFragment extends PageFrameChildFragment implements OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        view.findViewById(R.id.settingsRowController).setOnClickListener(this);
        view.findViewById(R.id.settingsRowSource).setOnClickListener(this);
        view.findViewById(R.id.settingsRowTeam).setOnClickListener(this);
        view.findViewById(R.id.settingsRowNotice).setOnClickListener(this);

        view.findViewById(R.id.settingsStartController).setOnClickListener(this);

        String version = this.getString(R.string.version) + " ";
        try {
            version += this.getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(SampleAppActivity.TAG, "Cannot retrieve package version!!!");
        }
        ((TextView) view.findViewById(R.id.settingsTextSampleApp)).setText(version);

        int moduleScenesV1TextID = ((SampleAppActivity)getActivity()).basicSceneV1Module.isModuleInstalled() ? R.string.value_installed_yes : R.string.value_installed_no;
        ((TextView) view.findViewById(R.id.settingsTextModuleScenesV1)).setText(getString(moduleScenesV1TextID));

        onUpdateView();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        ((SampleAppActivity)getActivity()).updateActionBar(R.string.action_settings, false, false, false, false, false);
    }

    public void onUpdateView() {
        SampleAppActivity activity = (SampleAppActivity)getActivity();

        LightingController bundledController = LightingController.get();
        String bundledControllerName = bundledController.getName();
        if (bundledControllerName == null || bundledControllerName.isEmpty()) {
            bundledControllerName = "N/A";
        }

        Controller leader = LightingDirector.get().getLeadController();
        String leaderName = leader != null && leader.isConnected() ? leader.getName() : getString(R.string.default_controller_name);

        if (leader != null && leader.isConnected()) {
            long leaderVersion = leader.getVersion();

            if (leaderVersion > 0) {
                leaderName = leader.getName() + " (V" + leader.getVersion() + ")";
            } else {
                leaderName = leader.getName();
            }

            if (bundledController.isRunning() && leader.getName().equals(bundledControllerName)) {
                leaderName += " (this)";
            }
        } else {
            leaderName = getString(R.string.default_controller_name);
        }

        ((TextView)view.findViewById(R.id.settingsTextController)).setText(leaderName);

        if (bundledController.isRunning()) {
            boolean isLeader = bundledController.isLeader();

            if (isLeader) {
                bundledControllerName += " (leader)";
            }
        }

        ((TextView) view.findViewById(R.id.settingsTextBundledController)).setText(bundledControllerName);

        CheckBox checkBox = (CheckBox)view.findViewById(R.id.settingsStartController);
        checkBox.setChecked(activity.isControllerServiceEnabled());
        checkBox.setText(activity.isControllerServiceStarted() ? R.string.controller_enable_started : R.string.controller_enable_stopped);
    }

    @Override
    public void onClick(View clickedView) {
        int clickedID = clickedView.getId();

        if (clickedID == R.id.settingsRowController) {
            //TODO implement controller name change
        } else if (clickedID == R.id.settingsRowSource) {
            onSourceClick();
        } else if (clickedID == R.id.settingsRowTeam) {
            onTeamClick();
        } else if (clickedID == R.id.settingsRowNotice) {
            onNoticeClick();
        } else if (clickedID == R.id.settingsStartController) {
            onEnableControllerClick(((CheckBox)clickedView).isChecked());
        }
    }

    protected void onNoticeClick() {
        parent.showTextChildFragment(getResources().getString(R.string.notice_text));
    }

    protected void onSourceClick() {
        parent.showTextChildFragment(getResources().getString(R.string.source_code_text));
    }

    protected void  onTeamClick() {
        parent.showTextChildFragment(getResources().getString(R.string.team_text));
    }

    protected void onEnableControllerClick(boolean enableControllerService) {
        ((SampleAppActivity)getActivity()).setControllerServiceEnabled(enableControllerService);
    }
}
