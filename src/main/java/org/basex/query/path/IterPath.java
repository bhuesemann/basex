package org.basex.query.path;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.util.InputInfo;

/**
 * Iterative path expression for location paths which return sorted and
 * duplicate-free results.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class IterPath extends AxisPath {
  /**
   * Constructor.
   * @param ii input info
   * @param r root expression
   * @param s axis steps
   * @param t return type
   * @param c cardinality
   */
  IterPath(final InputInfo ii, final Expr r, final Expr[] s, final SeqType t,
      final long c) {
    super(ii, r, s);
    type = t;
    size = c;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) {
    return new NodeIter() {
      Expr[] expr;
      Iter[] iter;
      ANode node;
      int p;

      @Override
      public ANode next() throws QueryException {
        if(iter == null) {
          if(expr == null) {
            expr = step;
            if(root != null) {
              // add root as first expression
              expr = new Expr[step.length + 1];
              expr[0] = root;
              System.arraycopy(step, 0, expr, 1, step.length);
            }
          }
          // create iterator array
          iter = new Iter[expr.length];
          iter[0] = ctx.iter(expr[0]);
        }

        final Value cv = ctx.value;
        final long cp = ctx.pos;

        while(true) {
          final Item item = iter[p].next();
          if(item == null) {
            if(--p == -1) {
              node = null;
              break;
            }
          } else if(p < iter.length - 1) {
            ++p;
            ctx.value = item;
            if(iter[p] == null || !iter[p].reset()) iter[p] = ctx.iter(expr[p]);
          } else {
            if(!item.node()) NODESPATH.thrw(input, this, item.type);
            final ANode n = (ANode) item;
            if(node == null || !node.is(n)) {
              node = n;
              break;
            }
          }
        }

        // reset context and return result
        ctx.value = cv;
        ctx.pos = cp;
        return node;
      }

      @Override
      public boolean reset() {
        iter = null;
        node = null;
        p = 0;
        return true;
      }
    };
  }
}