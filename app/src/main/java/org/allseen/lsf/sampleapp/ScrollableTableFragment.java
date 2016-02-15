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
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ScrollableTableFragment extends PageFrameChildFragment {
    protected TableLayout table = null;
    protected LinearLayout layout = null;
    protected SampleAppActivity.Type type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_scrollable_table, container, false);

        table = (TableLayout) view.findViewById(R.id.scrollableTable);
        layout = (LinearLayout) view.findViewById(R.id.scrollLayout);
        updateLoading();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void updateLoading() {
        SampleAppActivity activity = (SampleAppActivity)getActivity();

        if (!activity.isControllerConnected()) {
            // display loading controller service screen, hide the scroll table
            layout.findViewById(R.id.scrollLoadingView).setVisibility(View.VISIBLE);
            layout.findViewById(R.id.scrollScrollView).setVisibility(View.GONE);

            View loadingView = layout.findViewById(R.id.scrollLoadingView);

            String ssid = ((WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID();

            if (ssid == null) {
                ssid = "<unknown ssid>";
            }
            ((TextView) loadingView.findViewById(R.id.loadingText1)).setText(activity.getText(R.string.no_controller) + " " + ssid);
            ((TextView) loadingView.findViewById(R.id.loadingText2)).setText(activity.getText(R.string.loading_controller));

        } else {
            // remove the loading view, and resume showing the scroll table
            layout.findViewById(R.id.scrollLoadingView).setVisibility(View.GONE);
            layout.findViewById(R.id.scrollScrollView).setVisibility(View.VISIBLE);
        }
    }

    public void removeElement(String id) {
        final SampleAppActivity activity = (SampleAppActivity)getActivity();
        final TableRow row = (TableRow) table.findViewWithTag(id);
        if (row != null) {
            //TODO-CHK: why run on UI thread here? Everything should already be on the UI thread...
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    table.removeView(row);
                    table.postInvalidate();
                }
            });
        }

        activity.setTabTitles();
    }
}
