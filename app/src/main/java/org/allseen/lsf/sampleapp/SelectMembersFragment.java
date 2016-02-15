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

import org.allseen.lsf.sdk.ColorItem;
import org.allseen.lsf.sdk.Group;
import org.allseen.lsf.sdk.LampCapabilities;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.LightingItem;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class SelectMembersFragment extends SelectableItemTableFragment {

    protected int labelStringID;
    protected Set<String> selectedItems;

    public SelectMembersFragment(int labelStringID) {
        this.labelStringID = labelStringID;
    }

    protected boolean showLamps() {
        return !(showPresets() || showTransitionEffects() || showPulseEffects() || showScenes() || showSceneElements());
    }

    protected boolean showGroups() {
        return showLamps();
    }

    protected boolean showPresets() {
        return false;
    }

    protected boolean showTransitionEffects() {
        return false;
    }

    protected boolean showPulseEffects() {
        return false;
    }

    protected boolean showSceneElements() {
        return false;
    }

    protected boolean showScenes() {
        return false;
    }

    protected boolean confirmMixedSelection() {
        return true;
    }

    @Override
    protected boolean isItemSelected(String itemID) {
        return (selectedItems != null) && (selectedItems.contains(itemID));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        LightingDirector director = LightingDirector.get();
        String pendingItemID = getPendingItemID();

        if (pendingItemID == null) {
            pendingItemID = "";
        }

        getPendingSelection();

        if (showLamps()) {
            addItems(director.getLamps(), pendingItemID, inflater, root, R.drawable.group_lightbulb_icon);
        }

        if (showGroups()) {
            addGroups(director.getGroups(), pendingItemID, inflater, root, R.drawable.scene_lightbulbs_icon);
        }

        if (showPresets()) {
            addItems(director.getPresets(), pendingItemID, inflater, root, R.drawable.list_constant_icon);
        }

        if (showTransitionEffects()) {
            addItems(director.getTransitionEffects(), pendingItemID, inflater, root, R.drawable.list_transition_icon);
        }

        if (showPulseEffects()) {
            addItems(director.getPulseEffects(), pendingItemID, inflater, root, R.drawable.list_pulse_icon);
        }

        if (showSceneElements()) {
            addItems(director.getSceneElements(), pendingItemID, inflater, root, R.drawable.scene_element_set_icon);
        }

        if (showScenes()) {
            addItems(director.getScenes(), pendingItemID, inflater, root, R.drawable.scene_set_icon);
        }

        return root;
    }

    protected void addGroups(Group[] groups, String pendingItemID, LayoutInflater inflater, View view, int iconID) {
        for (Group group : groups) {
            String groupID = group.getId();

            // Filter out the All Lamps group and any parent groups
            if (!pendingItemID.equals(groupID) && !group.isAllLampsGroup() && !isParentOfPendingGroup(group)) {
                updateSelectableItemRow(inflater, view, groupID, group.getTag(), iconID, group.getName(), isItemSelected(groupID));
            }
        }
    }

    protected void addItems(LightingItem[] items, String pendingItemID, LayoutInflater inflater, View view, int iconID) {
        for (LightingItem item : items) {
            String itemID = item.getId();

            if (!pendingItemID.equals(itemID)) {
                updateSelectableItemRow(inflater, view, itemID, item.getTag(), iconID, item.getName(), isItemSelected(itemID));
            }
        }
    }

    @Override
    public void onActionDone() {
        if (processSelection()) {
            parent.clearBackStack();
        }
    }

    protected boolean isParentOfPendingGroup(Group group) {
        return false;
    }

    protected void getPendingSelection() {
        selectedItems = null;

        if (showLamps()) {
            addToSelection(getPendingLampIDs());
        }

        if (showGroups()) {
            addToSelection(getPendingGroupIDs());
        }

        if (showPresets()) {
            addToSelection(getPendingPresetIDs());
        }

        if (showTransitionEffects()) {
            addToSelection(getPendingTransitionEffectIDs());
        }

        if (showPulseEffects()) {
            addToSelection(getPendingPulseEffectIDs());
        }

        if (showSceneElements()) {
            addToSelection(getPendingSceneElements());
        }

        if (showScenes()) {
            addToSelection(getPendingScenes());
        }
    }

    protected void addToSelection(String[] pendingItemIDs) {
        addToSelection(pendingItemIDs != null ? Arrays.asList(pendingItemIDs) : null);
    }

    protected void addToSelection(Collection<String> pendingItemIDs) {
        if (pendingItemIDs != null) {
            if (selectedItems == null) {
                selectedItems = new HashSet<String>();
            }

            selectedItems.addAll(pendingItemIDs);
        }
    }

    protected String getPendingItemID() {
        return null;
    }

    protected Collection<String> getPendingLampIDs() {
        return null;
    }

    protected Collection<String> getPendingGroupIDs() {
        return null;
    }

    protected Collection<String> getPendingPresetIDs() {
        return null;
    }

    protected Collection<String> getPendingTransitionEffectIDs() {
        return null;
    }

    protected Collection<String> getPendingPulseEffectIDs() {
        return null;
    }

    protected String[] getPendingSceneElements() {
        return null;
    }

    protected String[] getPendingScenes() {
        return null;
    }

    protected boolean processLampID(SampleAppActivity activity, String lampID, List<String> lampIDs, LampCapabilities capability) {
        return processCapabilityItem(LightingDirector.get().getLamp(lampID), lampIDs, capability);
    }

    protected boolean processGroupID(SampleAppActivity activity, String groupID, List<String> groupIDs, LampCapabilities capability) {
        return processCapabilityItem(LightingDirector.get().getGroup(groupID), groupIDs, capability);
    }

    protected boolean processPresetID(SampleAppActivity activity, String presetID, List<String> presetIDs, LampCapabilities capability) {
        return processLightingItem(LightingDirector.get().getPreset(presetID), presetIDs);
    }

    protected boolean processTransitionEffectID(SampleAppActivity activity, String transitionEffectID, List<String> transitionEffectIDs, LampCapabilities capability) {
        return processLightingItem(LightingDirector.get().getTransitionEffect(transitionEffectID), transitionEffectIDs);
    }

    protected boolean processPulseEffectID(SampleAppActivity activity, String pulseEffectID, List<String> pulseEffectIDs, LampCapabilities capability) {
        return processLightingItem(LightingDirector.get().getPulseEffect(pulseEffectID), pulseEffectIDs);
    }

    protected boolean processSceneElementID(SampleAppActivity activity, String sceneElementID, List<String> sceneElementIDs, LampCapabilities capability) {
        return processLightingItem(LightingDirector.get().getSceneElement(sceneElementID), sceneElementIDs);
    }

    protected boolean processSceneID(SampleAppActivity activity, String sceneID, List<String> sceneIDs, LampCapabilities capability) {
        return processLightingItem(LightingDirector.get().getScene(sceneID), sceneIDs);
    }

    protected boolean processCapabilityItem(ColorItem item, List<String> itemIDs, LampCapabilities capability) {
        boolean found = processLightingItem(item, itemIDs);

        if (found) {
            capability.includeData(item.getCapability());
        }

        return found;
    }

    protected boolean processLightingItem(LightingItem item, List<String> itemIDs) {
        boolean found = item != null;

        if (found) {
            itemIDs.add(item.getId());
        }

        return found;
    }

    protected boolean processSelection() {
        SampleAppActivity activity = (SampleAppActivity)getActivity();
        LampCapabilities capability = new LampCapabilities();

        List<String> selectedItemIDs = getSelectedIDs();

        List<String> lampIDs = new ArrayList<String>();
        List<String> groupIDs = new ArrayList<String>();
        List<String> presetIDs = new ArrayList<String>();
        List<String> transitionEffectIDs = new ArrayList<String>();
        List<String> pulseEffectIDs = new ArrayList<String>();
        List<String> sceneElementIDs = new ArrayList<String>();
        List<String> sceneIDs = new ArrayList<String>();

        for (int index = 0; index < selectedItemIDs.size(); index++) {
            String itemID = selectedItemIDs.get(index);

            if (processLampID(activity, itemID, lampIDs, capability)) {
                Log.d(SampleAppActivity.TAG, "Adding lamp ID: " + itemID);
            } else if (processGroupID(activity, itemID, groupIDs, capability)) {
                Log.d(SampleAppActivity.TAG, "Adding group ID: " + itemID);
            } else if (processPresetID(activity, itemID, presetIDs, capability)) {
                Log.d(SampleAppActivity.TAG, "Adding preset ID: " + itemID);
            } else if (processTransitionEffectID(activity, itemID, transitionEffectIDs, capability)) {
                Log.d(SampleAppActivity.TAG, "Adding transition effect ID: " + itemID);
            } else if (processPulseEffectID(activity, itemID, pulseEffectIDs, capability)) {
                Log.d(SampleAppActivity.TAG, "Adding pulse effect ID: " + itemID);
            } else if (processSceneElementID(activity, itemID, sceneElementIDs, capability)) {
                Log.d(SampleAppActivity.TAG, "Adding scene element ID: " + itemID);
            } else if (processSceneID(activity, itemID, sceneIDs, capability)) {
                Log.d(SampleAppActivity.TAG, "Adding scene ID: " + itemID);
            } else {
                Log.w(SampleAppActivity.TAG, "Couldn't find itemID " + itemID);
            }
        }

        int count = lampIDs.size() + groupIDs.size() + presetIDs.size() + transitionEffectIDs.size() + pulseEffectIDs.size() + sceneElementIDs.size() + sceneIDs.size();
        boolean valid = count > 0;

        if (valid) {
            processSelection(activity, lampIDs, groupIDs, presetIDs, transitionEffectIDs, pulseEffectIDs, sceneElementIDs, sceneIDs, capability);
        } else {
            String text = String.format(getResources().getString(R.string.toast_members_missing), getResources().getString(labelStringID));
            activity.showToast(text);
        }

        return valid;
    }

    protected void processSelection(final SampleAppActivity activity, final List<String> lampIDs, final List<String> groupIDs, final List<String> presetIDs, final List<String> transitionEffectIDs, final List<String> pulseEffectIDs, final List<String> sceneElementIDs, final List<String> sceneIDs, LampCapabilities capability) {
        if (confirmMixedSelection() && capability.isMixed()) {
            // detected a mixed group of lamps
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(R.string.mixing_lamp_types);
            alertDialogBuilder.setMessage(getMixedSelectionMessageID());
            alertDialogBuilder.setPositiveButton(getMixedSelectionPositiveButtonID(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    processSelection(activity, lampIDs, presetIDs, transitionEffectIDs, pulseEffectIDs, groupIDs, sceneElementIDs, sceneIDs);
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            // create alert dialog and show it
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            processSelection(activity, lampIDs, groupIDs, presetIDs, transitionEffectIDs, pulseEffectIDs, sceneElementIDs, sceneIDs);
        }
    }

    protected abstract void processSelection(SampleAppActivity activity, List<String> lampIDs, List<String> groupIDs, final List<String> presetIDs, final List<String> transitionEffectIDs, final List<String> pulseEffectIDs, List<String> sceneElementIDs, List<String> sceneIDs);
    protected abstract int getMixedSelectionMessageID();
    protected abstract int getMixedSelectionPositiveButtonID();
}
