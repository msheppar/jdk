/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
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

package compiler.runtime.safepoints;

import compiler.lib.ir_framework.*;
import java.lang.ref.SoftReference;

/**
 * @test
 * @summary Test that undefined values generated by MachTemp nodes (in this
 *          case, derived from G1 barriers) are not included in OopMaps.
 *          Extracted from java.lang.invoke.LambdaFormEditor::getInCache.
 * @key randomness
 * @library /test/lib /
 * @requires vm.gc.G1 & vm.bits == 64 & vm.opt.final.UseCompressedOops == true
 * @run driver compiler.runtime.safepoints.TestMachTempsAcrossSafepoints
 */

public class TestMachTempsAcrossSafepoints {

    static class RefWithKey extends SoftReference<Object> {
        final int key;

        public RefWithKey(int key) {
            super(new Object());
            this.key = key;
        }

        @DontInline
        @Override
        public boolean equals(Object obj) {
            return obj instanceof RefWithKey that && this.key == that.key;
        }
    }

    public static void main(String[] args) throws Exception {
        String inlineCmd = "-XX:CompileCommand=inline,java.lang.ref.SoftReference::get";
        TestFramework.runWithFlags(inlineCmd, "-XX:+StressGCM", "-XX:+StressLCM", "-XX:StressSeed=1");
        TestFramework.runWithFlags(inlineCmd, "-XX:+StressGCM", "-XX:+StressLCM");
    }

    @Test
    @IR(counts = {IRNode.G1_LOAD_N, "1"}, phase = CompilePhase.FINAL_CODE)
    @IR(counts = {IRNode.MACH_TEMP, ">= 1"}, phase = CompilePhase.FINAL_CODE)
    @IR(counts = {IRNode.STATIC_CALL_OF_METHOD, "equals", "2"})
    @IR(failOn = {IRNode.OOPMAP_WITH, "NarrowOop"})
    static private Object test(RefWithKey key, RefWithKey[] refs) {
        RefWithKey k = null;
        // This loop causes the register allocator to not "rematerialize" all
        // MachTemp nodes generated for the reference g1LoadN instruction below.
        for (int i = 0; i < refs.length; i++) {
            RefWithKey k0 = refs[0];
            if (k0.equals(key)) {
                k = k0;
            }
        }
        if (k != null && !key.equals(k)) {
            return null;
        }
        // The MachTemp node implementing the dst TEMP operand in the g1LoadN
        // instruction corresponding to k.get() can be scheduled across the
        // above call to RefWithKey::equals(), due to an unfortunate interaction
        // of inaccurate basic block frequency estimation (emulated in this test
        // by randomizing the GCM and LCM heuristics) and call-catch cleanup.
        // Since narrow pointer MachTemp nodes are typed as narrow OOPs, this
        // causes the oopmap builder to include the MachTemp node definition in
        // the RefWithKey::equals() return oopmap.
        return (k != null) ? k.get() : null;
    }

    @Run(test = "test")
    @Warmup(0)
    public void run() {
        RefWithKey ref = new RefWithKey(42);
        test(ref, new RefWithKey[]{ref});
    }
}
