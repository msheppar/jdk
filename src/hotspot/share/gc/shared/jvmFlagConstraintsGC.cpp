/*
 * Copyright (c) 2015, 2025, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

#include "gc/serial/cardTableRS.hpp"
#include "gc/shared/collectedHeap.hpp"
#include "gc/shared/gcArguments.hpp"
#include "gc/shared/gcConfig.hpp"
#include "gc/shared/jvmFlagConstraintsGC.hpp"
#include "gc/shared/plab.hpp"
#include "gc/shared/threadLocalAllocBuffer.hpp"
#include "gc/shared/tlab_globals.hpp"
#include "runtime/arguments.hpp"
#include "runtime/globals.hpp"
#include "runtime/globals_extension.hpp"
#include "runtime/javaThread.hpp"
#include "utilities/align.hpp"
#include "utilities/macros.hpp"
#include "utilities/powerOfTwo.hpp"
#if INCLUDE_G1GC
#include "gc/g1/jvmFlagConstraintsG1.hpp"
#endif
#if INCLUDE_PARALLELGC
#include "gc/parallel/jvmFlagConstraintsParallel.hpp"
#endif

// Some flags that have default values that indicate that the
// JVM should automatically determine an appropriate value
// for that flag.  In those cases it is only appropriate for the
// constraint checking to be done if the user has specified the
// value(s) of the flag(s) on the command line.  In the constraint
// checking functions,  FLAG_IS_CMDLINE() is used to check if
// the flag has been set by the user and so should be checked.

static JVMFlag::Error MinPLABSizeBounds(const char* name, size_t value, bool verbose) {
  if ((GCConfig::is_gc_selected(CollectedHeap::G1) || GCConfig::is_gc_selected(CollectedHeap::Parallel)) &&
      (value < PLAB::min_size())) {
    JVMFlag::printError(verbose,
                        "%s (%zu) must be "
                        "greater than or equal to ergonomic PLAB minimum size (%zu)\n",
                        name, value, PLAB::min_size());
    return JVMFlag::VIOLATES_CONSTRAINT;
  }

  return JVMFlag::SUCCESS;
}

JVMFlag::Error MaxPLABSizeBounds(const char* name, size_t value, bool verbose) {
  if ((GCConfig::is_gc_selected(CollectedHeap::G1) ||
       GCConfig::is_gc_selected(CollectedHeap::Parallel)) && (value > PLAB::max_size())) {
    JVMFlag::printError(verbose,
                        "%s (%zu) must be "
                        "less than or equal to ergonomic PLAB maximum size (%zu)\n",
                        name, value, PLAB::max_size());
    return JVMFlag::VIOLATES_CONSTRAINT;
  }

  return JVMFlag::SUCCESS;
}

static JVMFlag::Error MinMaxPLABSizeBounds(const char* name, size_t value, bool verbose) {
  JVMFlag::Error status = MinPLABSizeBounds(name, value, verbose);

  if (status == JVMFlag::SUCCESS) {
    return MaxPLABSizeBounds(name, value, verbose);
  }
  return status;
}

JVMFlag::Error YoungPLABSizeConstraintFunc(size_t value, bool verbose) {
  return MinMaxPLABSizeBounds("YoungPLABSize", value, verbose);
}

JVMFlag::Error OldPLABSizeConstraintFunc(size_t value, bool verbose) {
  return MinMaxPLABSizeBounds("OldPLABSize", value, verbose);
}

JVMFlag::Error MinHeapFreeRatioConstraintFunc(uintx value, bool verbose) {
  if (value > MaxHeapFreeRatio) {
    JVMFlag::printError(verbose,
                        "MinHeapFreeRatio (%zu) must be "
                        "less than or equal to MaxHeapFreeRatio (%zu)\n",
                        value, MaxHeapFreeRatio);
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}

JVMFlag::Error MaxHeapFreeRatioConstraintFunc(uintx value, bool verbose) {
  if (value < MinHeapFreeRatio) {
    JVMFlag::printError(verbose,
                        "MaxHeapFreeRatio (%zu) must be "
                        "greater than or equal to MinHeapFreeRatio (%zu)\n",
                        value, MinHeapFreeRatio);
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}

static JVMFlag::Error CheckMaxHeapSizeAndSoftRefLRUPolicyMSPerMB(size_t maxHeap, intx softRef, bool verbose) {
  if ((softRef > 0) && ((maxHeap / M) > (max_uintx / softRef))) {
    JVMFlag::printError(verbose,
                        "Desired lifetime of SoftReferences cannot be expressed correctly. "
                        "MaxHeapSize (%zu) or SoftRefLRUPolicyMSPerMB "
                        "(%zd) is too large\n",
                        maxHeap, softRef);
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}

JVMFlag::Error SoftRefLRUPolicyMSPerMBConstraintFunc(intx value, bool verbose) {
  return CheckMaxHeapSizeAndSoftRefLRUPolicyMSPerMB(MaxHeapSize, value, verbose);
}

JVMFlag::Error MarkStackSizeConstraintFunc(size_t value, bool verbose) {
  // value == 0 is handled by the range constraint.
  if (value > MarkStackSizeMax) {
    JVMFlag::printError(verbose,
                        "MarkStackSize (%zu) must be "
                        "less than or equal to MarkStackSizeMax (%zu)\n",
                        value, MarkStackSizeMax);
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}

JVMFlag::Error MinMetaspaceFreeRatioConstraintFunc(uint value, bool verbose) {
  if (value > MaxMetaspaceFreeRatio) {
    JVMFlag::printError(verbose,
                        "MinMetaspaceFreeRatio (%u) must be "
                        "less than or equal to MaxMetaspaceFreeRatio (%u)\n",
                        value, MaxMetaspaceFreeRatio);
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}

JVMFlag::Error MaxMetaspaceFreeRatioConstraintFunc(uint value, bool verbose) {
  if (value < MinMetaspaceFreeRatio) {
    JVMFlag::printError(verbose,
                        "MaxMetaspaceFreeRatio (%u) must be "
                        "greater than or equal to MinMetaspaceFreeRatio (%u)\n",
                        value, MinMetaspaceFreeRatio);
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}

JVMFlag::Error InitialTenuringThresholdConstraintFunc(uint value, bool verbose) {
#if INCLUDE_PARALLELGC
  JVMFlag::Error status = InitialTenuringThresholdConstraintFuncParallel(value, verbose);
  if (status != JVMFlag::SUCCESS) {
    return status;
  }
#endif

  return JVMFlag::SUCCESS;
}

JVMFlag::Error MaxTenuringThresholdConstraintFunc(uint value, bool verbose) {
#if INCLUDE_PARALLELGC
  JVMFlag::Error status = MaxTenuringThresholdConstraintFuncParallel(value, verbose);
  if (status != JVMFlag::SUCCESS) {
    return status;
  }
#endif

  // MaxTenuringThreshold=0 means NeverTenure=false && AlwaysTenure=true
  if ((value == 0) && (NeverTenure || !AlwaysTenure)) {
    JVMFlag::printError(verbose,
                        "MaxTenuringThreshold (0) should match to NeverTenure=false "
                        "&& AlwaysTenure=true. But we have NeverTenure=%s "
                        "AlwaysTenure=%s\n",
                        NeverTenure ? "true" : "false",
                        AlwaysTenure ? "true" : "false");
    return JVMFlag::VIOLATES_CONSTRAINT;
  }
  return JVMFlag::SUCCESS;
}

JVMFlag::Error MaxGCPauseMillisConstraintFunc(uintx value, bool verbose) {
#if INCLUDE_G1GC
  JVMFlag::Error status = MaxGCPauseMillisConstraintFuncG1(value, verbose);
  if (status != JVMFlag::SUCCESS) {
    return status;
  }
#endif

  return JVMFlag::SUCCESS;
}

JVMFlag::Error GCPauseIntervalMillisConstraintFunc(uintx value, bool verbose) {
#if INCLUDE_G1GC
  JVMFlag::Error status = GCPauseIntervalMillisConstraintFuncG1(value, verbose);
  if (status != JVMFlag::SUCCESS) {
    return status;
  }
#endif

  return JVMFlag::SUCCESS;
}

// To avoid an overflow by 'align_up(value, alignment)'.
static JVMFlag::Error MaxSizeForAlignment(const char* name, size_t value, size_t alignment, bool verbose) {
  size_t aligned_max = ((max_uintx - alignment) & ~(alignment-1));
  if (value > aligned_max) {
    JVMFlag::printError(verbose,
                        "%s (%zu) must be "
                        "less than or equal to aligned maximum value (%zu)\n",
                        name, value, aligned_max);
    return JVMFlag::VIOLATES_CONSTRAINT;
  }
  return JVMFlag::SUCCESS;
}

static JVMFlag::Error MaxSizeForHeapAlignment(const char* name, size_t value, bool verbose) {
  size_t heap_alignment;

#if INCLUDE_G1GC
  if (UseG1GC) {
    // For G1 GC, we don't know until G1CollectedHeap is created.
    heap_alignment = MaxSizeForHeapAlignmentG1();
  } else
#endif
  {
    heap_alignment = GCArguments::compute_heap_alignment();
  }

  return MaxSizeForAlignment(name, value, heap_alignment, verbose);
}

JVMFlag::Error MinHeapSizeConstraintFunc(size_t value, bool verbose) {
  return MaxSizeForHeapAlignment("MinHeapSize", value, verbose);
}

JVMFlag::Error InitialHeapSizeConstraintFunc(size_t value, bool verbose) {
  return MaxSizeForHeapAlignment("InitialHeapSize", value, verbose);
}

JVMFlag::Error MaxHeapSizeConstraintFunc(size_t value, bool verbose) {
  JVMFlag::Error status = MaxSizeForHeapAlignment("MaxHeapSize", value, verbose);

  if (status == JVMFlag::SUCCESS) {
    status = CheckMaxHeapSizeAndSoftRefLRUPolicyMSPerMB(value, SoftRefLRUPolicyMSPerMB, verbose);
  }
  return status;
}

JVMFlag::Error SoftMaxHeapSizeConstraintFunc(size_t value, bool verbose) {
  if (value > MaxHeapSize) {
    JVMFlag::printError(verbose, "SoftMaxHeapSize must be less than or equal to the maximum heap size\n");
    return JVMFlag::VIOLATES_CONSTRAINT;
  }

  return JVMFlag::SUCCESS;
}

JVMFlag::Error HeapBaseMinAddressConstraintFunc(size_t value, bool verbose) {
  // If an overflow happened in Arguments::set_heap_size(), MaxHeapSize will have too large a value.
  // Check for this by ensuring that MaxHeapSize plus the requested min base address still fit within max_uintx.
  if (UseCompressedOops && FLAG_IS_ERGO(MaxHeapSize) && (value > (max_uintx - MaxHeapSize))) {
    JVMFlag::printError(verbose,
                        "HeapBaseMinAddress (%zu) or MaxHeapSize (%zu) is too large. "
                        "Sum of them must be less than or equal to maximum of size_t (%zu)\n",
                        value, MaxHeapSize, max_uintx);
    return JVMFlag::VIOLATES_CONSTRAINT;
  }

  return MaxSizeForHeapAlignment("HeapBaseMinAddress", value, verbose);
}

JVMFlag::Error NewSizeConstraintFunc(size_t value, bool verbose) {
#if INCLUDE_G1GC
  JVMFlag::Error status = NewSizeConstraintFuncG1(value, verbose);
  if (status != JVMFlag::SUCCESS) {
    return status;
  }
#endif

  return JVMFlag::SUCCESS;
}

JVMFlag::Error MinTLABSizeConstraintFunc(size_t value, bool verbose) {
  // At least, alignment reserve area is needed.
  if (value < ThreadLocalAllocBuffer::alignment_reserve_in_bytes()) {
    JVMFlag::printError(verbose,
                        "MinTLABSize (%zu) must be "
                        "greater than or equal to reserved area in TLAB (%zu)\n",
                        value, ThreadLocalAllocBuffer::alignment_reserve_in_bytes());
    return JVMFlag::VIOLATES_CONSTRAINT;
  }
  if (value > (ThreadLocalAllocBuffer::max_size() * HeapWordSize)) {
    JVMFlag::printError(verbose,
                        "MinTLABSize (%zu) must be "
                        "less than or equal to ergonomic TLAB maximum (%zu)\n",
                        value, ThreadLocalAllocBuffer::max_size() * HeapWordSize);
    return JVMFlag::VIOLATES_CONSTRAINT;
  }
  return JVMFlag::SUCCESS;
}

JVMFlag::Error TLABSizeConstraintFunc(size_t value, bool verbose) {
  // Skip for default value of zero which means set ergonomically.
  if (FLAG_IS_CMDLINE(TLABSize)) {
    if (value < MinTLABSize) {
      JVMFlag::printError(verbose,
                          "TLABSize (%zu) must be "
                          "greater than or equal to MinTLABSize (%zu)\n",
                          value, MinTLABSize);
      return JVMFlag::VIOLATES_CONSTRAINT;
    }
    if (value > (ThreadLocalAllocBuffer::max_size() * HeapWordSize)) {
      JVMFlag::printError(verbose,
                          "TLABSize (%zu) must be "
                          "less than or equal to ergonomic TLAB maximum size (%zu)\n",
                          value, (ThreadLocalAllocBuffer::max_size() * HeapWordSize));
      return JVMFlag::VIOLATES_CONSTRAINT;
    }
  }
  return JVMFlag::SUCCESS;
}

// We will protect overflow from ThreadLocalAllocBuffer::record_slow_allocation(),
// so AfterMemoryInit type is enough to check.
JVMFlag::Error TLABWasteIncrementConstraintFunc(uintx value, bool verbose) {
  if (UseTLAB) {
    size_t refill_waste_limit = Thread::current()->tlab().refill_waste_limit();

    // Compare with 'max_uintx' as ThreadLocalAllocBuffer::_refill_waste_limit is 'size_t'.
    if (refill_waste_limit > (max_uintx - value)) {
      JVMFlag::printError(verbose,
                          "TLABWasteIncrement (%zu) must be "
                          "less than or equal to ergonomic TLAB waste increment maximum size(%zu)\n",
                          value, (max_uintx - refill_waste_limit));
      return JVMFlag::VIOLATES_CONSTRAINT;
    }
  }
  return JVMFlag::SUCCESS;
}

JVMFlag::Error SurvivorRatioConstraintFunc(uintx value, bool verbose) {
  if (FLAG_IS_CMDLINE(SurvivorRatio) &&
      (value > (MaxHeapSize / SpaceAlignment))) {
    JVMFlag::printError(verbose,
                        "SurvivorRatio (%zu) must be "
                        "less than or equal to ergonomic SurvivorRatio maximum (%zu)\n",
                        value,
                        (MaxHeapSize / SpaceAlignment));
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}

JVMFlag::Error MetaspaceSizeConstraintFunc(size_t value, bool verbose) {
  if (value > MaxMetaspaceSize) {
    JVMFlag::printError(verbose,
                        "MetaspaceSize (%zu) must be "
                        "less than or equal to MaxMetaspaceSize (%zu)\n",
                        value, MaxMetaspaceSize);
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}

JVMFlag::Error MaxMetaspaceSizeConstraintFunc(size_t value, bool verbose) {
  if (value < MetaspaceSize) {
    JVMFlag::printError(verbose,
                        "MaxMetaspaceSize (%zu) must be "
                        "greater than or equal to MetaspaceSize (%zu)\n",
                        value, MaxMetaspaceSize);
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}

JVMFlag::Error GCCardSizeInBytesConstraintFunc(uint value, bool verbose) {
  if (!is_power_of_2(value)) {
    JVMFlag::printError(verbose,
                        "GCCardSizeInBytes ( %u ) must be "
                        "a power of 2\n",
                        value);
    return JVMFlag::VIOLATES_CONSTRAINT;
  } else {
    return JVMFlag::SUCCESS;
  }
}
