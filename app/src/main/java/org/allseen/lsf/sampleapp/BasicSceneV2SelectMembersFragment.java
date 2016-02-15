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

import java.util.List;

import org.allseen.lsf.sampleapp.R;
import org.allseen.lsf.sampleapp.SampleAppActivity;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.SceneElement;
import org.allseen.lsf.sdk.SceneV2;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

public class BasicSceneV2SelectMembersFragment extends SelectMembersFragment {

    public BasicSceneV2SelectMembersFragment() {
        super(R.string.label_basic_scene);
    }

    @Override
    protected boolean showSceneElements() {
        return true;
    }

    @Override
    protected String getHeaderText() {
        return getString(R.string.basic_scene_select_elements);
    }

    @Override
    protected String[] getPendingSceneElements() {
        return null; //TODO-FIX BasicSceneV2InfoFragment.pendingSceneV2.sceneElementIDs;
    }

    @Override
    protected String getPendingItemID() {
        return BasicSceneV2InfoFragment.pendingSceneV2.id;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        ((SampleAppActivity)getActivity()).updateActionBar(R.string.title_basic_scene_add, false, false, false, true, true);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.selectHeader) {
            //TODO-FIX
        } else if (view.getId() == R.id.selectableItemButtonIcon) {
            ((SampleAppActivity)getActivity()).showSceneElementMorePopup(view, getPendingItemID(), view.getTag().toString(), true);
        } else {
            super.onClick(view);
        }
    }

    @Override
    protected void processSelection(SampleAppActivity activity, List<String> lampIDs, List<String> groupIDs, List<String> presetIDs, List<String> transitionEffectIDs, List<String> pulseEffectIDs, List<String> sceneElementIDs, List<String> sceneIDs) {
        LightingDirector director = LightingDirector.get();
        SceneElement[] sceneElements = director.getSceneElements(sceneElementIDs);

        if (!isAddMode()) {
            ((SceneV2)director.getScene(getPendingItemID())).modify(sceneElements);
        } else {
            director.createScene(sceneElements, BasicSceneV2InfoFragment.pendingSceneV2.name);
        }
    }

    @Override
    protected int getMixedSelectionMessageID() {
        return R.string.mixing_lamp_types_message_scene;
    }

    @Override
    protected int getMixedSelectionPositiveButtonID() {
        return R.string.create_scene;
    }

    //TODO-REF Common
    protected boolean isAddMode() {
        String pendingItemID = getPendingItemID();

        return pendingItemID == null || pendingItemID.isEmpty();
    }
}
