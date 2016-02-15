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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.widget.EditText;

public abstract class UpdateItemNameAdapter implements ItemNameAdapter {
    protected LightingItem item;
    protected SampleAppActivity activity;

    public UpdateItemNameAdapter(LightingItem item, SampleAppActivity activity) {
        this.item = item;
        this.activity = activity;
    }

    @Override
    public String getCurrentName() {
        return item != null ? item.getName() : "";
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        EditText nameText = (EditText)(((AlertDialog)dialog).findViewById(R.id.itemNameText));
        final String itemName = nameText.getText().toString();

        Log.d(SampleAppActivity.TAG, "Item ID: " + item.getId() + " New name: " + itemName);

        if (itemName != null && !itemName.isEmpty()) {

            // check for duplicate names
            if (duplicateName(itemName)) {
                // create an alert

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.duplicate_name);
                builder.setMessage(String.format(getDuplicateNameMessage(), itemName));
                builder.setPositiveButton(R.string.dialog_ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doUpdateName(itemName);
                    }
                });
                builder.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.create().show();

            } else {
                // we can go ahead and use this name
                doUpdateName(itemName);
            }
        }
    }

    protected void doUpdateName(String itemName) {
        item.rename(itemName);
    }

    protected abstract String getDuplicateNameMessage();
    protected abstract boolean duplicateName(String itemName);
}
