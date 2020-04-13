// BluetoothClient.java

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

import lejos.nxt.comm.*;
import java.io.*;
import javax.bluetooth.*;
import ch.aplu.nxt.*;

/**
 * Class to create a client that connects to a server via Bluetooth.
 * The server's Bluetooth name has to be added manually to the known Bluetooth device list
 * using the Lejos command processor.
 * Keep in mind that a server must be up and running before any client can connect.
 */
public class BluetoothClient
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

  private boolean isConnected = false;
  private String serverName;
  private BTConnection btc = null;
  private InputStream is = null;
  private OutputStream os = null;
  private volatile boolean readError = false;

  public BluetoothClient()
  {}

  /**
   * Creates a BluetoothClient instance for the given server's Bluetooth name
   * that will connect to channel 1.
   * No connection is established a this time.
   */
  public BluetoothClient(String serverName)
  {
    this.serverName = serverName;
  }

  /**
   * Connects the client to the host.
   * @return true, if successful; false, if connection cannot be established
   * because the remote device is not added to the device list
   */
  public boolean connect()
  {
    if (isConnected)
      return true;

    RemoteDevice btrd = Bluetooth.getKnownDevice(serverName);

    if (btrd == null)
      return false;

    btc = Bluetooth.connect(btrd);

    if (btc == null)
      return false;
    btc.setIOMode(NXTConnection.RAW);
    is = btc.openInputStream();
    os = btc.openOutputStream();
    if (!checkConnection(is, os))
      return false;

    isConnected = true;
    return true;
  }

  private boolean checkConnection(InputStream is, OutputStream os)
  {
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
      Bluetooth.reset();  // Will take ReadThread out of blocking read()
                          // Closing the InputStream does not generate an exception (why?)
      return false;
    }

    return true;
  }

  /**
   * If connected, closes input and output streams and releases the Bluetooth communication.
   */
  public void disconnect()
  // Some of the references may be null
  {
    if (!isConnected)
      return;

    try {is.close();} catch (Exception ex) {}
    try {os.close();} catch (Exception ex) {}
    try {btc.close();} catch (Exception ex) {}
    isConnected = false;
  }

  /**
   * Returns the connection state.
   * @return true, if connected, otherwise false
   */
  public boolean isConnected()
  {
    return isConnected;
  }

  public InputStream getInputStream()
  {
    return is;
  }

  public OutputStream getOutputStream()
  {
    return os;
  }

  /**
   * Get the signal strength of the connection.
   * Returns -1 if not connected.
   * Do not invoke while performing a data transfer because
   * the data stream is closed and reopened.
   * @return a value from 0 to 255
   */
  public int getSignalStrength()
  {
    if (!isConnected)
      return -1;
    return btc.getSignalStrength();
  }
}


