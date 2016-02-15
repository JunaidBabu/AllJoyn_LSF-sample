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

import org.allseen.lsf.sampleapp.R;
import org.allseen.lsf.sdk.Effect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class SceneItemSelectEffectTypeFragment extends SelectableItemTableFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        updateSelectableItemRow(inflater, root, Effect.EFFECT_TYPE_PRESET, getResources().getString(R.string.effect_sort_constant), R.drawable.list_constant_icon, getResources().getString(getPresetEffectNameID()), true);
        updateSelectableItemRow(inflater, root, Effect.EFFECT_TYPE_TRANSITION, getResources().getString(R.string.effect_sort_transition), R.drawable.list_transition_icon, getResources().getString(R.string.effect_name_transition), false);
        updateSelectableItemRow(inflater, root, Effect.EFFECT_TYPE_PULSE, getResources().getString(R.string.effect_sort_pulse), R.drawable.list_pulse_icon, getResources().getString(R.string.effect_name_pulse), false);

        return root;
    }

    @Override
    protected String getHeaderText() {
        SampleAppActivity activity = (SampleAppActivity)getActivity();
        String members = Util.formatMemberNamesString(activity, getPendingSceneElementMemberLampIDs(), getPendingSceneElementMemberGroupIDs(), MemberNamesOptions.en, 3, getPendingSceneItemName());

        return String.format(getString(R.string.basic_scene_select_effect), members);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        ((SampleAppActivity)getActivity()).updateActionBar(R.string.title_effect_add, false, false, true, false, true);
    }

    @Override
    protected boolean isExclusive() {
        return true;
    }

    protected abstract int getPresetEffectNameID();
    protected abstract String getPendingSceneItemName();
    protected abstract String[] getPendingSceneElementMemberLampIDs();
    protected abstract String[] getPendingSceneElementMemberGroupIDs();
}
