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

import org.allseen.lsf.sdk.SceneElement;
import org.allseen.lsf.sdk.LightingDirector;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class SceneElementV2InfoFragment extends SceneItemInfoFragment {
    public static PendingSceneElementV2 pendingSceneElement = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_scene_element_info, container, false);

        // Icon
        View statusView = view.findViewById(R.id.sceneElementInfoStatus);

        setImageButtonBackgroundResource(statusView, R.id.statusButtonPower, R.drawable.scene_element_set_icon);

        // Name
        TextView nameLabel = (TextView)statusView.findViewById(R.id.statusLabelName);
        nameLabel.setText(R.string.scene_element_info_name);
        nameLabel.setClickable(true);
        nameLabel.setOnClickListener(this);

        TextView nameText = (TextView)statusView.findViewById(R.id.statusTextName);
        nameText.setClickable(true);
        nameText.setOnClickListener(this);

        // Members
        View membersView = view.findViewById(R.id.sceneElementInfoMembers);
        membersView.setClickable(true);
        membersView.setOnClickListener(this);

        setTextViewValue(membersView, R.id.nameValueNameText, getString(R.string.scene_element_info_members), 0);

        // Effect
        View effectView = view.findViewById(R.id.sceneElementInfoEffect);

        TextView textHeader = (TextView)effectView.findViewById(R.id.detailedItemRowTextHeader);
        textHeader.setClickable(true);
        textHeader.setOnClickListener(this);

        TextView textDetails = (TextView)effectView.findViewById(R.id.detailedItemRowTextDetails);
        textDetails.setClickable(true);
        textDetails.setOnClickListener(this);

        ImageButton moreButton = (ImageButton)effectView.findViewById(R.id.detailedItemButtonMore);
        moreButton.setImageResource(R.drawable.group_more_menu_icon);
        moreButton.setOnClickListener(this);

        // Update values
        updateInfoFields();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((SampleAppActivity)getActivity()).updateActionBar(R.string.title_scene_element_info, false, false, false, false, true);
    }

    @Override
    public void onClick(View view) {
        int viewID = view.getId();

        if (viewID == R.id.statusLabelName || viewID == R.id.statusTextName) {
            onHeaderClick();
        } else if (viewID == R.id.detailedItemRowTextHeader || viewID == R.id.detailedItemRowTextDetails) {
            onEffectClick();
        } else if (viewID == R.id.sceneElementInfoMembers) {
            onMembersClick();
        }
    }

    protected void onHeaderClick() {
        SampleAppActivity activity = (SampleAppActivity)getActivity();
        SceneElement sceneElement = LightingDirector.get().getSceneElement(key);

        activity.showItemNameDialog(R.string.title_scene_element_rename, new UpdateSceneElementNameAdapter(sceneElement, activity));
    }

    protected void onEffectClick() {
        //TODO-FIX we need to actually recompute the pendingSceneElement.hasDetails
        // value based on the currrent lamps and groups members here
        pendingSceneElement.hasEffects = true;

        ((ScenesPageFragment)parent).showSelectEffectChildFragment();
    }

    protected void onMembersClick() {
        ((ScenesPageFragment)parent).showSelectMembersChildFragment();
    }

    @Override
    public void updateInfoFields() {
        updateSceneElementInfoFields((SampleAppActivity)getActivity(), pendingSceneElement);
    }

    protected void updateSceneElementInfoFields(SampleAppActivity activity, PendingSceneElementV2 pendingSceneElement) {
        // Update name and members
        setTextViewValue(view.findViewById(R.id.sceneElementInfoStatus), R.id.statusTextName, pendingSceneElement.name, 0);
        setTextViewValue(view.findViewById(R.id.sceneElementInfoMembers), R.id.nameValueValueText, Util.createMemberNamesString(activity, pendingSceneElement, ", ", R.string.scene_element_members_none), 0);

        View effectView = view.findViewById(R.id.sceneElementInfoEffect);

        if (pendingSceneElement.pendingPresetEffect != null) {
            updateEffectFields(activity, effectView, pendingSceneElement.pendingPresetEffect.id, pendingSceneElement.pendingPresetEffect.name, R.drawable.list_constant_icon, R.string.effect_name_preset);
        } else if (pendingSceneElement.pendingTransitionEffect != null) {
            updateEffectFields(activity, effectView, pendingSceneElement.pendingTransitionEffect.id, pendingSceneElement.pendingTransitionEffect.name, R.drawable.list_transition_icon, R.string.effect_name_transition);
        } else if (pendingSceneElement.pendingPulseEffect != null) {
            updateEffectFields(activity, effectView, pendingSceneElement.pendingPulseEffect.id, pendingSceneElement.pendingPulseEffect.name, R.drawable.list_pulse_icon, R.string.effect_name_pulse);
        }
    }

    protected void updateEffectFields(SampleAppActivity activity, View effectView, String effectID, String effectName, int iconID, int textID) {
        ((ImageButton)effectView.findViewById(R.id.detailedItemButtonIcon)).setImageResource(iconID);

        TextView textHeader = (TextView)effectView.findViewById(R.id.detailedItemRowTextHeader);
        textHeader.setText(getString(textID));
        textHeader.setTag(effectID);

        TextView textDetails = (TextView)effectView.findViewById(R.id.detailedItemRowTextDetails);
        textDetails.setText(effectName);
        textDetails.setTag(effectID);

        ImageButton moreButton = (ImageButton)effectView.findViewById(R.id.detailedItemButtonMore);
        moreButton.setTag(effectID);
    }
}
