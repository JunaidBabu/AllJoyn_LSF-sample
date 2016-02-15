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
import java.util.Arrays;
import java.util.List;

import org.allseen.lsf.sdk.Color;
import org.allseen.lsf.sdk.Effect;
import org.allseen.lsf.sdk.GroupMember;
import org.allseen.lsf.sdk.LampState;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.LightingItem;
import org.allseen.lsf.sdk.LightingItemErrorEvent;
import org.allseen.lsf.sdk.MyLampState;
import org.allseen.lsf.sdk.Power;
import org.allseen.lsf.sdk.Preset;
import org.allseen.lsf.sdk.PulseEffect;
import org.allseen.lsf.sdk.SceneElement;
import org.allseen.lsf.sdk.SceneV2;
import org.allseen.lsf.sdk.TrackingID;
import org.allseen.lsf.sdk.TransitionEffect;

import android.util.Log;

public class BasicSceneV2TransactionManager {
    protected PendingSceneV2 pendingScene;
    protected List<SceneElement> sceneElements;

    public BasicSceneV2TransactionManager(PendingSceneV2 pendingScene ) {
        this.pendingScene = pendingScene;

        sceneElements = new ArrayList<SceneElement>(pendingScene.current.size());
    }

    public void start() {
        LightingDirector director = LightingDirector.get();

        for (PendingSceneElementV2 pendingSceneElement : pendingScene.current) {
            createOrModify(director, pendingSceneElement);
        }
    }

    protected void createOrModify(LightingDirector director, PendingSceneElementV2 pendingSceneElement) {
        String nameSuffix = pendingSceneElement.id.substring(PendingSceneElementV2.LOCAL_ID_PREFIX.length(), 8);

        if (pendingSceneElement.pendingPresetEffect != null) {
            createOrModify(director, pendingSceneElement, pendingSceneElement.pendingPresetEffect, nameSuffix);
        } else if (pendingSceneElement.pendingTransitionEffect != null) {
            createOrModify(director, pendingSceneElement, pendingSceneElement.pendingTransitionEffect, nameSuffix);
        } else if (pendingSceneElement.pendingPulseEffect != null) {
            createOrModify(director, pendingSceneElement, pendingSceneElement.pendingPulseEffect, nameSuffix);
        }
    }

    protected void createOrModify(LightingDirector director, PendingSceneElementV2 pendingSceneElement, PendingPresetEffect pendingPresetEffect, String nameSuffix) {
        Power effectPower = pendingPresetEffect.state.getPower();
        Color effectColor = pendingPresetEffect.state.getColor();

        if (pendingSceneElement.isAddMode()) {
            String effectName = "LSF_PRE_" + nameSuffix;

            create(director, pendingSceneElement, director.createPreset(effectPower, effectColor, effectName), nameSuffix);
        } else {
            Preset effect = director.getPreset(pendingPresetEffect.id);

            if (effect != null) {
                effect.modify(effectPower, effectColor);

                modify(director, pendingSceneElement, effect);
            } else {
                Log.e(SampleAppActivity.TAG, "Missing preset " + pendingPresetEffect.name);
            }
        }
    }

    protected void createOrModify(LightingDirector director, PendingSceneElementV2 pendingSceneElement, PendingTransitionEffectV2 pendingTransitionEffect, String nameSuffix) {
        LampState effectState = getEffectState(director, pendingTransitionEffect.presetID, pendingTransitionEffect.state);
        long effectDuration = pendingTransitionEffect.duration;

        if (pendingSceneElement.isAddMode()) {
            String effectName = "LSF_TRN_" + nameSuffix;

            create(director, pendingSceneElement, director.createTransitionEffect(effectState, effectDuration, effectName), nameSuffix);
        } else {
            TransitionEffect effect = director.getTransitionEffect(pendingTransitionEffect.id);

            if (effect != null) {
                effect.modify(effectState, effectDuration);

                modify(director, pendingSceneElement, effect);
            } else {
                Log.e(SampleAppActivity.TAG, "Missing transition effect " + pendingTransitionEffect.name);
            }
        }
    }

    protected void createOrModify(LightingDirector director, PendingSceneElementV2 pendingSceneElement, PendingPulseEffectV2 pendingPulseEffect, String nameSuffix) {
        LampState effectStartState = getEffectState(director, pendingPulseEffect.startPresetID, pendingPulseEffect.startState);
        LampState effectEndState = getEffectState(director, pendingPulseEffect.endPresetID, pendingPulseEffect.endState);

        long effectPeriod = pendingPulseEffect.period;
        long effectDuration = pendingPulseEffect.duration;
        long effectCount = pendingPulseEffect.count;

        if (pendingSceneElement.isAddMode()) {
            String effectName = "LSF_PLS_" + nameSuffix;

            create(director, pendingSceneElement, director.createPulseEffect(effectStartState, effectEndState, effectPeriod, effectDuration, effectCount, effectName), nameSuffix);
        } else {
            PulseEffect effect = director.getPulseEffect(pendingPulseEffect.id);

            if (effect != null) {
                effect.modify(effectStartState, effectEndState, effectPeriod, effectDuration, effectCount);

                modify(director, pendingSceneElement, effect);
            } else {
                Log.e(SampleAppActivity.TAG, "Missing pulse effect " + pendingPulseEffect.name);
            }
        }
    }

    protected void create(final LightingDirector director, final PendingSceneElementV2 pendingSceneElement, final TrackingID effectTrackingID, final String nameSuffix) {
        LightingListenerUtil.listenFor(effectTrackingID, new TrackingIDListener() {
            @Override
            public void onTrackingIDReceived(TrackingID trackingID, LightingItem item) {
                String sceneElementName = "LSF_SEL_" + nameSuffix;

                LightingListenerUtil.listenFor(director.createSceneElement((Effect)item, getMembers(director, pendingSceneElement), sceneElementName), new TrackingIDListener() {
                    @Override
                    public void onTrackingIDReceived(TrackingID trackingID, LightingItem item) {
                        onSceneElementCompletion((SceneElement)item);
                    }

                    @Override
                    public void onTrackingIDError(LightingItemErrorEvent error) {
                        Log.e(SampleAppActivity.TAG, "Scene element tracking ID error: " + error.itemID);
                    }
                });
            }

            @Override
            public void onTrackingIDError(LightingItemErrorEvent error) {
                Log.e(SampleAppActivity.TAG, "Preset tracking ID error: " + error.itemID);
            }
        });
    }

    protected void modify(LightingDirector director, PendingSceneElementV2 pendingSceneElement, Effect effect) {
        SceneElement sceneElement = director.getSceneElement(pendingSceneElement.id);

        if (sceneElement != null) {
            sceneElement.modify(effect, getMembers(director, pendingSceneElement));
            onSceneElementCompletion(sceneElement);
        } else {
            Log.e(SampleAppActivity.TAG, "Missing scene element " + pendingSceneElement.id);
        }
    }

    protected void delete(LightingDirector director, PendingSceneElementV2 deletedSceneElement) {
        SceneElement sceneElement = director.getSceneElement(deletedSceneElement.id);

        if (sceneElement != null) {
            Effect effect = sceneElement.getEffect();

            sceneElement.delete();

            if (effect != null) {
                effect.delete();
            } else {
                Log.w(SampleAppActivity.TAG, "Effect deletion failed for scene element id" + deletedSceneElement.id);
            }
        } else {
            Log.w(SampleAppActivity.TAG, "Scene element deletion failed for id" + deletedSceneElement.id);
        }
    }

    protected void onSceneElementCompletion(SceneElement sceneElement) {
        sceneElements.add(sceneElement);

        int count = sceneElements.size();

        if (count >= pendingScene.current.size()) {
            LightingDirector director = LightingDirector.get();
            SceneElement[] sceneElementArray = sceneElements.toArray(new SceneElement[count]);

            if (pendingScene.isAddMode()) {
                director.createScene(sceneElementArray, pendingScene.name);
            } else {
                ((SceneV2)director.getScene(pendingScene.id)).modify(sceneElementArray);
            }

            // Deletions must be processed after all of the effects, scene elements, and
            // the parent scene have been created or modified. This is so there are no
            // longer any dependencies on the items to be deleted.
            for (PendingSceneElementV2 deletedSceneElement : pendingScene.deleted) {
                delete(director, deletedSceneElement);
            }
        }
    }

    protected GroupMember[] getMembers(LightingDirector director, PendingSceneElementV2 pendingSceneElement) {
        ArrayList<GroupMember> memberList = new ArrayList<GroupMember>();

        memberList.addAll(Arrays.asList(director.getLamps(pendingSceneElement.lamps)));
        memberList.addAll(Arrays.asList(director.getGroups(pendingSceneElement.groups)));

        return memberList.toArray(new GroupMember[memberList.size()]);
    }

    protected LampState getEffectState(LightingDirector director, String pendingPresetID, MyLampState pendingEffectstate) {
        LampState effectState = null;

        if (pendingPresetID != null) {
            effectState = director.getPreset(pendingPresetID);
        }

        if (effectState == null) {
            effectState = pendingEffectstate;
        }

        return effectState;
    }
}
