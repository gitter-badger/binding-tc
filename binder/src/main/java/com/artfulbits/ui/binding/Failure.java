package com.artfulbits.ui.binding;

import android.support.annotation.NonNull;

/** Implement this interface if you want to know about validation failure. */
public interface Failure {
  /**
   * Raise 'on validation failure' for a specific binder instance..
   *
   * @param bm the manager instance to which binder attached.
   * @param b  the binder instance that raise event
   */
  void onValidationFailure(@NonNull final BindingsManager bm, @NonNull final Binder b);
}