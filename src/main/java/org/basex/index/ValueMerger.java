package org.basex.index;

import static org.basex.data.DataText.*;
import java.io.IOException;
import org.basex.core.cmd.DropDB;
import org.basex.data.Data;
import org.basex.io.DataInput;
import org.basex.io.IO;

/**
 * This class provides data for merging temporary value indexes.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class ValueMerger {
  /** Index instance. */
  private final DiskValues dv;
  /** Index keys. */
  private final DataInput dk;
  /** File prefix. */
  private final String pref;
  /** Data reference. */
  private final Data data;

  /** Current key. */
  byte[] key;
  /** Current values. */
  byte[] values;

  /**
   * Constructor.
   * @param d data reference
   * @param txt text flag
   * @param i merge id
   * @throws IOException I/O exception
   */
  ValueMerger(final Data d, final boolean txt, final int i) throws IOException {
    pref = (txt ? DATATXT : DATAATV) + i;
    dk = new DataInput(d.meta.file(pref + 't'));
    dv = new DiskValues(d, txt, pref);
    data = d;
    next();
  }

  /**
   * Jumps to the next value. {@link #values} will have 0 entries if the
   * end of file is reached.
   * @throws IOException I/O exception
   */
  void next() throws IOException {
    values = dv.nextValues();
    if(values.length != 0) {
      key = dk.readBytes();
    } else {
      dv.close();
      dk.close();
      DropDB.drop(data.meta.name, pref + '.' + IO.BASEXSUFFIX, data.meta.prop);
    }
  }
}
