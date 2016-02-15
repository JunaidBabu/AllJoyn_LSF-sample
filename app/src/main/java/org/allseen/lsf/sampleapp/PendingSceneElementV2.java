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

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.allseen.lsf.sdk.LampCapabilities;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.Preset;
import org.allseen.lsf.sdk.PulseEffect;
import org.allseen.lsf.sdk.SceneElement;
import org.allseen.lsf.sdk.ColorAverager;
import org.allseen.lsf.sdk.TransitionEffect;

import android.util.Log;

public class PendingSceneElementV2 extends PendingLightingItem {
    public static String LOCAL_ID_PREFIX = "_";

    public Collection<String> groups;
    public Collection<String> lamps;

    public LampCapabilities capability;
    public ColorAverager colorTempAverager;

    public int minColorTemp;
    public int maxColorTemp;
    public boolean hasEffects;

    public PendingPresetEffect pendingPresetEffect;
    public PendingTransitionEffectV2 pendingTransitionEffect;
    public PendingPulseEffectV2 pendingPulseEffect;

    public static boolean isLocalID(String sceneElementID) {
        return sceneElementID.startsWith(LOCAL_ID_PREFIX);
    }

    public PendingSceneElementV2() {
        this((SceneElement)null);
    }

    public PendingSceneElementV2(SceneElement sceneElement) {
        super.init(sceneElement);

        groups = sceneElement != null ? sceneElement.getGroupIDs() : new ArrayList<String>();
        lamps = sceneElement != null ? sceneElement.getLampIDs() : new ArrayList<String>();

        capability = new LampCapabilities();
        colorTempAverager = new ColorAverager();

        //TODO-FIX
        minColorTemp = -1;
        maxColorTemp = -1;
        hasEffects = false;

        String effectID = sceneElement != null ? sceneElement.getEffectID() : null;

        if (effectID != null) {
            LightingDirector director = LightingDirector.get();
            Preset preset = director.getPreset(effectID);

            if (preset == null) {
                TransitionEffect transitionEffect = director.getTransitionEffect(effectID);

                if (transitionEffect == null) {
                    PulseEffect pulseEffect = director.getPulseEffect(effectID);

                    if (pulseEffect == null) {
                        Log.e(SampleAppActivity.TAG, "Unknown effect with ID " + effectID);
                    } else {
                        pendingPresetEffect = null;
                        pendingTransitionEffect = null;
                        pendingPulseEffect = new PendingPulseEffectV2(pulseEffect);
                    }
                } else {
                    pendingPresetEffect = null;
                    pendingTransitionEffect = new PendingTransitionEffectV2(transitionEffect);
                    pendingPulseEffect = null;
                }
            } else {
                pendingPresetEffect = new PendingPresetEffect(preset);
                pendingTransitionEffect = null;
                pendingPulseEffect = null;
            }
        }
    }

    public PendingSceneElementV2(PendingSceneElementV2 pendingSceneElement) {
        super.init(pendingSceneElement.id, pendingSceneElement.name);

        groups = new ArrayList<String>(pendingSceneElement.groups);
        lamps = new ArrayList<String>(pendingSceneElement.lamps);

        capability = new LampCapabilities(pendingSceneElement.capability);
        colorTempAverager = new ColorAverager();//TODO-CHK Should this be initialized?

        minColorTemp = pendingSceneElement.minColorTemp;
        maxColorTemp = pendingSceneElement.maxColorTemp;
        hasEffects = pendingSceneElement.hasEffects;

        if (pendingSceneElement.pendingPresetEffect != null) {
            pendingPresetEffect = new PendingPresetEffect(pendingSceneElement.pendingPresetEffect);
        } else if (pendingSceneElement.pendingTransitionEffect != null) {
            pendingTransitionEffect = new PendingTransitionEffectV2(pendingSceneElement.pendingTransitionEffect);
        } else if (pendingSceneElement.pendingPulseEffect != null) {
            pendingPulseEffect = new PendingPulseEffectV2(pendingSceneElement.pendingPulseEffect);
        } else {
            Log.e(SampleAppActivity.TAG, "No effect in scene element " + id);
        }

    }

    @Override
    protected String ensureValidID(String pendingID) {
        if (pendingID == null || pendingID.isEmpty()) {
            pendingID = LOCAL_ID_PREFIX + UUID.randomUUID().toString();
        }

        return pendingID;
    }

    public boolean isAddMode() {
        return isLocalID(id);
    }
}
