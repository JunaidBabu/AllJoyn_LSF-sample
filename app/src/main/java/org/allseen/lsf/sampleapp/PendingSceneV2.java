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
import java.util.List;

import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.SceneElement;
import org.allseen.lsf.sdk.SceneV2;

import android.util.Log;

public class PendingSceneV2 extends PendingLightingItem {
    public List<PendingSceneElementV2> deleted = new ArrayList<PendingSceneElementV2>();
    public List<PendingSceneElementV2> current = new ArrayList<PendingSceneElementV2>();

    public PendingSceneV2() {
        this.init((SceneV2)null);
    }

    public PendingSceneV2(SceneV2 scene) {
        super.init(scene);

        if (scene != null) {
            LightingDirector director = LightingDirector.get();

            for (String sceneElementID : scene.getSceneElementIDs()) {
                SceneElement sceneElement = director.getSceneElement(sceneElementID);
                PendingSceneElementV2 pendingSceneElement = new PendingSceneElementV2(sceneElement);

                current.add(pendingSceneElement);
            }
        }
    }

    public boolean isAddMode() {
        return id == null || id.isEmpty();
    }

    public void doDeleteSceneElement(String sceneElementID) {
        int index = -1;

        for (int i = 0; index < 0 && i < current.size(); i++) {
            if (sceneElementID.equals(current.get(i).id)) {
                index = i;
            }
        }

        if (index >=0) {
            PendingSceneElementV2 deletedSceneElement = current.remove(index);

            if (!PendingSceneElementV2.isLocalID(sceneElementID)) {
                deleted.add(deletedSceneElement);
            }
        } else {
            Log.w(SampleAppActivity.TAG, "Could not delete scene element ID " + sceneElementID);
        }
    }
}
