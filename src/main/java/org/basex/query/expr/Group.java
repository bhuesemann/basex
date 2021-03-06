package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * Implementation of the group by clause.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Michael Seiferle
 */
public final class Group extends ParseExpr {
  /** Group by specification. */
  private final Var[] groupby;
  /** Grouping partition. **/
  GroupPartition gp;

  /**
   * Constructor.
   * @param ii input info
   * @param gb group by expression
   */
  public Group(final InputInfo ii, final Var[] gb) {
    super(ii);
    groupby = gb;
  }

  /**
   * Initializes the grouping partition.
   * @param fl for/let clauses
   * @param ob order by specifier
   * @throws QueryException exception
   */
  void init(final ForLet[] fl, final Order ob) throws QueryException {
    final Var[] fs = new Var[fl.length];
    for(int i = 0; i < fl.length; ++i) fs[i] = fl[i].var;
    gp = new GroupPartition(groupby, fs, ob, input);
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    for(final Var g : groupby) {
      g.comp(ctx);
      if(g.ret != null) g.ret = SeqType.get(g.ret.type, 1);
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    throw Util.notexpected(this);
  }

  @Override
  public boolean uses(final Use use) {
    for(final Var v : groupby) if(v.uses(use)) return true;
    return false;
  }

  @Override
  public int count(final Var v) {
    int c = 0;
    for(final Var g : groupby) c += g.count(v);
    return c;
  }

  @Override
  public boolean removable(final Var v) {
    // don't allow removal if variable is used
    for(final Var g : groupby) if(g.count(v) != 0) return false;
    return true;
  }

  @Override
  public Expr remove(final Var v) {
    return this;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int o = 0; o != groupby.length; ++o) groupby[o].plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return new TokenBuilder(" " + GROUP + " " + BY + " ").
      addSep(groupby, SEP).toString();
  }
}
