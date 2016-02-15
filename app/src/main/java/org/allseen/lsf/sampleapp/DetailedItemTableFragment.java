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

import org.allseen.lsf.sdk.LightingItem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;

public abstract class DetailedItemTableFragment extends ScrollableTableFragment implements View.OnClickListener {
    public void addItem(LightingItem item, String details, int iconID) {
        if (item != null) {
            insertDetailedItemRow(getActivity(), item.getId(), item.getTag(), item.getName(), details, iconID);
        }
    }

    protected <T> void insertDetailedItemRow(Context context, String itemID, Comparable<T> tag, String name, String details, int iconID) {
        insertDetailedItemRow(
            context,
            (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE),
            itemID,
            tag,
            name,
            details,
            iconID);
    }

    protected <T> void insertDetailedItemRow(Context context, LayoutInflater inflater, String itemID, Comparable<T> tag, String name, String details, int iconID) {
        Log.d(SampleAppActivity.TAG, "insertDetailedItemRow(): " + itemID + ", " + ", " + name + ", " + iconID);

        TableRow tableRow = (TableRow)table.findViewWithTag(itemID);

        if (tableRow == null) {
            tableRow = new TableRow(context);

            inflater.inflate(R.layout.view_detailed_item_row, tableRow);

            setImageButtonBackgroundResource(tableRow, R.id.detailedItemButtonIcon, iconID);

            TextView textHeader = (TextView)tableRow.findViewById(R.id.detailedItemRowTextHeader);
            textHeader.setText(name);
            textHeader.setTag(itemID);
            textHeader.setOnClickListener(this);

            TextView textDetails = (TextView)tableRow.findViewById(R.id.detailedItemRowTextDetails);
            textDetails.setText(details);
            textDetails.setTag(itemID);
            textDetails.setOnClickListener(this);

            ImageButton infoButton = (ImageButton)tableRow.findViewById(R.id.detailedItemButtonMore);
            infoButton.setImageResource(R.drawable.nav_more_menu_icon);
            infoButton.setTag(itemID);
            infoButton.setOnClickListener(this);

            tableRow.setTag(itemID);

            TableSorter.insertSortedTableRow(table, tableRow, tag);
        } else {
            Log.d(SampleAppActivity.TAG, "setText(): " + itemID + ", " + ", " + name + ", " + iconID);
            ((TextView)tableRow.findViewById(R.id.detailedItemRowTextHeader)).setText(name);
            ((TextView)tableRow.findViewById(R.id.detailedItemRowTextDetails)).setText(details);

            TableSorter.updateSortedTableRow(table, tableRow, tag);
        }

        ((SampleAppActivity)getActivity()).setTabTitles();
    }

    @Override
    public void onClick(View clickedView) {
        int clickedID = clickedView.getId();

        Log.d(SampleAppActivity.TAG, "onClick(): " + clickedID + ", " + parent);

        if (parent != null) {
            if (clickedID == R.id.detailedItemButtonMore) {
                ((SampleAppActivity)getActivity()).onItemButtonMore(parent, type, clickedView, clickedView.getTag().toString(), null, true);
            } else if (clickedID == R.id.detailedItemRowTextHeader || clickedID == R.id.detailedItemRowTextDetails) {
                onClickRowText(clickedView.getTag().toString());
            }
        }
    }

    protected void onClickRowText(String sceneItemID) {
        SampleAppActivity activity = (SampleAppActivity)getActivity();

        if (!activity.applySceneElement(sceneItemID)) {
            if (!activity.applyBasicScene(sceneItemID)) {
                if (!activity.applyMasterScene(sceneItemID)) {
                    Log.e(SampleAppActivity.TAG, "Apply failed: Invalid ID " + sceneItemID);
                }
            }
        }
    }
}
