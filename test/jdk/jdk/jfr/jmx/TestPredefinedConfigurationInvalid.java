/*
 * Copyright (c) 2013, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.jfr.jmx;

import jdk.jfr.Configuration;
import jdk.management.jfr.FlightRecorderMXBean;
import jdk.test.lib.Asserts;
import jdk.test.lib.jfr.CommonHelper;
import jdk.test.lib.jfr.VoidFunction;

/**
 * @test
 * @requires vm.flagless
 * @requires vm.hasJFR
 * @library /test/lib /test/jdk
 * @run main/othervm jdk.jfr.jmx.TestPredefinedConfigurationInvalid
 */
public class TestPredefinedConfigurationInvalid {
    public static void main(String[] args) throws Throwable {
        FlightRecorderMXBean bean = JmxHelper.getFlighteRecorderMXBean();

        long recId = bean.newRecording();

        // Test invalid named configs.
        verifyNullPointer(()->{ bean.setPredefinedConfiguration(recId, null); }, "setNamedConfig(null)");
        setInvalidConfigName(recId, "");
        setInvalidConfigName(recId, "invalidname");

        // Verify we can set named config after failed attempts.
        Configuration config = Configuration.getConfigurations().getFirst();
        bean.setPredefinedConfiguration(recId, config.getName());
        JmxHelper.verifyMapEquals(bean.getRecordingSettings(recId), config.getSettings());
    }

    private static void setInvalidConfigName(long recId, String name) {
        try {
            JmxHelper.getFlighteRecorderMXBean().setPredefinedConfiguration(recId, name);
            Asserts.fail("Missing Exception when setNamedConfig('" + name + "')");
        } catch (IllegalArgumentException e) {
            // Expected exception.
            String msg = e.getMessage().toLowerCase();
            System.out.println("Got expected exception: " + msg);
            String expectMsg = "not find configuration";
            Asserts.assertTrue(msg.contains(expectMsg), String.format("No '%s' in '%s'", expectMsg, msg));
        }
    }

    private static void verifyNullPointer(VoidFunction f, String msg) throws Throwable {
        CommonHelper.verifyException(f, msg, NullPointerException.class);
    }

}
