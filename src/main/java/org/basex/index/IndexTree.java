package org.basex.index;

import org.basex.util.BoolList;
import org.basex.util.IntList;
import org.basex.util.TokenIntMap;
import org.basex.util.Num;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * This class indexes keys in a balanced binary tree, including their id
 * values. Iterator methods are available to traverse through the tree.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Sebastian Gath
 */
class IndexTree {
  /** Factor for resize. */
  static final double FACTOR = 1.2;
  /** Keys saved in the tree. */
  final TokenList keys = new TokenList(FACTOR);
  /** Compressed id values. */
  TokenList values = new TokenList(FACTOR);
  /** Mapping for using existing tree. */
  TokenIntMap maps = new TokenIntMap();
  /** Current iterator node. */
  int cn;

  /** Tree structure [left, right, parent]. */
  private final IntList tree = new IntList(FACTOR);
  /** Flag if a node has been modified. */
  private final BoolList mod = new BoolList();
  /** Tree root node. */
  private int root = -1;
  /** Last iterator node. */
  private int ln;

  /**
   * Indexes the specified key and value.
   * @param key key to be indexed
   * @param value value to be indexes
   * @return int node
   */
  final int index(final byte[] key, final int value) {
    return index(key, value, true);
  }

  /**
   * Indexes the specified key and value. If the key has already been
   * indexed, its value is added to the existing value array.
   * Otherwise, a new index entry is created.
   * @param key key to be indexed
   * @param value value to be indexed
   * @param exist flag for using existing index
   * @return int node
   */
  final int index(final byte[] key, final int value, final boolean exist) {
    // index is empty.. create root node
    if(root == -1) {
      root = n(key, value, -1, exist);
      return root;
    }

    int n = root;
    while(true) {
      final int c = Token.diff(key, keys.get(n));
      if(c == 0) {
        if(exist) {
          values.set(Num.add(values.get(n), value), n);
        } else {
          final int i = maps.get(Num.num(n));
          if(i < 0) {
            maps.add(Num.num(n), values.size());
            values.add(Num.newNum(value));
          } else {
            values.set(Num.add(values.get(i), value), i);
          }
        }
        return n;
      }
      int ch = c < 0 ? l(n) : r(n);
      if(ch != -1) {
        n = ch;
      } else {
        ch = n(key, value, n, exist);
        if(c < 0) {
          l(n, ch);
          a(l(n));
        } else {
          r(n, ch);
          a(r(n));
        }
        return ch;
      }
    }
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  final int size() {
    return values.size();
  }

  /**
   * Initializes the index iterator.
   * will be removed to save memory.
   */
  final void init() {
    cn = root;
    if(cn != -1) while(l(cn) != -1) cn = l(cn);
  }

  /**
   * Checks if the iterator returns more keys.
   * @return true if more keys exist
   */
  final boolean more() {
    return cn != -1;
  }

  /**
   * Returns the next pointer.
   * @return next pointer
   */
  final int next() {
    ln = cn;
    if(r(cn) != -1) {
      cn = r(cn);
      while(l(cn) != -1) cn = l(cn);
    } else {
      int t = cn;
      cn = p(cn);
      while(cn != -1 && t == r(cn)) {
        t = cn;
        cn = p(cn);
      }
    }
    return ln;
  }

  // PRIVATE METHODS ==========================================================

  /**
   * Creates a new node.
   * @param key node key
   * @param value node value
   * @param par pointer on parent node
   * @param exist flag for reusing existing tree
   * @return pointer of the new node
   */
  private int n(final byte[] key, final int value, final int par,
      final boolean exist) {
    tree.add(-1); // left node
    tree.add(-1); // right node
    tree.add(par); // parent node
    mod.add(false);
    keys.add(key);
    values.add(Num.newNum(value));
    if(!exist) maps.add(Num.num(keys.size() - 1), values.size() - 1);
    return mod.size() - 1;
  }

  /**
   * Gets the left child.
   * @param nd current node
   * @return left node
   */
  private int l(final int nd) {
    return tree.get((nd << 1) + nd);
  }

  /**
   * Gets the right child.
   * @param nd current node
   * @return right node
   */
  private int r(final int nd) {
    return tree.get((nd << 1) + nd + 1);
  }

  /**
   * Gets the parent node.
   * @param nd current node
   * @return parent node
   */
  private int p(final int nd) {
    return tree.get((nd << 1) + nd + 2);
  }

  /**
   * Sets the left child.
   * @param nd current node
   * @param val left node
   */
  private void l(final int nd, final int val) {
    tree.set(val, (nd << 1) + nd);
  }

  /**
   * Sets the right child.
   * @param nd current node
   * @param val right node
   */
  private void r(final int nd, final int val) {
    tree.set(val, (nd << 1) + nd + 1);
  }

  /**
   * Sets the parent node.
   * @param nd current node
   * @param val parent node
   */
  private void p(final int nd, final int val) {
    tree.set(val, (nd << 1) + nd + 2);
  }

  /**
   * Adjusts the tree balance.
   * @param nd node to be adjusted
   */
  private void a(final int nd) {
    int n = nd;
    mod.set(true, n);

    while(n != -1 && n != root && mod.get(p(n))) {
      if(p(n) == l(p(p(n)))) {
        final int y = r(p(p(n)));
        if(y != -1 && mod.get(y)) {
          mod.set(false, p(n));
          mod.set(false, y);
          mod.set(true, p(p(n)));
          n = p(p(n));
        } else {
          if(n == r(p(n))) {
            n = p(n);
            rl(n);
          }
          mod.set(false, p(n));
          mod.set(true, p(p(n)));
          if(p(p(n)) != -1) rr(p(p(n)));
        }
      } else {
        final int y = l(p(p(n)));
        if(y != -1 && mod.get(y)) {
          mod.set(false, p(n));
          mod.set(false, y);
          mod.set(true, p(p(n)));
          n = p(p(n));
        } else {
          if(n == l(p(n))) {
            n = p(n);
            rr(n);
          }
          mod.set(false, p(n));
          mod.set(true, p(p(n)));
          if(p(p(n)) != -1) rl(p(p(n)));
        }
      }
    }
    mod.set(false, root);
  }

  /**
   * Left rotation.
   * @param n node to be rotated
   */
  private void rl(final int n) {
    final int r = r(n);
    r(n, l(r));
    if(l(r) != -1) p(l(r), n);
    p(r, p(n));
    if(p(n) == -1) root = r;
    else if(l(p(n)) == n) l(p(n), r);
    else r(p(n), r);
    l(r, n);
    p(n, r);
  }

  /**
   * Right rotation.
   * @param n node to be rotated
   */
  private void rr(final int n) {
    final int l = l(n);
    l(n, r(l));
    if(r(l) != -1) p(r(l), n);
    p(l, p(n));
    if(p(n) == -1) root = l;
    else if(r(p(n)) == n) r(p(n), l);
    else l(p(n), l);
    r(l, n);
    p(n, l);
  }
}
