package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import org.basex.core.CommandBuilder;
import org.basex.core.Command;
import org.basex.core.User;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdShow;
import org.basex.io.IO;
import org.basex.util.Table;
import org.basex.util.TokenList;

/**
 * Evaluates the 'show backups' command and shows available backups.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ShowBackups extends Command {
  /**
   * Default constructor.
   */
  public ShowBackups() {
    super(User.CREATE);
  }

  @Override
  protected boolean run() throws IOException {
    final Table table = new Table();
    table.description = BACKUPS;
    table.header.add(INFODBNAME);
    table.header.add(INFODBSIZE);

    for(final IO f : prop.dbpath().children()) {
      if(!f.name().endsWith(IO.ZIPSUFFIX)) continue;
      final TokenList tl = new TokenList();
      tl.add(f.name());
      tl.add(f.length());
      table.contents.add(tl);
    }
    table.sort();
    out.println(table.finish());
    return true;
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.SHOW + " " + CmdShow.BACKUPS);
  }
}
