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
import org.allseen.lsf.sdk.ColorItem;
import org.allseen.lsf.sdk.LampCapabilities;
import org.allseen.lsf.sdk.LampStateUniformity;
import org.allseen.lsf.sdk.MyLampState;

import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public abstract class DimmableItemInfoFragment extends PageFrameChildFragment implements View.OnClickListener {
    public static final String STATE_ITEM_TAG = "STATE";
    public static int defaultIndicatorColor = 00000000;

    protected SampleAppActivity.Type itemType = SampleAppActivity.Type.LAMP;
    protected LampStateViewAdapter stateAdapter;

    protected View statusView;
    protected View stateView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String itemID = key;

        view = inflater.inflate(getLayoutID(), container, false);
        statusView = view.findViewById(R.id.infoStatusRow);
        stateView = view.findViewById(R.id.infoStateRow);

        // power button
        ImageButton powerButton = (ImageButton)statusView.findViewById(R.id.statusButtonPower);
        powerButton.setTag(itemID);
        powerButton.setOnClickListener(this);

        // item name
        TextView nameLabel = (TextView)statusView.findViewById(R.id.statusLabelName);
        nameLabel.setClickable(true);
        nameLabel.setOnClickListener(this);

        TextView nameText = (TextView)statusView.findViewById(R.id.statusTextName);
        nameText.setClickable(true);
        nameText.setOnClickListener(this);

        // presets button
        Button presetsButton = (Button)stateView.findViewById(R.id.stateButton);
        presetsButton.setTag(STATE_ITEM_TAG);
        presetsButton.setClickable(true);
        presetsButton.setOnClickListener(this);

        // state adapter
        stateAdapter = new LampStateViewAdapter(stateView, getLampStateViewAdapterTag(), getColorTempMin(), getColorTempSpan(), this);

        return view;
    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();

        if (viewID == R.id.statusButtonPower) {
            ((SampleAppActivity)getActivity()).togglePower(itemType, key);
        } else if (viewID == R.id.statusLabelName || viewID == R.id.statusTextName) {
            onHeaderClick();
        } else if (viewID == R.id.stateButton) {
            parent.showPresetsChildFragment(key, view.getTag().toString());
        }
    }

    public void setField(SeekBar seekBar) {
        int seekBarID = seekBar.getId();

        if (seekBarID == R.id.stateSliderBrightness) {
            ((SampleAppActivity)getActivity()).setBrightness(itemType, seekBar.getTag().toString(), seekBar.getProgress());
        } else if (seekBarID == R.id.stateSliderHue) {
            ((SampleAppActivity)getActivity()).setHue(itemType, seekBar.getTag().toString(), seekBar.getProgress());
        } else if (seekBarID == R.id.stateSliderSaturation) {
            ((SampleAppActivity)getActivity()).setSaturation(itemType, seekBar.getTag().toString(), seekBar.getProgress());
        } else if (seekBarID == R.id.stateSliderColorTemp) {
            ((SampleAppActivity)getActivity()).setColorTemp(itemType, seekBar.getTag().toString(), seekBar.getProgress() + getColorTempMin());
        }
    }

    public void updateInfoFields(ColorItem colorItem) {
        if (colorItem.getId().equals(key)) {
            updateInfoFields(colorItem.getName(), colorItem.getState(), colorItem.getCapability(), colorItem.getUniformity());
        }
    }

    protected void updateInfoFields(String name, MyLampState state, LampCapabilities capability, LampStateUniformity uniformity) {
        Color color = state.getColor();

        if (uniformity.power) {
            setImageButtonBackgroundResource(statusView, R.id.statusButtonPower, state.isOn() ? R.drawable.power_button_on : R.drawable.power_button_off);
        } else {
            setImageButtonBackgroundResource(statusView, R.id.statusButtonPower, R.drawable.power_button_mix);
        }

        setTextViewValue(statusView, R.id.statusTextName, name, 0);

        stateAdapter.setBrightness(color.getBrightness(), uniformity.brightness);
        stateAdapter.setHue(color.getHue(), uniformity.hue);
        stateAdapter.setSaturation(color.getSaturation(), uniformity.saturation);
        stateAdapter.setColorTemp(color.getColorTemperature(), uniformity.colorTemp);

        // presets button
        updatePresetFields(state);

        setColorIndicator(stateAdapter.stateView, state, capability, getColorTempDefault());
    }

    public void updatePresetFields() {
        updatePresetFields(getItemLampState(key));
    }

    public void updatePresetFields(MyLampState itemState) {
        updatePresetFields(itemState, stateAdapter);
    }

    public void updatePresetFields(MyLampState itemState, LampStateViewAdapter itemAdapter) {
        itemAdapter.setPreset(Util.createPresetNamesString((SampleAppActivity)getActivity(), itemState));
    }

    public void setColorIndicator(View parentStateView, MyLampState lampState, LampCapabilities capability, int viewColorTempDefault) {
        int color = lampState != null ? ViewColor.calculate(lampState, capability, viewColorTempDefault) : defaultIndicatorColor;

        parentStateView.findViewById(R.id.stateRowColorIndicator).getBackground().setColorFilter(color, Mode.MULTIPLY);
    }

    protected String getLampStateViewAdapterTag() {
        return key;
    }

    protected abstract int getLayoutID();
    protected abstract int getColorTempMin();
    protected abstract int getColorTempSpan();
    protected abstract int getColorTempDefault();
    protected abstract void onHeaderClick();
    protected abstract MyLampState getItemLampState(String itemID);
}
