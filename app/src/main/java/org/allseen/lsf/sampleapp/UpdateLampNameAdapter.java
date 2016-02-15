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

import org.allseen.lsf.sdk.Lamp;
import org.allseen.lsf.sdk.LightingDirector;

public class UpdateLampNameAdapter extends UpdateItemNameAdapter {
    public UpdateLampNameAdapter(Lamp lamp, SampleAppActivity activity) {
        super(lamp, activity);
    }

    @Override
    protected String getDuplicateNameMessage() {
        return activity.getString(R.string.duplicate_name_message_lamp);
    }

    @Override
    protected boolean duplicateName(String lampName) {
        return Util.isDuplicateName(LightingDirector.get().getLamps(), lampName);
    }
}
