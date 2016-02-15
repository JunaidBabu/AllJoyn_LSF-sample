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

import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;

public class TableSorter {
    public static <T> void insertSortedTableRow(TableLayout table, TableRow tableRow, Comparable<T> tag) {
        int searchLow = 0;
        int searchHigh = table.getChildCount() - 1;

        while (searchLow <= searchHigh) {
            int searchNext = searchLow + ((searchHigh - searchLow) / 2);
            int comparison = compareTags(tag, (TableRow)table.getChildAt(searchNext));

            Log.v(SampleAppActivity.TAG, "insertSortedTableRow(): checking [" + searchLow + ", " + searchNext + ", " + searchHigh + "]");

            if (comparison < 0) {
                searchHigh = searchNext - 1;
            } else if (comparison > 0) {
                searchLow = searchNext + 1;
            } else {
                searchLow = searchHigh + 1;
            }
        }

        setTag(tableRow, tag);

        table.addView(tableRow, searchLow);
    }

    public static <T> void updateSortedTableRow(TableLayout table, TableRow tableRow, Comparable<T> tag) {
        if (!equalTags(tag, tableRow)) {
            table.removeView(tableRow);

            TableSorter.insertSortedTableRow(table, tableRow, tag);
        }
    }

    protected static <T> boolean equalTags(Comparable<T> tag, TableRow tableRow) {
        return compareTags(tag, tableRow) == 0;
    }

    @SuppressWarnings("unchecked")
    protected static <T> int compareTags(Comparable<T> tag, TableRow tableRow) {
        return tag.compareTo((T)tableRow.getTag(R.id.TAG_KEY_SORTABLE_NAME));
    }

    protected static <T> void setTag(TableRow tableRow, T tag) {
        tableRow.setTag(R.id.TAG_KEY_SORTABLE_NAME, tag);
    }
}
