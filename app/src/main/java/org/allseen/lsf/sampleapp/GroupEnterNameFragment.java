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

public class GroupEnterNameFragment extends EnterNameFragment {

    public GroupEnterNameFragment() {
        super(R.string.label_group);
    }

    @Override
    protected int getTitleID() {
        return R.string.title_group_add;
    }

    @Override
    protected void setName(String name) {
        GroupInfoFragment.pendingGroupName = name;

        Log.d(SampleAppActivity.TAG, "Pending lamp group name: " + GroupInfoFragment.pendingGroupName);
    }

    @Override
    protected String getDuplicateNameMessage() {
        return this.getString(R.string.duplicate_name_message_group);
    }

    @Override
    protected boolean duplicateName(String groupName) {
        return Util.isDuplicateName(LightingDirector.get().getGroups(), groupName);
    }
}
