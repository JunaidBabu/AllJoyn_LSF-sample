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
import java.util.Collection;
import java.util.List;

import org.allseen.lsf.sdk.Group;
import org.allseen.lsf.sdk.GroupMember;
import org.allseen.lsf.sdk.LightingDirector;

import android.view.Menu;
import android.view.MenuInflater;

public class GroupSelectMembersFragment extends SelectMembersFragment {

    public GroupSelectMembersFragment() {
        super(R.string.label_group);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        SampleAppActivity activity = (SampleAppActivity)getActivity();

        activity.updateActionBar(isAddMode() ? R.string.title_group_add : R.string.title_group_edit, false, false, false, true, true);
    }

    @Override
    protected boolean isParentOfPendingGroup(Group group) {
        return !isAddMode() && group.hasGroupID(GroupInfoFragment.pendingGroupID);
    }

    @Override
    protected String getHeaderText() {
        return getString(R.string.group_select_members);
    }

    @Override
    protected String getPendingItemID() {
        return GroupInfoFragment.pendingGroupID;
    }

    @Override
    protected Collection<String> getPendingGroupIDs() {
        return GroupInfoFragment.pendingGroupContainedGroups;
    }

    @Override
    protected Collection<String> getPendingLampIDs() {
        return GroupInfoFragment.pendingGroupContainedLamps;
    }

    @Override
    protected void processSelection(SampleAppActivity activity, List<String> lampIDs, List<String> groupIDs, List<String> presetIDs, List<String> transitionEffectIDs, List<String> pulseEffectIDs, List<String> sceneElementIDs, List<String> sceneIDs) {
        List<GroupMember> members = new ArrayList<GroupMember>();

        members.addAll(Arrays.asList(LightingDirector.get().getLamps(lampIDs)));
        members.addAll(Arrays.asList(LightingDirector.get().getGroups(groupIDs)));

        if (!isAddMode()) {
            Group group = LightingDirector.get().getGroup(GroupInfoFragment.pendingGroupID);

            if (group != null) {
                group.modify(members.toArray(new GroupMember[members.size()]));
            }
        } else {
            LightingDirector.get().createGroup(members.toArray(new GroupMember[members.size()]), GroupInfoFragment.pendingGroupName);
        }
    }

    @Override
    protected int getMixedSelectionMessageID() {
        return R.string.mixing_lamp_types_message_group;
    }

    @Override
    protected int getMixedSelectionPositiveButtonID() {
        return R.string.create_group;
    }

    //TODO-REF Common
    protected boolean isAddMode() {
        return GroupInfoFragment.pendingGroupID == null || GroupInfoFragment.pendingGroupID.isEmpty();
    }
}
