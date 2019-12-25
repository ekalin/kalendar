package com.github.ekalin.kalendar.testutil;

import org.joda.time.DateTime;

import static com.google.common.truth.Truth.assertWithMessage;

public class DateAssert {
    public static void assertDatesWithTolerance(DateTime actual, DateTime expected) {
        // Since a few ms have elapsed since setNow() and setting of startOfTimeRange/endOfTimeRange, we need a
        // little fuzziness
        int toleranceMs = 500;
        assertWithMessage("%s is not equal to %s (with tolerance %sms)", actual, expected, toleranceMs)
                .that(actual.getMillis() - expected.getMillis()).isAtMost(toleranceMs);
    }
}
