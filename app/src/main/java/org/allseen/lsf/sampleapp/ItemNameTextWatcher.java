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

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public abstract class ItemNameTextWatcher implements TextWatcher {

    protected EditText nameText;

    public ItemNameTextWatcher(EditText nameText) {
        super();

        this.nameText = nameText;
    }

    @Override
    public void afterTextChanged(Editable s) {
        doValidation();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Currently nothing to do
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Currently nothing to do
    }

    public void doValidation() {
        Editable newName = nameText.getText();

        onTextValidation(newName != null && newName.length() > 0 && newName.charAt(0) != ' ');
    }

    protected abstract void onTextValidation(boolean isValid);
}
