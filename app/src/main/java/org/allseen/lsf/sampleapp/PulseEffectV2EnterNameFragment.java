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
import android.util.Log;

public class PulseEffectV2EnterNameFragment extends EnterNameFragment {

    public PulseEffectV2EnterNameFragment() {
        super(R.string.label_pulse_effect);
    }

    @Override
    protected int getTitleID() {
        return R.string.title_effect_pulse_add;
    }

    @Override
    protected void setName(String name) {
        PulseEffectV2Fragment.pendingPulseEffect.name = name;

        Log.d(SampleAppActivity.TAG, "Pending pulse effect name: " + PulseEffectV2Fragment.pendingPulseEffect.name);
    }

    @Override
    protected void showNextFragment() {
        ((ScenesPageFragment)parent).showPulseEffectChildFragment();
    }

    @Override
    protected String getDuplicateNameMessage() {
        return this.getString(R.string.duplicate_name_message_pulse_effect);
    }

    @Override
    protected boolean duplicateName(String pulseEffectName) {
        return Util.isDuplicateName(LightingDirector.get().getPulseEffects(), pulseEffectName);
    }
}
