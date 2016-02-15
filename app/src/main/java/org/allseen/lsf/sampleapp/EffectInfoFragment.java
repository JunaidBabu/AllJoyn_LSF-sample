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

import org.allseen.lsf.sdk.Color;
import org.allseen.lsf.sdk.Effect;
import org.allseen.lsf.sdk.LampCapabilities;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.MyLampState;
import org.allseen.lsf.sdk.Preset;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.SeekBar;

public abstract class EffectInfoFragment extends DimmableItemInfoFragment {
    public static final String STATE2_ITEM_TAG = "STATE2";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        ((SampleAppActivity)getActivity()).updateActionBar(getTitleID(), false, false, false, true, true);
    }

    // This should be called at the end of a leaf subclass's onCreateView() method
    protected void initLampState() {
        int lampStateCount = getLampStateCount();

        for (int lampStateIndex = 0; lampStateIndex < lampStateCount; lampStateIndex++) {
            MyLampState pendingLampState = getPendingLampState(lampStateIndex);

            updateColorTempMin(pendingLampState);
            updateLampState(pendingLampState, lampStateIndex);
        }
    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();

        if (viewID == R.id.statusButtonPower) {
            onHeaderClick();
        } else {
            super.onClick(view);
        }
    }

    @Override
    protected void onHeaderClick() {
        // TODO-FIX dialog to change effect type here
    }

    @Override
    protected MyLampState getItemLampState(String itemID) {
        return getPendingLampState(0);
    }

    protected void updateColorTempMin(MyLampState pendingLampState) {
        Color pendingColor = pendingLampState.getColor();
        int colorTempMin = getColorTempMin();

        if (pendingColor.getColorTemperature() < colorTempMin) {
            pendingColor.setColorTemperature(colorTempMin);
        }
    }

    protected void updateLampState(MyLampState pendingLampState, int lampStateIndex) {
        String pendingPresetID = getPendingPresetID(lampStateIndex);

        if (pendingPresetID != null && !pendingPresetID.equals(Effect.PRESET_ID_USE_CURRENT_STATE)) {
            Preset preset = LightingDirector.get().getPreset(pendingPresetID);

            if (preset != null) {
                Color presetColor = preset.getColor();

                pendingLampState.setPowerOn(preset.isOn());
                pendingLampState.setColorHsvt(presetColor.getHue(), presetColor.getSaturation(), presetColor.getBrightness(), presetColor.getColorTemperature());
            } else {
                setPendingPresetID(lampStateIndex, null);
            }
        }
    }

    // Override parent to update the pending lamp state rather than call the activity
    @Override
    public void setField(SeekBar seekBar) {
        int seekBarID = seekBar.getId();
        int stateIndex = getIndexForTag(seekBar.getTag());

        MyLampState pendingState = getPendingLampState(stateIndex);

        if (seekBarID == R.id.stateSliderBrightness) {
            pendingState.getColor().setBrightness(seekBar.getProgress());
        } else if (seekBarID == R.id.stateSliderHue) {
            pendingState.getColor().setHue(seekBar.getProgress());
        } else if (seekBarID == R.id.stateSliderSaturation) {
            pendingState.getColor().setSaturation(seekBar.getProgress());
        } else if (seekBarID == R.id.stateSliderColorTemp) {
            pendingState.getColor().setColorTemperature(seekBar.getProgress() + getColorTempMin());
        }

        LampStateViewAdapter viewAdapter = getLampStateViewAdapter(stateIndex);

        updatePresetFields(pendingState, viewAdapter);
        setPendingPresetID(stateIndex, getMatchingPreset(pendingState));

        LampCapabilities pendingCapability = new LampCapabilities(getPendingMembersCapability());
        setColorIndicator(viewAdapter.stateView, pendingState, pendingCapability, getColorTempDefault());
    }

    protected String getMatchingPreset(MyLampState itemState) {
        for (Preset preset : LightingDirector.get().getPresets()) {
            if (preset.stateEquals(itemState)) {
                return preset.getId();
            }
        }

        return null;
    }

    // Overriding getLampStateCount() implies overriding getIndexForTag() also
    protected int getLampStateCount() {
        return 1;
    }

    // Overriding getIndexForTag() implies overriding getLampStateCount() also
    protected int getIndexForTag(Object tag) {
        if (tag != STATE_ITEM_TAG) {
            // This shouldn't happen (the tag should have been recognized by a subclass)
            Log.e(SampleAppActivity.TAG, "Invalid effect state tag: " + tag);
        }

        return 0;
    }

    protected void logErrorOnInvalidIndex(int index) {
        if (index < 0 || index >= getLampStateCount()) {
            Log.e(SampleAppActivity.TAG, "Invalid effect state index: " + index);
        }
    }

    protected LampStateViewAdapter getLampStateViewAdapter(int index) {
        logErrorOnInvalidIndex(index);

        return stateAdapter;
    }

    protected abstract int getTitleID();
    protected abstract MyLampState getPendingLampState(int index);

    //TODO these should move down to EFfectV1InfoFragment
    protected abstract String getPendingPresetID(int index);
    protected abstract void setPendingPresetID(int index, String presetID);

    protected abstract LampCapabilities getPendingMembersCapability();
}
