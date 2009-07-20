package org.basex.gui.view.explore;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIToolBar;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;
import org.basex.gui.view.ViewNotifier;

/**
 * This view allows the input of database queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ExploreView extends View {
  /** Header string. */
  private final BaseXLabel header;
  /** Current search panel. */
  ExploreArea search;

  /** Execute button. */
  final BaseXButton go;
  /** Filter button. */
  final BaseXButton filter;

  /**
   * Default constructor.
   * @param man view manager
   */
  public ExploreView(final ViewNotifier man) {
    super(HELPEXPLORE, man);

    setLayout(new BorderLayout(0, 4));
    setBorder(6, 8, 8, 8);
    setFocusable(false);

    header = new BaseXLabel(EXPLORETIT, true, false);

    final BaseXBack back = new BaseXBack(Fill.NONE);
    back.setLayout(new BorderLayout());
    back.add(header, BorderLayout.CENTER);

    go = new BaseXButton(GUI.icon("cmd-go"), HELPGO, gui);
    go.trim();
    go.addKeyListener(this);
    go.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        search.query(true);
      }
    });

    filter = GUIToolBar.newButton(GUICommands.FILTER, gui);
    filter.addKeyListener(this);

    BaseXBack sp = new BaseXBack(Fill.NONE);
    sp.setLayout(new TableLayout(1, 5));
    sp.add(go);
    sp.add(Box.createHorizontalStrut(1));
    sp.add(filter);
    back.add(sp, BorderLayout.EAST);
    add(back, BorderLayout.NORTH);

    search = new ExploreArea(this);
    add(search, BorderLayout.CENTER);
    
    refreshLayout();
  }

  @Override
  public void refreshInit() {
    search.init();
  }

  @Override
  public void refreshFocus() { }

  @Override
  public void refreshMark() {
    BaseXLayout.enable(go, !GUIProp.execrt);
    final Nodes marked = gui.context.marked();
    BaseXLayout.enable(filter, !GUIProp.filterrt &&
        marked != null && marked.size() != 0);
  }

  @Override
  public void refreshContext(final boolean more, final boolean quick) { }

  @Override
  public void refreshUpdate() { }

  @Override
  public void refreshLayout() {
    header.setFont(GUIConstants.lfont);
    refreshMark();
  }

  @Override
  protected boolean visible() {
    return GUIProp.showexplore;
  }
  
  @Override
  public void keyPressed(final KeyEvent e) {
    if(e.isAltDown()) super.keyPressed(e);
  }
}