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

import org.allseen.lsf.sdk.Lamp;
import org.allseen.lsf.sdk.LampParameters;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.MyLampState;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LampInfoFragment extends DimmableItemInfoFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        itemType = SampleAppActivity.Type.LAMP;

        ((TextView)statusView.findViewById(R.id.statusLabelName)).setText(R.string.label_lamp_name);

        // details
        view.findViewById(R.id.lampInfoTableRow5).setOnClickListener(this);

        updateInfoFields(LightingDirector.get().getLamp(key));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((SampleAppActivity)getActivity()).updateActionBar(R.string.title_lamp_info, false, false, false, false, true);
    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();

        if (viewID == R.id.lampInfoTableRow5) {
            ((SampleAppActivity)getActivity()).showLampDetailsFragment((LampsPageFragment)parent, key);
        } else {
            super.onClick(view);
        }
    }

    protected void updateInfoFields(Lamp lamp) {
        if (lamp.getId().equals(key)) {
            stateAdapter.setCapability(lamp.getCapability());
            super.updateInfoFields(lamp);

            LampParameters lampParams = lamp.getParameters();
            setTextViewValue(view, R.id.lampInfoTextLumens, lampParams.getLumens(), 0);
            setTextViewValue(view, R.id.lampInfoTextEnergy, lampParams.getEnergyUsageMilliwatts(), R.string.units_mw);
        }
    }

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_lamp_info;
    }

    @Override
    protected int getColorTempMin() {
        Lamp lamp = LightingDirector.get().getLamp(key);
        int colorTempMin = lamp != null ? lamp.getColorTempMin() : LightingDirector.COLORTEMP_MIN;

        return colorTempMin;
    }

    @Override
    protected int getColorTempSpan() {
        Lamp lamp = LightingDirector.get().getLamp(key);
        int colorTempMin = lamp != null ? lamp.getColorTempMin() : LightingDirector.COLORTEMP_MIN;
        int colorTempMax = lamp != null ? lamp.getColorTempMax() : LightingDirector.COLORTEMP_MAX;

        return colorTempMax - colorTempMin;
    }

    @Override
    protected int getColorTempDefault() {
        return getColorTempMin();
    }

    @Override
    protected void onHeaderClick() {
        SampleAppActivity activity = (SampleAppActivity)getActivity();
        Lamp lamp = LightingDirector.get().getLamp(key);

        activity.showItemNameDialog(R.string.title_group_rename, new UpdateLampNameAdapter(lamp, activity));
    }

    @Override
    protected MyLampState getItemLampState(String lampID){
        Lamp lamp = LightingDirector.get().getLamp(lampID);

        return lamp != null ? lamp.getState() : null;
    }
}
