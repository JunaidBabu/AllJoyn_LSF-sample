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

public class BasicSceneV2EnterNameFragment extends EnterNameFragment {

    public BasicSceneV2EnterNameFragment() {
        super(R.string.label_basic_scene);
    }

    @Override
    protected int getTitleID() {
        return R.string.title_basic_scene_add;
    }

    @Override
    protected void setName(String name) {
        BasicSceneV2InfoFragment.pendingSceneV2.name = name;

        Log.d(SampleAppActivity.TAG, "Pending scene name: " + name);
    }

    @Override
    protected String getDuplicateNameMessage() {
        return this.getString(R.string.duplicate_name_message_scene_element);
    }

    @Override
    protected boolean duplicateName(String sceneElementName) {
        return Util.isDuplicateName(LightingDirector.get().getSceneElements(), sceneElementName);
    }

    @Override
    protected void showNextFragment() {
        SceneElementV2InfoFragment.pendingSceneElement = new PendingSceneElementV2();

        super.showNextFragment();
    }
}
