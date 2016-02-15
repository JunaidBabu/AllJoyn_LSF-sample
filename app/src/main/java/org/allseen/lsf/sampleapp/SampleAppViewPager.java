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

import android.app.ActionBar;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SampleAppViewPager extends ViewPager {

    SampleAppActivity activity;

    public SampleAppViewPager(Context context) {
        super(context);
    }

    public SampleAppViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setActivity(SampleAppActivity activity) {
        this.activity = activity;

        final ActionBar actionBar = activity.getActionBar();
        final PageFrameParentAdapter pageAdapter = new PageFrameParentAdapter(activity, activity.getSupportFragmentManager());

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(false);

        setAdapter(pageAdapter);
        setOffscreenPageLimit(3);

        setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int tabIndex) {
                actionBar.setSelectedNavigationItem(tabIndex);
            }
        });

        for (int tabIndex = 0; tabIndex < pageAdapter.getCount(); tabIndex++) {
            actionBar.addTab(actionBar.newTab().setText(activity.getPageTitle(tabIndex)).setTabListener(activity));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

    	if(activity.getToast().getView().isShown()){
			System.out.println("cancel the toast");
			activity.getToast().cancel();
		}

    	if ((activity != null) && (activity.isSwipeable())) {
    		return super.onInterceptTouchEvent(event);
    	} else {
    		return false;
    	}
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((activity != null) && (activity.isSwipeable())) {
            return super.onTouchEvent(event);
        } else {
            return false;
        }
    }
}
