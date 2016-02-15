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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.allseen.lsf.sdk.LightingItemInterface;

public class MemberNamesString {

    public static String format(SampleAppActivity activity, LightingItemInterface[] items, MemberNamesOptions options, int maxCount, String noMembers) {
        List<String> itemNames = new ArrayList<String>();

        for (LightingItemInterface item : items) {
            itemNames.add(item.getName());
        }

        return format(activity, itemNames, options, maxCount, noMembers);
    }

    public static String format(SampleAppActivity activity, List<String> names, MemberNamesOptions options, int maxCount, String noMembers) {
        return format(activity, names, new ArrayList<String>(), options, maxCount, noMembers);
    }

    public static String format(SampleAppActivity activity, List<String> primaryNames, List<String> secondaryNames, MemberNamesOptions options, int maxCount, String noMembers) {
        Collections.sort(primaryNames);
        Collections.sort(secondaryNames);

        String details = noMembers;
        int totalCount = primaryNames.size() + secondaryNames.size();

        if (totalCount > 0) {
            StringBuilder sb = new StringBuilder();
            int nextIndex = 0;

            nextIndex = format(sb, primaryNames.iterator(), nextIndex, totalCount, maxCount, options);
            nextIndex = format(sb, secondaryNames.iterator(), nextIndex, totalCount, maxCount, options);

            if (totalCount > maxCount) {
                sb.append(options.finalSeparator);
                sb.append(String.format(options.andOthersFormat, totalCount - maxCount));
            }

            details = sb.toString();
        }

        return details;
    }

    protected static int format(StringBuilder sb, Iterator<String> it, int nextIndex, int totalCount, int maxCount, MemberNamesOptions options) {
        boolean others = totalCount > maxCount;

        while (nextIndex < maxCount && it.hasNext()) {
            nextIndex++;

            if (nextIndex > 1) {
                if (nextIndex == maxCount && !others) {
                    sb.append(options.finalSeparator);
                } else if (nextIndex == totalCount) {
                    sb.append(options.finalSeparator);
                } else {
                    sb.append(options.innerSeparator);
                }
            }

            sb.append(options.openQuote);
            sb.append(it.next());
            sb.append(options.closeQuote);
        }

        return nextIndex;
    }
}
