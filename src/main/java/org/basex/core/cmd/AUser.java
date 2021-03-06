package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.regex.Pattern;

import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.io.IOFile;

/**
 * Abstract class for user commands.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
abstract class AUser extends Command {
  /**
   * Protected constructor, specifying command arguments.
   * @param a arguments
   */
  protected AUser(final String... a) {
    super(User.ADMIN, a);
  }

  /**
   * Returns all users matching the specified glob pattern.
   * If the specified pattern does not contain any special characters,
   * it is treated as literal.
   * @param name user name pattern
   * @return array with database names
   */
  protected final String[] users(final String name) {
    final String pat = name.matches(".*[*?,].*") ? IOFile.regex(name) : name;
    return context.users.find(Pattern.compile(pat,
        Prop.WIN ? Pattern.CASE_INSENSITIVE : 0));
  }

  /**
   * Runs the command for all users and databases.
   * @param off offset for users and optional databases
   * @param opt optional specification of users/databases
   * @return success flag
   */
  protected boolean run(final int off, final boolean opt) {
    final String u = args[off];
    final String d = off + 1 < args.length ? args[off + 1] : null;

    if(!validName(u, true)) return error(NAMEINVALID, u);
    if(d != null && !validName(d, true)) return error(NAMEINVALID, d);

    // retrieve all users; stop if no user is found
    final String[] users = users(u);
    if(users.length == 0) return info(USERNO, u) && opt;
    // retrieve all databases
    String[] dbs = null;
    if(d != null) {
      dbs = databases(d);
      if(dbs.length == 0) return info(DBNOTFOUND, d) && opt;
    }

    // loop through all users
    boolean ok = true;
    for(final String user : users) {
      if(dbs == null) {
        ok &= run(user, null);
      } else {
        for(final String db : dbs) ok &= run(user, db);
      }
    }
    return ok;
  }

  /**
   * Runs the command for the specified user and database.
   * @param user user to be modified
   * @param db database to be modified
   * @return success flag
   */
  protected abstract boolean run(final String user, final String db);
}
