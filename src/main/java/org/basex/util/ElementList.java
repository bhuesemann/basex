package org.basex.util;

/**
 * This is an abstract class for storing elements of any kind.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class ElementList {
  /** Initial hash capacity. */
  protected static final int CAP = 1 << 3;
  /** Resize factor for extending the arrays. */
  protected double factor = Array.RESIZE;
  /** Number of elements. */
  protected int size;

  /**
   * Default constructor.
   */
  protected ElementList() { }

  /**
   * Returns a new array size.
   * @return new array size
   */
  protected final int newSize() {
    return Array.newSize(size, factor);
  }

  /**
   * Returns a new array size that is larger than or equal to the specified
   * size.
   * @param min minimum size
   * @return new array size
   */
  protected final int newSize(final int min) {
    return Math.max(newSize(), min);
  }

  /**
   * Returns the number of elements.
   * @return number of elements
   */
  public final int size() {
    return size;
  }

  /**
   * Resets the array size.
   */
  public final void reset() {
    size = 0;
  }
}
