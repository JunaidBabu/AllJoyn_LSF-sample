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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PageFrameParentAdapter extends FragmentPagerAdapter {

    private final Context context;

    public PageFrameParentAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);

        this.context = context;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int index) {
        PageFrameParentFragment parentFragment;

        if (index == 0) {
            parentFragment = new LampsPageFragment();
        } else if (index == 1) {
            parentFragment = new GroupsPageFragment();
        } else if (index == 2) {
            parentFragment = new ScenesPageFragment();
        } else {
            parentFragment = null;
        }

        return parentFragment;
    }

    @Override
    public CharSequence getPageTitle(int index) {
        return ((SampleAppActivity) context).getPageTitle(index);
    }
}
