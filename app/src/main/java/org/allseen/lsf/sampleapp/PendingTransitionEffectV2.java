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
import org.allseen.lsf.sdk.TransitionEffect;

public class PendingTransitionEffectV2 extends PendingLightingItem {
    public MyLampState state;
    public String presetID;
    public long duration;

    public final LampStateUniformity uniformity = new LampStateUniformity();

    public PendingTransitionEffectV2() {
        this((TransitionEffect)null);
    }

    public PendingTransitionEffectV2(TransitionEffect transitionEffect) {
        init(transitionEffect);

        // Note the state must have its own instance of a Color object
        state = transitionEffect != null ? transitionEffect.getState() : new MyLampState(Power.ON, new Color(Color.defaultColor()));
        presetID = transitionEffect != null ? transitionEffect.getPresetID() : "";
        duration = transitionEffect != null ? transitionEffect.getDuration() : 5000;
    }

    public PendingTransitionEffectV2(PendingTransitionEffectV2 pendingTransitionEffect) {
        init(pendingTransitionEffect.id, pendingTransitionEffect.name);

        state = new MyLampState(pendingTransitionEffect.state);
        presetID = pendingTransitionEffect.presetID;
        duration = pendingTransitionEffect.duration;
    }
}
