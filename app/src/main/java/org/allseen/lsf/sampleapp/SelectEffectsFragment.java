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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.LightingItem;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;

public abstract class SelectEffectsFragment extends SelectableItemTableFragment {
    protected int labelStringID;
    protected Set<String> selectedItems;

    public SelectEffectsFragment(int labelStringID) {
        this.labelStringID = labelStringID;
    }

    protected boolean showPresets() {
        return showAll();
    }

    protected boolean showTransitionEffects() {
        return showAll();
    }

    protected boolean showPulseEffects() {
        return showAll();
    }

    protected boolean showAll() {
        return false;
    }

    //TODO-REF Common
    protected boolean isAddMode() {
        String pendingSceneElementID = getPendingItemID();

        return pendingSceneElementID == null || pendingSceneElementID.isEmpty();
    }

    @Override
    protected boolean isExclusive() {
        return true;
    }

    @Override
    protected boolean isItemSelected(String itemID) {
        return (selectedItems != null) && (selectedItems.contains(itemID));
    }

    @Override
    protected int getTableRowLayout() {
        return R.layout.view_selectable_effect_row;
    }

    @Override
    protected int getSelectButtonDrawableID() {
        return R.drawable.checkbox;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        getPendingSelection();
        onUpdateView(inflater, root);

        return root;
    }

    public void onUpdateView() {
        onUpdateView(getActivity().getLayoutInflater(), view);
    }

    public void onUpdateView(LayoutInflater inflater, View root) {
        LightingDirector director = LightingDirector.get();
        String pendingItemID = getPendingItemID();

        if (pendingItemID == null) {
            pendingItemID = "";
        }

        if (showPresets()) {
            addItems(director.getPresets(), pendingItemID, inflater, root, R.drawable.list_constant_icon, R.string.effect_name_preset);
        }

        if (showTransitionEffects()) {
            addItems(director.getTransitionEffects(), pendingItemID, inflater, root, R.drawable.list_transition_icon, R.string.effect_name_transition);
        }

        if (showPulseEffects()) {
            addItems(director.getPulseEffects(), pendingItemID, inflater, root, R.drawable.list_pulse_icon, R.string.effect_name_pulse);
        }
    }

    //TODO-REF Common w/SelectMembersFragment
    protected void addItems(LightingItem[] items, String pendingItemID, LayoutInflater inflater, View root, int imageID, int detailsID) {
        for (LightingItem item : items) {
            String itemID = item.getId();

            if (!pendingItemID.equals(itemID)) {
                updateSelectableItemRow(inflater, root, itemID, item.getTag(), R.drawable.nav_more_menu_icon, getString(detailsID), isItemSelected(itemID));

                TableRow tableRow = (TableRow)table.findViewWithTag(itemID);
                if (tableRow != null) {
                    ((ImageButton)tableRow.findViewById(R.id.selectableItemRowIcon)).setBackgroundResource(imageID);
                    ((TextView)tableRow.findViewById(R.id.selectableItemRowTextDetails)).setText(item.getName());
                } else {
                    Log.w(SampleAppActivity.TAG, "Missing row: " + itemID);
                }
            }
        }
    }

    @Override
    public void onActionAdd() {
        SceneElementV2SelectEffectTypeFragment.clearPendingEffects();

        ((ScenesPageFragment)parent).showSelectEffectTypeChildFragment();
    }

    //TODO-REF Common w/SelectMembersFragment
    @Override
    public void onActionDone() {
        if (processSelection()) {
            if (isAddMode()) {
                parent.clearBackStack();
            } else {
                parent.popBackStack(PageFrameParentFragment.CHILD_TAG_INFO);
            }
        }
    }

    //TODO-REF Abstract w/SelectMembersFragment?
    protected void getPendingSelection() {
        selectedItems = null;

        if (showPresets()) {
            addToSelection(getPendingPresetIDs());
        }

        if (showTransitionEffects()) {
            addToSelection(getPendingTransitionEffectIDs());
        }

        if (showPulseEffects()) {
            addToSelection(getPendingPulseEffectIDs());
        }
    }

    //TODO-REF Common w/SelectMembersFragment
    protected void addToSelection(String[] pendingItemIDs) {
        addToSelection(pendingItemIDs != null ? Arrays.asList(pendingItemIDs) : null);
    }

    //TODO-REF Common w/SelectMembersFragment
    protected void addToSelection(Collection<String> pendingItemIDs) {
        if (pendingItemIDs != null) {
            if (selectedItems == null) {
                selectedItems = new HashSet<String>();
            }

            selectedItems.addAll(pendingItemIDs);
        }
    }

    //TODO-REF Common w/SelectMembersFragment
    protected Collection<String> getPendingPresetIDs() {
        return null;
    }

    protected Collection<String> getPendingTransitionEffectIDs() {
        return null;
    }

    protected Collection<String> getPendingPulseEffectIDs() {
        return null;
    }

    protected boolean processPresetID(String presetID, List<String> presetIDs) {
        return processLightingItem(LightingDirector.get().getPreset(presetID), presetIDs);
    }

    protected boolean processTransitionEffectID(String transitionEffectID, List<String> transitionEffectIDs) {
        return processLightingItem(LightingDirector.get().getTransitionEffect(transitionEffectID), transitionEffectIDs);
    }

    protected boolean processPulseEffectID(String pulseEffectID, List<String> pulseEffectIDs) {
        return processLightingItem(LightingDirector.get().getPulseEffect(pulseEffectID), pulseEffectIDs);
    }

    //TODO-REF Common w/SelectMembersFragment
    protected boolean processLightingItem(LightingItem item, List<String> itemIDs) {
        boolean found = item != null;

        if (found) {
            itemIDs.add(item.getId());
        }

        return found;
    }

    protected boolean processSelection() {
        SampleAppActivity activity = (SampleAppActivity)getActivity();

        List<String> selectedItemIDs = getSelectedIDs();

        List<String> presetIDs = new ArrayList<String>();
        List<String> transitionEffectIDs = new ArrayList<String>();
        List<String> pulseEffectIDs = new ArrayList<String>();

        for (int index = 0; index < selectedItemIDs.size(); index++) {
            String itemID = selectedItemIDs.get(index);

            if (processPresetID(itemID, presetIDs)) {
                Log.d(SampleAppActivity.TAG, "Adding preset ID: " + itemID);
            } else if (processTransitionEffectID(itemID, transitionEffectIDs)) {
                Log.d(SampleAppActivity.TAG, "Adding transition effect ID: " + itemID);
            } else if (processPulseEffectID(itemID, pulseEffectIDs)) {
                Log.d(SampleAppActivity.TAG, "Adding pulse effect ID: " + itemID);
            } else {
                Log.w(SampleAppActivity.TAG, "Couldn't find itemID " + itemID);
            }
        }

        int count = presetIDs.size() + transitionEffectIDs.size() + pulseEffectIDs.size();
        boolean valid = count > 0;

        if (valid) {
            processSelection(activity, presetIDs, transitionEffectIDs, pulseEffectIDs);
        } else {
            String text = String.format(getResources().getString(R.string.toast_members_missing), getResources().getString(labelStringID));
            activity.showToast(text);
        }

        return valid;
    }

    //TODO-REF Common w/SelectMembersFragment
    protected abstract String getPendingItemID();

    //TODO-REF Common w/SelectMembersFragment
    protected abstract void processSelection(SampleAppActivity activity, List<String> presetIDs, List<String> transitionEffectIDs, List<String> pulseEffectIDs);
}
