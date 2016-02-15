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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.allseen.lsf.sdk.ColorAverager;
import org.allseen.lsf.sdk.Group;
import org.allseen.lsf.sdk.Lamp;
import org.allseen.lsf.sdk.LampCapabilities;
import org.allseen.lsf.sdk.LampDetails;
import org.allseen.lsf.sdk.LightingDirector;

public abstract class SceneItemSelectMembersFragment extends SelectMembersFragment {
    public SceneItemSelectMembersFragment(int labelStringID) {
        super(labelStringID);
    }

    @Override
    protected void processSelection(SampleAppActivity activity, List<String> lampIDs, List<String> groupIDs, List<String> presetIDs, List<String> transitionEffectIDs, List<String> pulseEffectIDs, List<String> sceneElementIDs, List<String> sceneIDs, LampCapabilities capability) {
        setPendingCapability(capability);

        super.processSelection(activity, lampIDs, groupIDs, presetIDs, transitionEffectIDs, pulseEffectIDs, sceneElementIDs, sceneIDs, capability);
    }

    @Override
    protected void processSelection(SampleAppActivity activity, List<String> lampIDs, List<String> groupIDs, List<String> presetIDs, List<String> transitionEffectIDs, List<String> pulseEffectIDs, List<String> sceneElementIDs, List<String> sceneIDs) {
        setPendingMemberIDs(lampIDs, groupIDs);

        setPendingMembersHaveEffects(false);
        setPendingMembersMinColorTemp(-1);
        setPendingMembersMaxColorTemp(-1);

        getPendingColorTempAverager().reset();

        processGroupSelection(activity, groupIDs);
        processLampSelection(activity, lampIDs);

        if (getPendingMembersMinColorTemp() == -1) {
            setPendingMembersMinColorTemp(LightingDirector.COLORTEMP_MIN);
        }

        if (getPendingMembersMaxColorTemp() == -1) {
            setPendingMembersMaxColorTemp(LightingDirector.COLORTEMP_MAX);
        }
    }

    protected void processGroupSelection(SampleAppActivity activity, List<String> groupIDs) {
        if (groupIDs.size() > 0) {
            for (Iterator<String> it = groupIDs.iterator(); it.hasNext();) {
                processGroupSelection(activity, it.next());
            }
        }
    }

    protected void processGroupSelection(SampleAppActivity activity, String groupID) {
        Group group = LightingDirector.get().getGroup(groupID);

        if (group != null) {
            processLampSelection(activity, group.getLampIDs());
        }
    }

    protected void processLampSelection(SampleAppActivity activity, Collection<String> lampIDs) {
        if (lampIDs.size() > 0) {
            for (Iterator<String> it = lampIDs.iterator(); it.hasNext();) {
                Lamp lamp = LightingDirector.get().getLamp(it.next());

                if (lamp != null) {
                    LampDetails lampDetails = lamp.getDetails();

                    if (lampDetails != null) {
                        boolean lampHasEffects = lampDetails.hasEffects();
                        boolean lampHasVariableColorTemp = lampDetails.hasVariableColorTemp();
                        int lampMinTemperature = lampDetails.getMinTemperature();
                        int lampMaxTemperature = lampHasVariableColorTemp ? lampDetails.getMaxTemperature() : lampMinTemperature;
                        boolean lampValidColorTempMin = lampMinTemperature >= LightingDirector.COLORTEMP_MIN && lampMinTemperature <= LightingDirector.COLORTEMP_MAX;
                        boolean lampValidColorTempMax = lampMaxTemperature >= LightingDirector.COLORTEMP_MIN && lampMaxTemperature <= LightingDirector.COLORTEMP_MAX;

                        if (lampHasEffects) {
                            setPendingMembersHaveEffects(true);
                        }

                        if (lampHasVariableColorTemp) {
                            updatePendingColorTempAverager(lamp.getColor().getColorTemperature());
                        } else if (lampValidColorTempMin) {
                            updatePendingColorTempAverager(lampMinTemperature);
                        }

                        if (lampValidColorTempMin && lampValidColorTempMax) {
                            if (lampMinTemperature > getPendingMembersMinColorTemp() || getPendingMembersMinColorTemp() == -1) {
                                setPendingMembersMinColorTemp(lampMinTemperature);
                            }

                            if (lampMaxTemperature < getPendingMembersMaxColorTemp() || getPendingMembersMaxColorTemp() == -1) {
                                setPendingMembersMaxColorTemp(lampMaxTemperature);
                            }
                        }
                    }
                }
            }
        }
    }

    protected abstract void setPendingCapability(LampCapabilities capability);
    protected abstract void setPendingMemberIDs(Collection<String> lampIDs, Collection<String> groupIDs);

    protected abstract boolean getPendingMembersHaveEffects();
    protected abstract void setPendingMembersHaveEffects(boolean haveEffects);

    protected abstract int getPendingMembersMinColorTemp();
    protected abstract void setPendingMembersMinColorTemp(int minColorTemp);

    protected abstract int getPendingMembersMaxColorTemp();
    protected abstract void setPendingMembersMaxColorTemp(int maxColorTemp);

    protected abstract ColorAverager getPendingColorTempAverager();
    protected abstract void updatePendingColorTempAverager(int viewColorTemp);
}
