package com.artfulbits.ui.binding.toolbox;

import android.support.annotation.NonNull;

import com.artfulbits.ui.binding.BindingsManager;

/** Basic implementation of the Fragment with binding library enabled support. */
public abstract class BindingFragment extends android.support.v4.app.Fragment implements BindingsManager.Lifecycle {
  /** Instance of binding manager. */
  private BindingsManager mBm = BindingsManager.newInstance(this, this);

  /** get instance of the Binding manager. */
  public BindingsManager getBindingsManager() {
    return mBm;
  }

  @Override
  public void onStart() {
    super.onStart();

    // freeze all updates
    mBm.freeze().doStart(); // call of --> onCreateBinding(...);
  }

  @Override
  public void onResume() {
    super.onResume();

    // fragment is ready for updates. UN-freeze can be called several times
    mBm.unfreeze().doResume(); // call of --> onValidationResult
  }

  @Override
  public void onPause() {
    super.onPause();

    mBm.doPause();
  }

  @Override
  public void onDestroy() {
    mBm.doDestroy();

    super.onDestroy();
  }

  @Override
  public void onCreateBinding(@NonNull final BindingsManager bm) {
  }

  @Override
  public void onValidationResult(@NonNull final BindingsManager bm, boolean success) {
  }
}
