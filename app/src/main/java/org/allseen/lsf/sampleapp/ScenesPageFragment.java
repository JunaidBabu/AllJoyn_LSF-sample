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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScenesPageFragment extends PageMainContainerFragment {
    public static enum Mode {
        BASIC,
        MASTER
    }

    public static final String CHILD_TAG_SELECT_EFFECT = "SELECT_EFFECT";
    public static final String CHILD_TAG_SELECT_EFFECT_TYPE = "SELECT_EFFECT_TYPE";

    public static final String CHILD_TAG_CONSTANT_EFFECT = "CONSTANT_EFFECT";
    public static final String CHILD_TAG_TRANSITION_EFFECT = "TRANSITION_EFFECT";
    public static final String CHILD_TAG_PULSE_EFFECT = "PULSE_EFFECT";

    public static final String CHILD_TAG_EFFECT_NAME = "EFFECT_NAME";

    public static final String CHILD_TAG_EFFECT_DURATION = "EFFECT_DURATION";
    public static final String CHILD_TAG_EFFECT_PERIOD = "EFFECT_PERIOD";
    public static final String CHILD_TAG_EFFECT_COUNT = "EFFECT_COUNT";

    public static String TAG;

    protected Mode mode = Mode.BASIC;

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean isBasicMode() {
        return mode == Mode.BASIC;
    }

    public boolean isBasicModeV1() {
        return isBasicMode() && LightingDirector.get().isControllerServiceLeaderV1();
    }

    public boolean isBasicModeV2() {
        return isBasicMode() && !LightingDirector.get().isControllerServiceLeaderV1();
    }

    public boolean isMasterMode() {
        return mode == Mode.MASTER;
    }

    public void showSelectEffectChildFragment() {
        showChildFragment(CHILD_TAG_SELECT_EFFECT, null);
    }

    public void showSelectEffectTypeChildFragment() {
        showChildFragment(CHILD_TAG_SELECT_EFFECT_TYPE, null);
    }

    public void showEnterEffectNameChildFragment() {
        showChildFragment(CHILD_TAG_EFFECT_NAME, null);
    }

    public void showConstantEffectChildFragment() {
        showChildFragment(CHILD_TAG_CONSTANT_EFFECT, isBasicMode() ? getBasicSceneV1Module().getPendingNoEffectID() : PresetEffectFragment.pendingPresetEffect.id);
    }

    public void showTransitionEffectChildFragment() {
        showChildFragment(CHILD_TAG_TRANSITION_EFFECT, isBasicMode() ? getBasicSceneV1Module().getPendingTransitionEffectID() : TransitionEffectV2Fragment.pendingTransitionEffect.id);
    }

    public void showPulseEffectChildFragment() {
        showChildFragment(CHILD_TAG_PULSE_EFFECT, isBasicMode() ? getBasicSceneV1Module().getPendingPulseEffectID() : PulseEffectV2Fragment.pendingPulseEffect.id);
    }

    public void showEnterDurationChildFragment() {
        String tag;

        if (isBasicMode()) {
            BasicSceneV1ModuleProxy basicSceneV1Manager = getBasicSceneV1Module();
            tag = basicSceneV1Manager.isTransitionEffectPending() ? basicSceneV1Manager.getPendingTransitionEffectID() : basicSceneV1Manager.getPendingPulseEffectID();
        } else {
            tag = TransitionEffectV2Fragment.pendingTransitionEffect != null ? TransitionEffectV2Fragment.pendingTransitionEffect.id : PulseEffectV2Fragment.pendingPulseEffect.id;
        }

        showChildFragment(CHILD_TAG_EFFECT_DURATION, tag);
    }

    public void showEnterPeriodChildFragment() {
        showChildFragment(CHILD_TAG_EFFECT_PERIOD, isBasicMode() ? getBasicSceneV1Module().getPendingPulseEffectID() : PulseEffectV2Fragment.pendingPulseEffect.id);
    }

    public void showEnterCountChildFragment() {
        showChildFragment(CHILD_TAG_EFFECT_COUNT, isBasicMode() ? getBasicSceneV1Module().getPendingPulseEffectID() : PulseEffectV2Fragment.pendingPulseEffect.id);
    }

    @Override
    protected PageFrameChildFragment createChildFragment(String tag)
    {
        if (tag == CHILD_TAG_SELECT_EFFECT) {
            return createSelectEffectChildFragment();
        } else if (tag == CHILD_TAG_SELECT_EFFECT_TYPE) {
            return createSelectEffectTypeChildFragment();
        } else if (tag == CHILD_TAG_CONSTANT_EFFECT) {
            return createConstantEffectChildFragment();
        } else if (tag == CHILD_TAG_TRANSITION_EFFECT) {
            return createTransitionEffectChildFragment();
        } else if (tag == CHILD_TAG_PULSE_EFFECT) {
            return createPulseEffectChildFragment();
        } else if (tag == CHILD_TAG_EFFECT_NAME) {
            return createEnterEffectNameChildFragment();
        } else if (tag == CHILD_TAG_EFFECT_DURATION) {
            return createEnterDurationChildFragment();
        } else if (tag == CHILD_TAG_EFFECT_PERIOD) {
            return createEnterPeriodChildFragment();
        } else if (tag == CHILD_TAG_EFFECT_COUNT) {
            return createEnterCountChildFragment();
        } else {
            return super.createChildFragment(tag);
        }
    }

    @Override
    public PageFrameChildFragment createTableChildFragment() {
        return new ScenesTableFragment();
    }

    @Override
    public PageFrameChildFragment createInfoChildFragment() {
        return
            isMasterMode()  ? new MasterSceneInfoFragment() :
            isBasicModeV1() ? getBasicSceneV1Module().createBasicSceneInfoFragment() :
            isBasicModeV2() ? new BasicSceneV2InfoFragment() :
            null;
    }

    @Override
    public PageFrameChildFragment createPresetsChildFragment() {
        return
            isMasterMode()  ? null :
            isBasicModeV1() ? getBasicSceneV1Module().createSceneElementPresetsFragment() :
            isBasicModeV2() ? new SceneElementV2PresetsFragment() :
            null;
    }

    @Override
    public PageFrameChildFragment createEnterNameChildFragment() {
        return
            isMasterMode()  ? new MasterSceneEnterNameFragment() :
            isBasicModeV1() ? getBasicSceneV1Module().createBasicSceneEnterNameFragment() :
            isBasicModeV2() ? new BasicSceneV2EnterNameFragment() :
            null;
    }

    @Override
    public PageFrameChildFragment createSelectMembersChildFragment() {
        return
            isMasterMode()  ? new MasterSceneSelectMembersFragment() :
            isBasicModeV1() ? getBasicSceneV1Module().createBasicSceneSelectMembersFragment():
            isBasicModeV2() ? new SceneElementV2SelectMembersFragment() :
            null;
    }

    public PageFrameChildFragment createSelectEffectChildFragment() {
        return new SceneElementV2SelectEffectFragment();
    }

    public PageFrameChildFragment createEnterEffectNameChildFragment() {
        return
            PresetEffectFragment.pendingPresetEffect           != null ? new PresetEffectEnterNameFragment() :
            TransitionEffectV2Fragment.pendingTransitionEffect != null ? new TransitionEffectV2EnterNameFragment() :
            PulseEffectV2Fragment.pendingPulseEffect           != null ? new PulseEffectV2EnterNameFragment() :
            null;
    }

    public PageFrameChildFragment createSelectEffectTypeChildFragment() {
        return
            isMasterMode()  ? null :
            isBasicModeV1() ? getBasicSceneV1Module().createBasicSceneSelectEffectTypeFragment():
            isBasicModeV2() ? new SceneElementV2SelectEffectTypeFragment() :
            null;
    }

    public PageFrameChildFragment createConstantEffectChildFragment() {
        return
            isMasterMode()  ? null :
            isBasicModeV1() ? getBasicSceneV1Module().createNoEffectFragment():
            isBasicModeV2() ? new PresetEffectFragment() :
            null;
    }

    public PageFrameChildFragment createTransitionEffectChildFragment() {
        return
            isMasterMode()  ? null :
            isBasicModeV1() ? getBasicSceneV1Module().createTransitionEffectFragment():
            isBasicModeV2() ? new TransitionEffectV2Fragment() :
            null;
    }

    public PageFrameChildFragment createPulseEffectChildFragment() {
        return
            isMasterMode()  ? null :
            isBasicModeV1() ? getBasicSceneV1Module().createPulseEffectFragment():
            isBasicModeV2() ? new PulseEffectV2Fragment() :
            null;
    }

    public PageFrameChildFragment createEnterDurationChildFragment() {
        return new EnterDurationFragment();
    }

    public PageFrameChildFragment createEnterPeriodChildFragment() {
        return new EnterPeriodFragment();
    }

    public PageFrameChildFragment createEnterCountChildFragment() {
        return new EnterCountFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        ScenesPageFragment.TAG = getTag();

        return root;
    }

    @Override
    public int onBackPressed() {
        String startingChildTag = child.getTag();
        int backStackCount = super.onBackPressed();

        if (isBasicMode() && CHILD_TAG_ENTER_NAME.equals(startingChildTag)) {
            // To support the basic scene V1 creation workflow, when going backwards
            // from the enter name fragment we have to skip over the dummy scene
            // info fragment (see SampleAppActivity.doAddScene()). So we queue up
            // a second back press here.
            ((SampleAppActivity)getActivity()).postOnBackPressed();
        }

        return backStackCount;
    }

    private BasicSceneV1ModuleProxy getBasicSceneV1Module() {
        return ((SampleAppActivity)getActivity()).basicSceneV1Module;
    }
}
