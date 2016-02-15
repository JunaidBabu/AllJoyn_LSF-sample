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

import org.allseen.lsf.sampleapp.DimmableItemPresetsFragment;
import org.allseen.lsf.sdk.MyLampState;
import org.allseen.lsf.sdk.Preset;

import android.util.Log;

public class SceneElementV2PresetsFragment extends DimmableItemPresetsFragment {

    @Override
    protected MyLampState getItemLampState() {
        MyLampState lampState;

        if (PresetEffectFragment.pendingPresetEffect != null) {
            lampState = PresetEffectFragment.pendingPresetEffect.state;
        } else if (TransitionEffectV2Fragment.pendingTransitionEffect != null) {
            lampState = TransitionEffectV2Fragment.pendingTransitionEffect.state;
        } else if (key2 != PulseEffectV2Fragment.STATE2_ITEM_TAG) {
            lampState = PulseEffectV2Fragment.pendingPulseEffect.startState;
        } else {
            lampState = PulseEffectV2Fragment.pendingPulseEffect.endState;
        }

        return lampState;
    }

    @Override
    protected void doApplyPreset(Preset preset) {
        if (PresetEffectFragment.pendingPresetEffect != null) {
            Log.e(SampleAppActivity.TAG, "Cannot assign a preset to a preset");
        } else if (TransitionEffectV2Fragment.pendingTransitionEffect != null) {
            TransitionEffectV2Fragment.pendingTransitionEffect.presetID = preset.getId();
            TransitionEffectV2Fragment.pendingTransitionEffect.state = preset.getState();
        } else if (key2 != PulseEffectV2Fragment.STATE2_ITEM_TAG) {
            PulseEffectV2Fragment.pendingPulseEffect.startPresetID = preset.getId();
            PulseEffectV2Fragment.pendingPulseEffect.startState = preset.getState();
        } else {
            PulseEffectV2Fragment.pendingPulseEffect.endPresetID = preset.getId();
            PulseEffectV2Fragment.pendingPulseEffect.endState = preset.getState();
        }
    }
}
