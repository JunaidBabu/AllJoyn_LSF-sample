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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PageFrameChildFragment extends Fragment {
    protected PageFrameParentFragment parent;
    protected String key;
    protected String key2;
    protected View view;

    public void setParentFragment(PageFrameParentFragment parent) {
        this.parent = parent;
    }

    public void setKeys(String key1, String key2) {
        this.key = key1 != null ? key1 : "";
        this.key2 = key2 != null ? key2 : "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        // hide soft keyboard
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Note that this sets the background image. The foreground image
    // may also have been set, either in XML or in code, which could
    // make this call appear to have no effect.
    protected void setImageButtonBackgroundResource(View parent, int viewID, int imageID) {
        ImageButton imageButton = (ImageButton)parent.findViewById(viewID);

        if (imageButton != null) {
            imageButton.setBackgroundResource(imageID);
        } else {
            Log.e(SampleAppActivity.TAG, "Missing image button: " + viewID);
        }
    }

    protected void setTextViewValue(View parent, int viewID, boolean value, int unitsID) {
        setTextViewValue(parent, viewID, getResources().getString(value ? R.string.value_yes : R.string.value_no), unitsID);
    }

    protected void setTextViewValue(View parent, int viewID, Enum value, int unitsID) {
        setTextViewValue(parent, viewID, value != null ? value.name() : null, unitsID);
    }

    protected void setTextViewValue(View parent, int viewID, long value, int unitsID) {
        setTextViewValue(parent, viewID, String.valueOf(value), unitsID);
    }

    protected void setTextViewValue(View parent, int viewID, String value, int unitsID) {
        TextView textView = (TextView)parent.findViewById(viewID);

        if (textView != null) {
            String text;

            if (value != null) {
                if (unitsID > 0) {
                    text = value + getResources().getString(unitsID);
                } else {
                    text = value;
                }
            } else {
                text = "";
            }

            textView.setText(text);
        }
    }

    protected void setSeekBarValue(View parent, int id, int value) {
        SeekBar seekBar = (SeekBar)parent.findViewById(id);

        if (seekBar != null) {
            Log.d(SampleAppActivity.TAG, "Set seek bar to " + value);
            seekBar.setProgress(value);
        }
    }

    protected void setCompoundButtonChecked(View parent, int id, boolean checked) {
        CompoundButton button = (CompoundButton)parent.findViewById(id);

        if (button != null) {
            button.setChecked(checked);
        }
    }

    public void onActionAdd() {
        // Nothing to do by default -- subclasses may override
    }

    public void onActionNext() {
        // Nothing to do by default -- subclasses may override
    }

    public void onActionDone() {
        // Nothing to do by default -- subclasses may override
    }
}
