package com.artfulbits.binding.toolbox;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.artfulbits.binding.Formatting;
import com.artfulbits.binding.exceptions.OneWayBindingError;

/**
 * Methods for construction of typical data type converter's.
 * <p/>
 * Why MOLD? I like short names. Synonyms are: mold, makeup, cast, formatting, formatter, converter.
 */
@SuppressWarnings({"unused", "unchecked"})
public final class Molds {
  /* [ CONSTRUCTORS ] ============================================================================================== */

  /** hidden constructor. */
  private Molds() {
    throw new AssertionError();
  }

	/* [ STATIC METHODS - GENERIC ] ================================================================================== */

  /** No formatting. Input and Output is the same value. */
  @NonNull
  public static <T, V> Formatting<T, V> direct() {
    return new Formatting<T, V>() {
      @Override
      public T toView(V value) {
        return (T) value;
      }

      @Override
      public V toModel(T value) {
        return (V) value;
      }
    };
  }

  /** Reverse formatting instance. */
  @NonNull
  public static <T, V> Formatting<T, V> reverse(@NonNull final Formatting<V, T> f) {
    return new Formatting<T, V>() {
      @Override
      public T toView(final V value) {
        return f.toModel(value);
      }

      @Override
      public V toModel(final T value) {
        return f.toView(value);
      }
    };
  }

  /**
   * Create one way binding - allowed only POP operation, from MODEL to VIEW. VIEW to MODEL - not allowed.
   */
  @NonNull
  public static <T, V> Formatting<T, V> onlyPop(@NonNull final Formatting<T, V> f) {
    return new Formatting<T, V>() {
      @Override
      public T toView(final V value) {
        return f.toView(value);
      }

      @Override
      public V toModel(final T value) {
        throw new OneWayBindingError();
      }
    };
  }

  /**
   * Create one way binding - allowed only POP operation, from MODEL to VIEW. VIEW to MODEL - not allowed.
   */
  @NonNull
  public static <T, V> Formatting<T, V> onlyPop(@NonNull final ToView<T, V> f) {
    return new Formatting<T, V>() {
      @Override
      public T toView(final V value) {
        return f.toView(value);
      }

      @Override
      public V toModel(final T value) {
        throw new OneWayBindingError();
      }
    };
  }

  /**
   * Create one way binding - allowed only PUSH operation, from VIEW to MODEL. MODEL to VIEW - not allowed.
   */
  @NonNull
  public static <T, V> Formatting<T, V> onlyPush(@NonNull final Formatting<T, V> f) {
    return new Formatting<T, V>() {
      @Override
      public T toView(final V value) {
        throw new OneWayBindingError();
      }

      @Override
      public V toModel(final T value) {
        return f.toModel(value);
      }
    };
  }

  /**
   * Create one way binding - allowed only PUSH operation, from VIEW to MODEL. MODEL to VIEW - not allowed.
   */
  @NonNull
  public static <T, V> Formatting<T, V> onlyPush(@NonNull final ToModel<V, T> f) {
    return new Formatting<T, V>() {
      @Override
      public T toView(final V value) {
        throw new OneWayBindingError();
      }

      @Override
      public V toModel(final T value) {
        return f.toModel(value);
      }
    };
  }

  /**
   * Create composition from two instances.
   *
   * @param m instance that knows how to convert to MODEL.
   * @param v instance that knows how to convert to VIEW.
   */
  @NonNull
  public static <T, V> Formatting<T, V> join(@NonNull final ToView<T, V> v, @NonNull final ToModel<V, T> m) {
    return new Formatting<T, V>() {
      @Override
      public V toModel(final T value) {
        return m.toModel(value);
      }

      @Override
      public T toView(final V value) {
        return v.toView(value);
      }
    };
  }

  /** Create chained formatting. */
  @NonNull
  public static <T, V, Z> Formatting<T, V> chain(@NonNull final Formatting<T, Z> outer,
                                                 @NonNull final Formatting<Z, V> inner) {
    return new Formatting<T, V>() {
      @Override
      public V toModel(final T value) {
        return inner.toModel(outer.toModel(value));
      }

      @Override
      public T toView(final V value) {
        return outer.toView(inner.toView(value));
      }
    };
  }

  /* [ CONCRETE IMPLEMENTATIONS ] ================================================================================== */

  /** Convert String to Number and vise verse. */
  @NonNull
  /* package */ static <T extends Number> Formatting<CharSequence, T> fromCharsToNumber(@NonNull final Class<T> type) {
    return new Formatting<CharSequence, T>() {
      @Override
      public CharSequence toView(final T value) {
        return value.toString();
      }

      @Override
      public T toModel(final CharSequence cs) {
        String value = cs.toString();

        // No value is equal to ZERO
        if (TextUtils.isEmpty(value))
          value = "0";

        if (Byte.class.equals(type)) {
          return (T) Byte.valueOf(value);
        } else if (Short.class.equals(type)) {
          return (T) Short.valueOf(value);
        } else if (Integer.class.equals(type)) {
          return (T) Integer.valueOf(value);
        } else if (Long.class.equals(type)) {
          return (T) Long.valueOf(value);
        } else if (Float.class.equals(type)) {
          return (T) Float.valueOf(value);
        } else if (Double.class.equals(type)) {
          return (T) Double.valueOf(value);
        }

        throw new AssertionError("Unsupported type. Not implemented yet.");
      }
    };
  }

  /**  */
  @NonNull
  /* package */ static <T extends Number> Formatting<String, T> fromStringToNumber(@NonNull final Class<T> type) {
    return chain(fromStringToChars(), fromCharsToNumber(type));
  }

  /** String to Integer. */
  @NonNull
  public static Formatting<CharSequence, Integer> fromCharsToInteger() {
    return fromCharsToNumber(Integer.class);
  }

  @NonNull
  public static Formatting<Integer, Boolean> fromIntegerToBoolean() {
    return new Formatting<Integer, Boolean>() {
      @Override
      public Boolean toModel(final Integer value) {
        return value > 0;
      }

      @Override
      public Integer toView(final Boolean value) {
        return value ? 1 : 0;
      }
    };
  }

  @NonNull
  public static Formatting<String, CharSequence> fromStringToChars() {
    return reverse(fromCharsToString());
  }

  @NonNull
  public static Formatting<CharSequence, String> fromCharsToString() {
    return new Formatting<CharSequence, String>() {
      @Override
      public String toModel(final CharSequence value) {
        return value.toString();
      }

      @Override
      public CharSequence toView(final String value) {
        return value;
      }
    };
  }
}
