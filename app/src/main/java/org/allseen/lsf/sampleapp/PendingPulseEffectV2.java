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
import org.allseen.lsf.sdk.LampStateUniformity;
import org.allseen.lsf.sdk.MyLampState;
import org.allseen.lsf.sdk.Power;
import org.allseen.lsf.sdk.PulseEffect;

public class PendingPulseEffectV2 extends PendingLightingItem {
    public boolean startWithCurrent;

    public MyLampState startState;
    public MyLampState endState;

    public String startPresetID;
    public String endPresetID;

    public long period;
    public long duration;
    public long count;

    public final LampStateUniformity uniformity = new LampStateUniformity();

    public PendingPulseEffectV2() {
        this((PulseEffect)null);
    }

    public PendingPulseEffectV2(PulseEffect pulseEffect) {
        init(pulseEffect);

        startWithCurrent = pulseEffect != null ? pulseEffect.isStartWithCurrent() : false;

        // Note the lamp states must have their own instance of a Color object
        startState = pulseEffect != null ? pulseEffect.getStartState() : new MyLampState(Power.ON, new Color(Color.defaultColor()));
        endState = pulseEffect != null ? pulseEffect.getEndState() : new MyLampState(Power.ON, new Color(Color.defaultColor()));

        startPresetID = pulseEffect != null ? pulseEffect.getStartPresetID() : "";
        endPresetID = pulseEffect != null ? pulseEffect.getEndPresetID() : "";

        period = pulseEffect != null ? pulseEffect.getPeriod() : 1000;
        duration = pulseEffect != null ? pulseEffect.getDuration() : 500;
        count = pulseEffect != null ? pulseEffect.getCount() : 10;
    }

    public PendingPulseEffectV2(PendingPulseEffectV2 pendingPulseEffect) {
        init(pendingPulseEffect.id, pendingPulseEffect.name);

        startWithCurrent = pendingPulseEffect.startWithCurrent;

        startState = new MyLampState(pendingPulseEffect.startState);
        endState = new MyLampState(pendingPulseEffect.endState);

        startPresetID = pendingPulseEffect.startPresetID;
        endPresetID = pendingPulseEffect.endPresetID;

        period = pendingPulseEffect.period;
        duration = pendingPulseEffect.duration;
        count = pendingPulseEffect.count;
    }
}
