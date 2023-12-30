// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package dev.reprator.core

interface AppLogger {

  /** Log a verbose exception and a message with optional format args.  */
  fun v(throwable: Throwable? = null, message: () -> String = { "" }) = Unit

  /** Log a debug exception and a message with optional format args.  */
  fun d(throwable: Throwable? = null, message: () -> String = { "" }) = Unit

  /** Log a info exception and a message with optional format args.  */
  fun i(throwable: Throwable? = null, message: () -> String = { "" }) = Unit

  /** Log an exception and a message with optional format args.  */
  fun e(throwable: Throwable? = null, message: () -> String = { "" }) = Unit

  /** Log a warning exception and a message with optional format args.  */
  fun w(throwable: Throwable? = null, message: () -> String = { "" }) = Unit
}