package com.artfulbits.ui.binding;

import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.BaseAdapter;

import com.artfulbits.ui.binding.reflection.Property;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manager class responsible for controlling integration of the binding library into corresponding fragment or activity.
 * It controls aspects:<br/> <ul> <li>context instance extracting;</li> <li>Binding defining and configuring;</li>
 * <li></li> </ul>
 */
@SuppressWarnings("unused")
public class BindingManager {
  /* [ CONSTANTS AND MEMBERS ] ==================================================================================== */

  /** Associated with POP action constant. */
  private final static boolean DO_POP = true;
  /** Associated with PUSH action constant. */
  private final static boolean DO_PUSH = false;

  /** Weak references on listeners. */
  private final Set<LifecycleCallback> mListeners = new WeakHashMap<LifecycleCallback, LifecycleCallback>().keySet();
  /** Facade For all types of the Views. */
  private final ViewFacade mFacade;
  /** Collection of all defined binding rules. */
  private final List<Binder> mRules = new LinkedList<Binder>();
  /** Freeze counter. */
  private final AtomicInteger mFreezeCounter = new AtomicInteger(0);
  /** Set of binding exchange transactions. We store order, binder and direction (boolean: true - pop, false - push). */
  private final List<Pair<Binder, Boolean>> mPending = new ArrayList<>();

  /* [ CONSTRUCTORS ] ============================================================================================= */

  public BindingManager(final Activity parent) {
    mFacade = new ViewFacade(parent);
  }

  public BindingManager(final Fragment parent) {
    mFacade = new ViewFacade(parent);
  }

  public BindingManager(final android.support.v4.app.Fragment parent) {
    mFacade = new ViewFacade(parent);
  }

  public BindingManager(final View parent) {
    mFacade = new ViewFacade(parent);
  }

  public BindingManager(final BaseAdapter adapter) {
    mFacade = new ViewFacade(adapter);
  }

  /* [ BINDING RULES DEFINING ] =================================================================================== */

  public List<Binder> getBindings() {
    return mRules;
  }

  public List<Binder> getBindingsByInstance(final Object instance) {
    final List<Binder> result = new LinkedList<>();

    for (Binder<?, ?> b : mRules) {
      if (instance.equals(b.getRuntimeModel())) {
        result.add(b);
      }
    }

    return result;
  }

  public List<Binder> getSuccessBindings() {
    final List<Binder> result = new LinkedList<>();

    // TODO: do the search

    return result;
  }

  public List<Binder> getFailedBindings() {
    final List<Binder> result = new LinkedList<>();

    // TODO: do the search

    return result;
  }

  public <TLeft, TRight> Binder<TLeft, TRight> bind() {
    final Binder<TLeft, TRight> result = new Binder<>();

    return result.attachToManager(this);
  }

  public <TLeft, TRight> Binder<TLeft, TRight> bind(final Selector<?, Property<TLeft>> view,
                                                    final Selector<?, Property<TRight>> model) {
    final Binder<TLeft, TRight> result = new Binder<>();

    return result
        .view(view)
        .model(model)
        .attachToManager(this);
  }

  /* [ LIFECYCLE ] ================================================================================================ */

  /** Register lifecycle extender listener. */
  public BindingManager register(final LifecycleCallback listener) {
    if (null != listener) {
      mListeners.add(listener);
    }

    return this;
  }

  /** Unregister lifecycle extender listener. */
  public BindingManager unregister(final LifecycleCallback listener) {
    if (null != listener) {
      mListeners.remove(listener);
    }

    return this;
  }

	/* [ PUSH AND POP ] ============================================================================================= */

  /**
   * Force model instance update by values from view's.
   *
   * @param instance the instance of model
   */
  public BindingManager pushByInstance(@NonNull final Object instance) {
    for (final Binder bind : getBindingsByInstance(instance)) {
      push(bind);
    }

    return this;
  }

  /**
   * Force model instance update by value from view with respect to 'Freeze mode'.
   *
   * @param binder binding rule.
   */
  public BindingManager push(@NonNull final Binder binder) {
    if (isFrozen()) {
      mPending.add(new Pair<>(binder, DO_PUSH));
    } else {
      binder.push();
    }

    return this;
  }

  /**
   * Force views updates that are bind to the provided model instance.
   *
   * @param instance the instance of model
   */
  public BindingManager popByInstance(@NonNull final Object instance) {
    for (final Binder bind : getBindingsByInstance(instance)) {
      pop(bind);
    }

    return this;
  }

  /**
   * Force views updates that are bind to the provided model instance with respect to 'Freeze mode'.
   *
   * @param binder binding rule.
   */
  public BindingManager pop(@NonNull final Binder binder) {
    if (isFrozen()) {
      mPending.add(new Pair<>(binder, DO_POP));
    } else {
      binder.pop();
    }

    return this;
  }

  /**
   * Are we in 'freeze mode' state or not?
   *
   * @return {@code true} - if we frozen, otherwise {@code false}.
   */
  public boolean isFrozen() {
    return mFreezeCounter.get() > 0;
  }

  /** Stop triggering of all data push/pop operations. */
  public BindingManager freeze() {
    mFreezeCounter.incrementAndGet();
    return this;
  }

  /** Recover triggering of all data push/pop operations. */
  public BindingManager unfreeze() {
    if (0 >= mFreezeCounter.decrementAndGet()) {
      mFreezeCounter.set(0);

      // execute pending data exchange requests
      if (!mPending.isEmpty()) {
        for (Pair<Binder, Boolean> p : mPending) {
          if (/* DO_POP == */ p.second) {
            pop(p.first);
          } else {
            push(p.first);
          }
        }

        mPending.clear();
      }
    }

    return this;
  }

  /**
   * Method force binding between data model and view without data exchange. This is useful for binding verification on
   * initial phase. It allows to check that all fields in data model and view exists and can be associated.
   *
   * @throws WrongConfigurationException - found mismatch.
   */
  public void associate() throws WrongConfigurationException {
    // TODO: can be executed only from MAIN thread!

    // TODO: force binding manager evaluate binding for each property
  }

	/* [ NESTED DECLARATIONS ] ====================================================================================== */

  /** Consolidate API for all types of Views. */
  private static final class ViewFacade {
    /** Reference on root element of binding. */
    private final Activity mRootActivity;

    private final View mRootView;

    private final BaseAdapter mRootAdapter;

    private final android.support.v4.app.Fragment mSupportFragment;

    private final Fragment mFragment;

    public ViewFacade(final android.support.v4.app.Fragment fragment) {
      mRootActivity = null;
      mSupportFragment = fragment;
      mFragment = null;
      mRootView = null;
      mRootAdapter = null;
    }

    public ViewFacade(final Fragment fragment) {
      mRootActivity = null;
      mSupportFragment = null;
      mFragment = fragment;
      mRootView = null;
      mRootAdapter = null;
    }

    public ViewFacade(final View parent) {
      mRootActivity = null;
      mSupportFragment = null;
      mFragment = null;
      mRootView = parent;
      mRootAdapter = null;
    }

    public ViewFacade(final Activity parent) {
      mRootActivity = parent;
      mSupportFragment = null;
      mFragment = null;
      mRootView = null;
      mRootAdapter = null;
    }

    public ViewFacade(final BaseAdapter adapter) {
      mRootActivity = null;
      mSupportFragment = null;
      mFragment = null;
      mRootView = null;
      mRootAdapter = adapter;
    }
  }

  /**
   * Lifecycle extending callback. Implement it if you want to enhance original lifecycle by new state, during which
   * binding operation is the most suitable.
   */
  public interface LifecycleCallback {
    void onCreateBinding(final BindingManager bm);

    void onValidationResult(final BindingManager bm, final boolean success);
  }
}