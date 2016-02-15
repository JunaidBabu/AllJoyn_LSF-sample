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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

    public static final long SPLASH_TIME_OUT = 2500;

    private Handler closeHandler;
    private Runnable closeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // TODO offload AllJoyn initialization functionality from MainActivity to start here

        closeHandler = new Handler();
        closeRunnable = new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, SampleAppActivity.class);
                startActivity(i);

                finish();
            }
        };
    }

    @Override
    protected void onPause() {
        closeHandler.removeCallbacks(closeRunnable);
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        closeHandler.postDelayed(closeRunnable, SPLASH_TIME_OUT);
        super.onResume();
    }
}
