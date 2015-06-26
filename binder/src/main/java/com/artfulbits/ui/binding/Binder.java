package com.artfulbits.ui.binding;

import android.support.annotation.NonNull;

import com.artfulbits.ui.binding.reflection.Property;

import org.hamcrest.CoreMatchers;

/** Base class for all binding rules keeping. */
public class Binder<TLeft, TRight> {
  /** Reference on binding owner. */
  private BindingManager mManager;
  /** Reference on view instance. */
  private Selector<?, Property<TLeft>> mView;
  /** Reference on storage instance. */
  private Selector<?, Property<TRight>> mStorage;
  /** View changes listener. */
  private Listener<?> mOnView;
  /** Data model changes listener. */
  private Listener<?> mOnModel;
  /** Data type converter. */
  private Converter<TLeft, TRight> mFormatter;
  /** Data validation. */
  private org.hamcrest.Matcher<TRight> mValidation;
  /** Value used in last evaluated/extracted/exchange operation. View side. */
  private TLeft mLastLeft;
  /** Value used in last evaluated/extracted/exchange operation. Model side. */
  private TRight mLastRight;

  /* ============================================================================================================== */

  /* package */ Binder() {
  }

  /* package */ Binder<TLeft, TRight> attachToManager(final BindingManager manager) {
    mManager = manager;

    // do self registration
    mManager.getBindings().add(this);

    return this;
  }

  /* ============================================================================================================== */

  public Binder<TLeft, TRight> view(final Selector<?, Property<TLeft>> view) {
    mView = view;

    onView(mOnView);

    return this;
  }

  public Binder<TLeft, TRight> model(final Selector<?, Property<TRight>> storage) {
    mStorage = storage;

    onModel(mOnModel);

    return this;
  }

  public Binder<TLeft, TRight> format(final Converter<TLeft, TRight> converter) {
    mFormatter = converter;

    return this;
  }

  public Binder<TLeft, TRight> validate(final org.hamcrest.Matcher<TRight> validator) {
    mValidation = validator;

    return this;
  }

  public Binder<TLeft, TRight> onView(final Listener<?> listener) {
    mOnView = listener;

    if (null != mView) {
      mView.listenTo(mOnView);
    }

    return this;
  }

  public Binder<TLeft, TRight> onModel(final Listener<?> listener) {
    mOnModel = listener;

    if (null != mStorage) {
      mStorage.listenTo(mOnModel);
    }

    return this;
  }

  /* ============================================================================================================== */

  @NonNull
  public Property<TLeft> resolveView() {
    return null;
  }

  @NonNull
  public Property<TRight> resolveModel() {
    return null;
  }

  @NonNull
  public Converter<TLeft, TRight> resolveFormatter() {
    return mFormatter;
  }

  @NonNull
  public org.hamcrest.Matcher<TRight> resolveValidation() {
    if (null == mValidation) {
      // by default we validating only data type
      return CoreMatchers.isA(getModelType());
    }

    return mValidation;
  }

  /**
   * Do data exchange in direction: View --> Model.
   * <p>
   * Data flow: View --> IsChanged --> Formatter --> Validator --> Is Changed --> Model;
   */
  public void pop() {
    // get value from View
    final TLeft lValue = resolveView().get(getRuntimeView());

    // if no changes since last request
    if (mLastLeft == lValue) return;

    // store Value in cache
    mLastLeft = lValue;

    // formatter
    final TRight rValue = resolveFormatter().toIn(lValue);

    // validation
    if (!resolveValidation().matches(rValue)) return;

    // if no changes since last request
    if (mLastRight == rValue) return;

    // store value in cache
    mLastRight = rValue;

    // update Model
    resolveModel().set(getRuntimeModel(), rValue);

  }

  /**
   * Do data exchange in direction: Model --> View.
   * <p>
   * Data flow: Model --> Is Changed --> Validator --> Formatter --> Is Changed --> View.
   */
  public void push() {
    // extract the value
    final TRight rValue = resolveModel().get(getRuntimeModel());

    // is changed?
    if (mLastRight == rValue) return;

    // update value in cache
    mLastRight = rValue;

    // validation passed?
    if (!resolveValidation().matches(rValue)) return;

    // do formatting
    final TLeft lValue = resolveFormatter().toOut(rValue);

    // is changed?
    if (mLastLeft == lValue) return;

    // update cache
    mLastLeft = lValue;

    // update View
    resolveView().set(getRuntimeView(), lValue);
  }

  /* ============================================================================================================== */

  public TLeft getRuntimeModel() {
    return (TLeft) mStorage.getRuntimeInstance();
  }

  public TRight getRuntimeView() {
    return (TRight) mView.getRuntimeInstance();
  }

  protected final Class<TRight> getModelType() {
    return null;
  }

  protected final Class<TLeft> getViewType() {
    return null;
  }
}