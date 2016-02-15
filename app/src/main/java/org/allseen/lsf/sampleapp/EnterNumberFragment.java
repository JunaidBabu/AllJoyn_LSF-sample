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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public abstract class EnterNumberFragment extends PageFrameChildFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_enter_number, container, false);

        setTextViewValue(view, R.id.enterNumberLabel, getString(getLabelID()), 0);

        ((EditText)view.findViewById(R.id.enterNumberText)).setInputType(getInputType());

        updateNumberField();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((SampleAppActivity)getActivity()).updateActionBar(getTitleID(), false, false, false, true, true);
    }

    @Override
    public void onActionDone() {
        final String stringValue = ((EditText)view.findViewById(R.id.enterNumberText)).getText().toString();

        if ((stringValue != null) && (!stringValue.isEmpty())) {
            try {
                long longValue = Math.round(Double.valueOf(stringValue) * getScale());
                long maxValue = (long)Integer.MAX_VALUE - Integer.MIN_VALUE;

                if (longValue >= 0 && longValue <= maxValue) {
                    setNumberValue(longValue);
                } else {
                	((SampleAppActivity)getActivity()).showToast(String.format(getString(R.string.toast_number_value_invalid), maxValue / getScale()));
                }
            } catch (NumberFormatException e) {
                ((SampleAppActivity)getActivity()).showToast(R.string.toast_number_format_invalid);
            }
        } else {
        	((SampleAppActivity)getActivity()).showToast(String.format(getString(R.string.toast_number_missing), getString(getLabelID())));
        }
    }

    protected double getScale() {
        return 1000.0;
    }

    public void updateNumberField() {
        String number = getNumberString();

        ((EditText)view.findViewById(R.id.enterNumberText)).setText(number);
    }

    protected abstract int getTitleID();
    protected abstract int getLabelID();
    protected abstract int getInputType();

    protected abstract String getNumberString();
    protected abstract boolean setNumberValue(long longValue);
}

