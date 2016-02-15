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

import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;

public class ItemNameDialogTextWatcher extends ItemNameTextWatcher {

    protected AlertDialog alertDialog;

    public ItemNameDialogTextWatcher(AlertDialog alertDialog, EditText nameText) {
        super(nameText);

        this.alertDialog = alertDialog;
    }

    @Override
    protected void onTextValidation(boolean isValid) {
        Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        if (button != null) {
            button.setEnabled(isValid);
        }
    }
}
