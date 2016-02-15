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

import org.allseen.lsf.sdk.Group;
import org.allseen.lsf.sdk.LightingDirector;
import org.allseen.lsf.sdk.MyLampState;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GroupInfoFragment extends DimmableItemInfoFragment {
    public static String pendingGroupID;
    public static String pendingGroupName;
    public static List<String> pendingGroupContainedGroups;
    public static List<String> pendingGroupContainedLamps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        itemType = SampleAppActivity.Type.GROUP;

        ((TextView)statusView.findViewById(R.id.statusLabelName)).setText(R.string.label_group_name);

        // displays members of this group
        TextView membersLabel = (TextView)(view.findViewById(R.id.groupInfoMembers).findViewById(R.id.nameValueNameText));
        membersLabel.setText(R.string.group_info_label_members);
        membersLabel.setClickable(true);
        membersLabel.setOnClickListener(this);

        TextView membersValue = (TextView)(view.findViewById(R.id.groupInfoMembers).findViewById(R.id.nameValueValueText));
        membersValue.setClickable(true);
        membersValue.setOnClickListener(this);

        updateInfoFields(LightingDirector.get().getGroup(key));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((SampleAppActivity)getActivity()).updateActionBar(R.string.title_group_info, false, false, false, false, true);
    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();

        if (viewID == R.id.nameValueNameText || viewID == R.id.nameValueValueText) {
            if (parent != null) {
                Group group = LightingDirector.get().getGroup(key);

                if ((group != null) && (!group.isAllLampsGroup())) {
                    pendingGroupID = group.getId();
                    pendingGroupName = group.getName();
                    pendingGroupContainedGroups = new ArrayList<String>();
                    pendingGroupContainedLamps = new ArrayList<String>();

                    ((PageMainContainerFragment)parent).showSelectMembersChildFragment();
                }
            }
        } else {
            super.onClick(view);
        }
    }

    public void updateInfoFields(Group group) {
        if (group.getId().equals(key)) {
            stateAdapter.setCapability(group.getCapability());
            super.updateInfoFields(group);

            String details = Util.createMemberNamesString((SampleAppActivity)getActivity(), group, ", ", R.string.group_info_members_none);
            TextView membersValue = (TextView)(view.findViewById(R.id.groupInfoMembers).findViewById(R.id.nameValueValueText));

            if (details != null && !details.isEmpty()) {
                membersValue.setText(details);
            }
        }
    }

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_group_info;
    }

    @Override
    protected int getColorTempMin() {
        Group group = LightingDirector.get().getGroup(key);
        int colorTempMin = group != null ? group.getColorTempMin() : LightingDirector.COLORTEMP_MIN;

        return colorTempMin;
    }

    @Override
    protected int getColorTempSpan() {
        Group group = LightingDirector.get().getGroup(key);
        int colorTempMin = group != null ? group.getColorTempMin() : LightingDirector.COLORTEMP_MIN;
        int colorTempMax = group != null ? group.getColorTempMax() : LightingDirector.COLORTEMP_MAX;

        return colorTempMax - colorTempMin;
    }

    @Override
    protected int getColorTempDefault() {
        Group group = LightingDirector.get().getGroup(key);

        return group != null ? group.getColor().getColorTemperature() : LightingDirector.COLORTEMP_MIN;
    }

    @Override
    protected void onHeaderClick() {
        Group group = LightingDirector.get().getGroup(key);

        if (!group.isAllLampsGroup()) {
            SampleAppActivity activity = (SampleAppActivity)getActivity();

            activity.showItemNameDialog(R.string.title_group_rename, new UpdateGroupNameAdapter(group, activity));
        }
    }

    @Override
    protected MyLampState getItemLampState(String groupID){
        Group group = LightingDirector.get().getGroup(groupID);
        return group != null ? group.getState() : null;
    }
}
