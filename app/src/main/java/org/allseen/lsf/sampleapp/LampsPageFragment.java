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
import android.view.View;
import android.view.ViewGroup;

public class LampsPageFragment extends PageFrameParentFragment {
    public static final String CHILD_TAG_DETAILS = "DETAILS";

    public static String TAG;

    public void showDetailsChildFragment(String key) {
        showChildFragment(CHILD_TAG_DETAILS, key);
    }

    @Override
    protected PageFrameChildFragment createChildFragment(String tag)
    {
        return tag == CHILD_TAG_DETAILS ? createDetailsChildFragment() : super.createChildFragment(tag);
    }

    @Override
    public PageFrameChildFragment createTableChildFragment() {
        return new LampsTableFragment();
    }

    @Override
    public PageFrameChildFragment createInfoChildFragment() {
        return new LampInfoFragment();
    }

    @Override
    public PageFrameChildFragment createPresetsChildFragment() {
        return new LampPresetsFragment();
    }

    public PageFrameChildFragment createDetailsChildFragment() {
        return new LampDetailsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        LampsPageFragment.TAG = getTag();

        return root;
    }
}
