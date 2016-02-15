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

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class EnterCountFragment extends EnterNumberFragment {
    public static long count;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ((TextView) view.findViewById(R.id.enterNumberUnits)).setText(getString(R.string.units_pulses));
        return view;
    }

    @Override
    protected int getTitleID() {
        return R.string.title_effect_pulse_count;
    }

    @Override
    protected int getLabelID() {
        return R.string.label_enter_count;
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    }

    @Override
    protected String getNumberString() {
        return String.valueOf(EnterCountFragment.count);
    }

    @Override
    protected double getScale() {
        return 1.0;
    }

    @Override
    protected boolean setNumberValue(long numberValue) {
        EnterCountFragment.count = numberValue;

        if (PulseEffectV2Fragment.pendingPulseEffect != null) {
            PulseEffectV2Fragment.pendingPulseEffect.count = numberValue;
        }

        // Go back to the effect info display
        getActivity().onBackPressed();

        return true;
    }
}

