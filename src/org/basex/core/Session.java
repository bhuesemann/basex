package org.basex.core;

import static org.basex.Text.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.basex.BaseX;
import org.basex.BaseXServer;
import org.basex.core.proc.Close;
import org.basex.core.proc.Exit;
import org.basex.core.proc.IntInfo;
import org.basex.core.proc.IntOutput;
import org.basex.core.proc.IntStop;
import org.basex.io.BufferedOutput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Performance;

/**
 * Session for a client-server connection.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class Session implements Runnable {
  /** Server reference. */
  final BaseXServer bxs;
  /** Database context. */
  public final Context context;
  /** Socket. */
  final Socket socket;
  /** Verbose mode. */
  final boolean info;

  /** Core. */
  public Process core;
  /** Timeout thread. */
  Thread timeout;

  /** Input stream. */
  DataInputStream dis;
  /** Print output. */
  PrintOutput out;

  /**
   * Session.
   * @param s socket
   * @param i info mode
   * @param b server reference
   */
  public Session(final Socket s, final boolean i, final BaseXServer b) {
    context = new Context(b.context);
    socket = s;
    info = i;
    bxs = b;
    new Thread(this).start();
  }

  /**
   * Handles client-server communication.
   * @throws IOException I/O exception
   */
  private void handle() throws IOException {
    final Performance perf = new Performance();

    // get command and arguments
    dis = new DataInputStream(socket.getInputStream());
    out = new PrintOutput(new BufferedOutput(socket.getOutputStream()));

    if(info) BaseX.outln(this + " Login.");

    while(true) {
      final String in = getMessage();
      if(in == null) {
        stop(false);
        break;
      }
      if(info) BaseX.outln(this + " " + in);

      // parse input and create process instance
      Process proc = null;
      try {
        proc = new CommandParser(in, context, true).parse()[0];
      } catch(final QueryException ex) {
        // invalid command was sent by a client; create empty process
        // with error feedback
        proc = new Process(0) {};
        proc.error(ex.extended());
        core = proc;
        send(false);
        continue;
      }

      if(proc instanceof IntStop || proc instanceof Exit) {
        send(true);
        stop(true);
        if(proc instanceof IntStop) bxs.stop();
        break;
      } else if(proc instanceof IntOutput || proc instanceof IntInfo) {
        if(core == null) {
          out.print(BaseX.info(SERVERTIME));
        } else {
          if(proc instanceof IntOutput) {
            core.output(out);
            out.write(new byte[IO.BLOCKSIZE]);
          } else {
            new DataOutputStream(out).writeUTF(core.info());
          }
          out.flush();
        }
      } else {
        core = proc;
        timeout(proc);
        send(proc.execute(context));
        if(proc.info().equals(PROGERR)) proc.error(SERVERTIME);
        timeout.interrupt();
      }
      if(info) BaseX.outln(this + " " + perf);
    }

    if(info) BaseX.outln(this + " Logout.");
  }

  /**
   * Times out a process.
   * @param proc process reference
   */
  private void timeout(final Process proc) {
    timeout = new Thread() {
      @Override
      public void run() {
        Performance.sleep(context.prop.num(Prop.TIMEOUT) * 1000);
        proc.stop();
      }
    };
    timeout.start();
  }

  /**
   * Returns the message from the client.
   * @return message
   */
  String getMessage() {
    try {
      return dis.readUTF();
    } catch(final IOException ex) {
      BaseX.debug(ex);
      // for stopping all client threads cause of server stop
      return null;
    }
  }

  /**
   * Sends the success flag to the client.
   * @param ok success flag
   * @throws IOException I/O exception
   */
  void send(final boolean ok) throws IOException {
    out.write(ok ? 0 : 1);
    out.flush();
  }

  /**
   * Stops the session.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    dis.close();
  }

  /**
   * Closes the session.
   * @param s boolean
   */
  private void stop(final boolean s) {
    if(s) bxs.sessions.remove(this);
    new Close().execute(context);

    if(timeout != null) timeout.interrupt();
    try {
      socket.close();
    } catch(final IOException ex) {
      BaseXServer.error(ex, false);
    }
  }

  /**
   * Starts the session.
   */
  public void run() {
    try {
      handle();
    } catch(final IOException ex) {
      BaseXServer.error(ex, false);
    }
  }

  @Override
  public String toString() {
    final String host = socket.getInetAddress().getHostAddress();
    final int port = socket.getPort();
    return BaseX.info("[%:%]", host, port);
  }
}