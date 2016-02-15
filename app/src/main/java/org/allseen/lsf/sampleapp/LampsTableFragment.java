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
import org.allseen.lsf.sdk.LightingDirector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LampsTableFragment extends DimmableItemTableFragment {

    public LampsTableFragment() {
        super();
        type = SampleAppActivity.Type.LAMP;
    }

    @Override
    protected int getInfoButtonImageID() {
        return R.drawable.light_status_icon;
    }

    @Override
    protected Fragment getInfoFragment() {
        return new LampInfoFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        // We don't user super.addItems() here because we need to call
        // ViewColor.calculate() for each lamp.
        addLamps(LightingDirector.get().getLamps());

        return root;
    }

    public void addLamps(Lamp[] lamps) {
        for (Lamp lamp : lamps) {
            addLamp(lamp);
        }
    }

    public void addLamp(Lamp lamp) {
        addItem(lamp, ViewColor.calculate(lamp.getState(), lamp.getCapability(), lamp.getDetails()));
    }

    @Override
    public void removeElement(String id) {
        super.removeElement(id);
        updateLoading();
    }

    @Override
    public void updateLoading() {
        super.updateLoading();

        SampleAppActivity activity = (SampleAppActivity) getActivity();
        boolean hasLamps = LightingDirector.get().getLampCount() > 0;

        if (activity.isControllerConnected() && !hasLamps) {
            // connected but no lamps found; display loading lamps screen, hide the scroll table
            layout.findViewById(R.id.scrollLoadingView).setVisibility(View.VISIBLE);
            layout.findViewById(R.id.scrollScrollView).setVisibility(View.GONE);

            View loadingView = layout.findViewById(R.id.scrollLoadingView);

            ((TextView) loadingView.findViewById(R.id.loadingText1)).setText(activity.getText(R.string.no_lamps));
            ((TextView) loadingView.findViewById(R.id.loadingText2)).setText(activity.getText(R.string.loading_lamps));
        }
    }
}
