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
import org.allseen.lsf.sdk.Preset;

public class PendingPresetEffect extends PendingLightingItem {
    public final LampStateUniformity uniformity = new LampStateUniformity();

    public MyLampState state;

    public PendingPresetEffect() {
        this((Preset)null);
    }

    public PendingPresetEffect(Preset preset) {
        init(preset);

        // Note the lamp state must have its own instance of a Color object
        state = preset != null ? preset.getState() : new MyLampState(Power.ON, new Color(Color.defaultColor()));
    }

    public PendingPresetEffect(PendingPresetEffect pendingPresetEffect) {
        init(pendingPresetEffect.id, pendingPresetEffect.name);

        state = new MyLampState(pendingPresetEffect.state);
    }
}
