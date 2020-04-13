// BluetoothServer.java

/*
This software is part of the NxtJLib library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.
*/

package ch.aplu.bluetooth;

import java.io.*;
import lejos.nxt.comm.*;
import lejos.nxt.*;
import ch.aplu.nxt.*;

/**
 * Class that creates a Bluetooth server thread.
 * Keep in mind that the server must be started before any client tries to connect.
 */
public class BluetoothServer extends Thread
{
  // ------------- Inner class ReadThread ------------
  private class ReadThread extends Thread
  {
    private InputStream is;

    public ReadThread(InputStream is)
    {
      this.is = is;
    }

    public void run()
    {
      try
      {
        is.read();
      }
      catch (IOException ex)
      {
        readError = true;
      }
    }
  }
  // ------------- End of inner class ----------------

  private volatile boolean isCanceled = false;
  private BtListener listener;
  private BTConnection btc;
  private volatile boolean readError = false;
  private boolean isNxtClient;

  /**
   * Creates a server instance. The given listener
   * gets callback information when a client connects.
   * @param listener the BtListener to get connnect information
   */
  public BluetoothServer(BtListener listener, boolean isNxtClient)
  {
    this.listener = listener;
    this.isNxtClient = isNxtClient;
    start();
  }

  public BluetoothServer(BtListener listener)
  {
    this(listener, true);
  }

  /**
   * For internal use only.
   */
  public void run()
  {
    InputStream is = null;
    OutputStream os = null;

    while (!isCanceled)
    {
      btc = null;
      btc = Bluetooth.waitForConnection();
      if (btc != null)   // null, if Bluetooth.reset() called
      {
        btc.setIOMode(NXTConnection.RAW);
        is = btc.openInputStream();
        os = btc.openOutputStream();

        if (checkConnection(is, os))
          listener.notifyConnection(is, os);
      }
    }
  }

  private boolean checkConnection(InputStream is, OutputStream os)
  {
    if (!isNxtClient)
      return true;
    ReadThread rt = new ReadThread(is);
    rt.start();
    int n = 0;
    while (rt.isAlive() && n < 10)
    {
      Tools.delay(300);
      n++;
    }
    if (n == 10 || readError)
    {
      Bluetooth.reset();  // is.close() does not unblock waiting read() (why?)
      return false;
    }

    byte txByte = 0; // Connection test byte
    try
    {
      os.write(txByte);
      os.flush();
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }

  /**
   * Cancels the blocking waitForConnection() or closes an opened connection.
   * If the server is actually connected to a client, cancel() will also
   * inform the connected client staying in a blocking read method
   * with an end-of-stream value.
   */
  public void cancel()
  {
    isCanceled = true;
    Bluetooth.reset();
  }

  /**
   * Closes the opened connection. Any blocking read method will throw an IOException.
   * A connected client waiting in a blocking read method will get an end-of-stream value.
   * If no transfer is underway, returns immediately.
   */
  public void close()
  {
    if (btc != null)
      btc.close();
  }

  /**
   * Returns true, if cancel was previously called.
   * @return true, if cancel was called, otherwise false
   */
  public boolean isCanceled()
  {
    return isCanceled;
  }
}

