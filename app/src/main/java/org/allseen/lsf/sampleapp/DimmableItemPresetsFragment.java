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

import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.MyLampState;
import org.allseen.lsf.sdk.Preset;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public abstract class DimmableItemPresetsFragment extends SelectableItemTableFragment implements ItemNameAdapter {

    protected boolean allowApply = false;
    protected String currentName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        TextView headerView = (TextView)view.findViewById(R.id.selectHeader);
        headerView.setClickable(true);
        headerView.setOnClickListener(this);

        onUpdateView(inflater, root);

        return root;
    }

    public void onUpdateView() {
        onUpdateView(getActivity().getLayoutInflater(), view);
    }

    public void onUpdateView(LayoutInflater inflater, View root) {
        Preset[] presets = LightingDirector.get().getPresets();

        table.removeAllViews();

        for (Preset preset : presets) {
            updateSelectableItemRow(inflater, root, preset.getId(), preset.getTag(), R.drawable.nav_more_menu_icon, preset.getName(), false);
        }

        view.findViewById(R.id.selectHeader).setVisibility(presets.length < LightingDirector.MAX_PRESETS ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void updateTableRows() {
        allowApply = false;
        super.updateTableRows();
        allowApply = true;
    }

    @Override
    public void removeElement(String id) {
        super.removeElement(id);

        view.findViewById(R.id.selectHeader).setVisibility(LightingDirector.get().getPresetCount() < LightingDirector.MAX_PRESETS ? View.VISIBLE : View.GONE);
    }

    @Override
    protected String getHeaderText() {
        return getString(R.string.title_presets_save_new);
    }

    @Override
    protected boolean isItemSelected(String presetID) {
        return isItemSelected(LightingDirector.get().getPreset(presetID));
    }

    protected boolean isItemSelected(Preset preset) {
        return preset.stateEquals(getItemLampState());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        ((SampleAppActivity)getActivity()).updateActionBar(R.string.title_presets, false, false, false, false, true);
    }

    @Override
    protected int getTableRowLayout() {
        return R.layout.view_selectable_preset_row;
    }

    @Override
    protected int getSelectButtonDrawableID() {
        return R.drawable.checkbox;
    }

    @Override
    protected boolean isExclusive() {
        return true;
    }

    @Override
    public void onClick(View view) {
        final ItemNameAdapter adapter = this;

        if (view.getId() == R.id.selectHeader) {
            currentName = null;
            ((SampleAppActivity)getActivity()).showItemNameDialog(R.string.title_presets_save_new, adapter);
        } else if (view.getId() == R.id.selectableItemButtonIcon) {
            ((SampleAppActivity)getActivity()).showPresetMorePopup(view, view.getTag().toString());
        } else {
            super.onClick(view);
        }
    }

    @Override
    public String getCurrentName() {
        SampleAppActivity activity = (SampleAppActivity)getActivity();

        // Default new preset name suffix is next available index (number of preset models + 1 for now)
        return currentName == null ? String.format(activity.getString(R.string.presets_new_name), LightingDirector.get().getPresetCount() + 1) : currentName;
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        EditText nameText = (EditText) (((AlertDialog) dialog).findViewById(R.id.itemNameText));
        final ItemNameAdapter adapter = this;

        currentName = nameText.getText().toString();

        if (duplicateName(currentName)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.duplicate_name);
            builder.setMessage(String.format(getString(R.string.duplicate_name_message_preset), currentName));
            builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doSavePreset(currentName);
                }
            });
            builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    ((SampleAppActivity)getActivity()).showItemNameDialog(R.string.title_presets_save_new, adapter);
                }
            });
            builder.create().show();
        } else {
            doSavePreset(currentName);
        }
    }

    private boolean duplicateName(String presetName) {
        return Util.isDuplicateName(LightingDirector.get().getPresets(), presetName);
    }

    @Override
    public void onCheckedChanged(CompoundButton selectButton, boolean checked) {
        super.onCheckedChanged(selectButton, checked);

        if (checked && allowApply) {
            doApplyPreset(selectButton.getTag().toString());
        }
    }

    protected void doSavePreset(String presetName) {
        MyLampState lampState = getItemLampState();

        if ((lampState != null) && (presetName != null) && (!presetName.isEmpty())) {
            doSavePreset(presetName, lampState);
        }
    }

    protected void doSavePreset(String presetName, MyLampState presetState) {
        if ((presetName != null) && (!presetName.isEmpty()) && (presetState != null)) {
            LightingDirector.get().createPreset(presetState.getPower(), presetState.getColor(), presetName);
        }

        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    protected void doApplyPreset(String presetID) {
        Preset preset = LightingDirector.get().getPreset(presetID);

        if (preset != null) {
            doApplyPreset(preset);
        }

        getActivity().onBackPressed();
    }

    protected abstract void doApplyPreset(Preset preset);
    protected abstract MyLampState getItemLampState();
}
