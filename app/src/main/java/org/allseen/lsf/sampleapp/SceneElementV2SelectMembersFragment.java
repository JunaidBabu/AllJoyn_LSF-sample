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

import java.util.Collection;

import org.allseen.lsf.sdk.LampCapabilities;
import org.allseen.lsf.sdk.ColorAverager;
import android.view.Menu;
import android.view.MenuInflater;

public class SceneElementV2SelectMembersFragment extends SceneItemSelectMembersFragment {

    public SceneElementV2SelectMembersFragment() {
        super(R.string.label_scene_element);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        SampleAppActivity activity = (SampleAppActivity)getActivity();

        activity.updateActionBar(isAddMode() ? R.string.title_scene_element_add : R.string.title_scene_element_edit, false, false, true, false, true);
    }

    @Override
    protected String getHeaderText() {
        return getString(R.string.scene_element_select_members);
    }

    @Override
    protected String getPendingItemID() {
        return SceneElementV2InfoFragment.pendingSceneElement.id;
    }

    @Override
    protected int getMixedSelectionMessageID() {
        return R.string.mixing_lamp_types_message_scene_element;
    }

    @Override
    protected int getMixedSelectionPositiveButtonID() {
        return R.string.create_scene_element;
    }

    @Override
    protected Collection<String> getPendingGroupIDs() {
        return SceneElementV2InfoFragment.pendingSceneElement.groups;
    }

    @Override
    protected Collection<String> getPendingLampIDs() {
        return SceneElementV2InfoFragment.pendingSceneElement.lamps;
    }

    @Override
    protected void setPendingMemberIDs(Collection<String> lampIDs, Collection<String> groupIDs) {
        SceneElementV2InfoFragment.pendingSceneElement.lamps = lampIDs;
        SceneElementV2InfoFragment.pendingSceneElement.groups = groupIDs;
    }

    @Override
    protected void setPendingCapability(LampCapabilities capability) {
        SceneElementV2InfoFragment.pendingSceneElement.capability = capability;
    }

    @Override
    protected boolean getPendingMembersHaveEffects() {
        return SceneElementV2InfoFragment.pendingSceneElement.hasEffects;
    }

    @Override
    protected void setPendingMembersHaveEffects(boolean haveEffects) {
        SceneElementV2InfoFragment.pendingSceneElement.hasEffects = haveEffects;
    }

    @Override
    protected int getPendingMembersMinColorTemp() {
        return SceneElementV2InfoFragment.pendingSceneElement.minColorTemp;
    }

    @Override
    protected void setPendingMembersMinColorTemp(int minColorTemp) {
        SceneElementV2InfoFragment.pendingSceneElement.minColorTemp = minColorTemp;
    }

    @Override
    protected int getPendingMembersMaxColorTemp() {
        return SceneElementV2InfoFragment.pendingSceneElement.maxColorTemp;
    }

    @Override
    protected void setPendingMembersMaxColorTemp(int maxColorTemp) {
        SceneElementV2InfoFragment.pendingSceneElement.maxColorTemp = maxColorTemp;
    }

    @Override
    protected ColorAverager getPendingColorTempAverager() {
        return SceneElementV2InfoFragment.pendingSceneElement.colorTempAverager;
    }

    @Override
    protected void updatePendingColorTempAverager(int viewColorTemp) {
        getPendingColorTempAverager().add(viewColorTemp);
    }

    @Override
    public void onActionNext() {
        PendingSceneElementV2 pendingSceneElement = SceneElementV2InfoFragment.pendingSceneElement;

        if (processSelection()) {
            if (getPendingMembersHaveEffects()) {
                if (pendingSceneElement.pendingPresetEffect != null) {
                    PresetEffectFragment.pendingPresetEffect = new PendingPresetEffect(pendingSceneElement.pendingPresetEffect);
                    ((ScenesPageFragment)parent).showConstantEffectChildFragment();
                } else if (pendingSceneElement.pendingTransitionEffect != null) {
                    TransitionEffectV2Fragment.pendingTransitionEffect = new PendingTransitionEffectV2(pendingSceneElement.pendingTransitionEffect);
                    ((ScenesPageFragment)parent).showTransitionEffectChildFragment();
                } else if (pendingSceneElement.pendingPulseEffect != null) {
                    PulseEffectV2Fragment.pendingPulseEffect = new PendingPulseEffectV2(pendingSceneElement.pendingPulseEffect);
                    ((ScenesPageFragment)parent).showPulseEffectChildFragment();
                } else {
                    ((ScenesPageFragment)parent).showSelectEffectTypeChildFragment();
                }
            } else {
                if (pendingSceneElement.pendingPresetEffect != null) {
                    PresetEffectFragment.pendingPresetEffect = new PendingPresetEffect(pendingSceneElement.pendingPresetEffect);
                } else {
                    PresetEffectFragment.pendingPresetEffect = new PendingPresetEffect();
                }

                ((ScenesPageFragment)parent).showConstantEffectChildFragment();
            }
        }
    }

    //TODO-REF Common
    protected boolean isAddMode() {
        String pendingSceneElementID = getPendingItemID();

        return pendingSceneElementID == null || pendingSceneElementID.isEmpty();
    }
}
