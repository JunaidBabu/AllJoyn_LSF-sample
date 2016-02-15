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

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public abstract class SceneItemInfoFragment extends PageFrameChildFragment implements View.OnClickListener {

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((SampleAppActivity) getActivity()).updateActionBar(R.string.title_basic_scene_info, false, true, false, true, true);
    }

    protected void addElementRow(SampleAppActivity activity, TableLayout elementTable, int iconID, String elementID, String memberNames, int detailsID) {
        addElementRow(activity, elementTable, iconID, elementID, memberNames, getString(detailsID));
    }

    protected void addElementRow(SampleAppActivity activity, TableLayout elementTable, int iconID, String elementID, String memberNames, String details) {
        TableRow tableRow = new TableRow(view.getContext());
        activity.getLayoutInflater().inflate(R.layout.view_scene_element_row, tableRow);

        ((ImageButton)tableRow.findViewById(R.id.detailedItemButtonIcon)).setImageResource(iconID);

        TextView textHeader = (TextView)tableRow.findViewById(R.id.detailedItemRowTextHeader);
        textHeader.setText(memberNames);
        textHeader.setTag(elementID);
        textHeader.setClickable(true);
        textHeader.setOnClickListener(this);

        TextView textDetails = (TextView)tableRow.findViewById(R.id.detailedItemRowTextDetails);
        textDetails.setText(details);
        textDetails.setTag(elementID);
        textDetails.setClickable(true);
        textDetails.setOnClickListener(this);

        ImageButton moreButton = (ImageButton)tableRow.findViewById(R.id.detailedItemButtonMore);
        moreButton.setImageResource(R.drawable.group_more_menu_icon);
        moreButton.setTag(elementID);
        moreButton.setOnClickListener(this);

        elementTable.addView(tableRow);
    }

    public abstract void updateInfoFields();
}
