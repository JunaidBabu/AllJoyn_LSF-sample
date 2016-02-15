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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class PageFrameParentFragment extends Fragment {
    public static final String CHILD_TAG_TABLE = "TABLE";
    public static final String CHILD_TAG_INFO = "INFO";
    public static final String CHILD_TAG_PRESETS = "PRESETS";
    public static final String CHILD_TAG_SETTINGS = "SETTINGS";
    public static final String CHILD_TAG_TEXT = "TEXT";

    public static String TAG;

    protected PageFrameChildFragment child = null;

    public abstract PageFrameChildFragment createTableChildFragment();
    public abstract PageFrameChildFragment createInfoChildFragment();
    public abstract PageFrameChildFragment createPresetsChildFragment();

    public PageFrameChildFragment createSettingsChildFragment() {
        return new SettingsFragment();
    }

    public PageFrameChildFragment createTextChildFragment() {
        return new TextFragment();
    }

    public int onBackPressed() {
        FragmentManager manager = getChildFragmentManager();
        int backStackCount = manager.getBackStackEntryCount();

        if (backStackCount > 1) {
            manager.popBackStack();
            backStackCount--;
        }

        if (backStackCount > 0) {
            String tag = manager.getBackStackEntryAt(backStackCount - 1).getName();
            child = (PageFrameChildFragment)manager.findFragmentByTag(tag);
        }

        return backStackCount;
    }

    public void clearBackStack() {
        popBackStack(CHILD_TAG_TABLE);

        ((SampleAppActivity)getActivity()).onClearBackStack();
    }

    public void popBackStack(String tag) {
        FragmentManager manager = getChildFragmentManager();

        manager.popBackStack(tag, 0);

        child = (PageFrameChildFragment)manager.findFragmentByTag(tag);
    }

    public void showTableChildFragment() {
        showChildFragment(CHILD_TAG_TABLE, null);
    }

    public void showInfoChildFragment(String key) {
        showChildFragment(CHILD_TAG_INFO, key);
    }

    public void showPresetsChildFragment(String key1, String key2) {
        showChildFragment(CHILD_TAG_PRESETS, key1, key2);
    }

    public void showSettingsChildFragment(String key) {
        showChildFragment(CHILD_TAG_SETTINGS, key);
    }

    public void showTextChildFragment(String key) {
        showChildFragment(CHILD_TAG_TEXT, key);
    }

    protected void showChildFragment(String tag, String key) {
        showChildFragment(tag, key, null);
    }

    protected void showChildFragment(String tag, String key1, String key2) {
        FragmentManager manager = getChildFragmentManager();

        child = (PageFrameChildFragment)manager.findFragmentByTag(tag);

        if (child == null) {
            child = createChildFragment(tag);
        }

        child.setParentFragment(this);
        child.setKeys(key1, key2);

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.zoom_in, R.anim.fade_out, R.anim.fade_in, R.anim.zoom_out);
        transaction.addToBackStack(tag);
        transaction.replace(R.id.pageFrame, child, tag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
    }

    protected PageFrameChildFragment createChildFragment(String tag)
    {
        PageFrameChildFragment child;

        if (tag == CHILD_TAG_TABLE) {
            child = createTableChildFragment();
        } else if (tag == CHILD_TAG_INFO) {
            child = createInfoChildFragment();
        } else if (tag == CHILD_TAG_PRESETS) {
            child = createPresetsChildFragment();
        } else if (tag == CHILD_TAG_SETTINGS) {
            child = createSettingsChildFragment();
        } else if (tag == CHILD_TAG_TEXT){
            child = createTextChildFragment();
        } else {
            Log.e(SampleAppActivity.TAG, "Invalid child fragment tag: " + tag);
            child = null;
        }

        return child;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_frame, container, false);

        showTableChildFragment();

        PageFrameParentFragment.TAG = getTag();

        return view;
    }

    public void onActionAdd() {
        if (child != null) {
            child.onActionAdd();
        }
    }

    public void onActionNext() {
        if (child != null) {
            child.onActionNext();
        }
    }

    public void onActionDone() {
        if (child != null) {
            child.onActionDone();
        }
    }
}
