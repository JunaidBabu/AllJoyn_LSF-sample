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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public abstract class EnterNameFragment extends PageFrameChildFragment {
    protected int labelStringID;

    protected ItemNameFragmentTextWatcher nameWatcher;

    public EnterNameFragment(int labelStringID) {
        this.labelStringID = labelStringID;
    }

    protected abstract int getTitleID();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_enter_name, container, false);

        EditText nameText = (EditText)view.findViewById(R.id.enterNameText);
        nameWatcher = new ItemNameFragmentTextWatcher((SampleAppActivity)getActivity(), nameText);
        nameText.addTextChangedListener(nameWatcher);

        updateEnterNameFields();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((SampleAppActivity)getActivity()).updateActionBar(getTitleID(), false, false, true, false, true);

        if (nameWatcher != null) {
            nameWatcher.doValidation();
        }
    }

    public void updateEnterNameFields() {
        String text = String.format(getResources().getString(R.string.label_enter_name), getResources().getString(labelStringID));

        setTextViewValue(view, R.id.enterNameLabel, text, 0);

        nameWatcher.doValidation();
    }

    @Override
    public void onActionNext() {
        final String name = ((EditText)view.findViewById(R.id.enterNameText)).getText().toString().trim();

        if ((name != null) && (!name.isEmpty())) {
            if (parent != null) {
                // check for duplicate names
                if (duplicateName(name)) {
                    // create an alert
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.duplicate_name);
                    builder.setMessage(String.format(getDuplicateNameMessage(), name));
                    builder.setPositiveButton(R.string.dialog_ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setName(name);
                            showNextFragment();
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
                    setName(name);
                    showNextFragment();
                }
            }
        } else {
            String text = String.format(getResources().getString(R.string.toast_name_missing), getResources().getString(labelStringID));
            ((SampleAppActivity)getActivity()).showToast(text);
        }
    }

    protected void showNextFragment() {
        ((PageMainContainerFragment)parent).showSelectMembersChildFragment();
    }

    protected abstract void setName(String name);
    protected abstract String getDuplicateNameMessage();
    protected abstract boolean duplicateName(String name);
}

