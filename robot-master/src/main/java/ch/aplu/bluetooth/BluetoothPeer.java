// BluetoothPeer.java

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
import ch.aplu.nxt.*;

/**
 * Class that implements a Bluetooth peer-to-peer communication based on
 * the client-server model. When started, an existing server is searched
 * using a specified Bluetooth service. If found, the connection is establish
 * in a client mode. Otherwise a server mode with the given service name is started.<br><br>
 *
 * The Bluetooth names of each remote device have to be added manually
 * to the known Bluetooth device list using the Lejos command processor.<br><br>
 *
 * Data is exchanged as blocks of 1..100 integers.
 */
public class BluetoothPeer
{

  // --------------- Inner class MyBtListener --------------
  private class MyBtListener implements BtListener
  {
    // I am a server and a client connected successfully
    public void notifyConnection(InputStream is, OutputStream os)
    {
      isConnected = true;
      dis = new DataInputStream(is);
      dos = new DataOutputStream(os);

      if (listener != null)
        listener.notifyConnection(true);


      int value = 0;
      int size = 0;
      int[] data = null;
      int index = -1;
      try
      {
        while (isConnected)
        {
          // Blocking, throws IOException
          // when client closes connection or BluetoothServer.cancel() is called
          // For an unknown reason, readInt() may return with a fault value when
          // the client closes the connection. We restrict valid values to 1..100.
          value = dis.readInt();
          if (index == -1)
          {
            if (value > 100 || value <= 0)
            {
              isConnected = false;
              break;
            }
            size = value;
            data = new int[size];
            index = 0;
          }
          else
          {
            data[index++] = value;
            if (index == size)
            {
              index = -1;
             if (listener != null)
               listener.receiveDataBlock(data);
            }
          }
        }
      }
      catch (IOException ex)
      {
        isConnected = false;
      }
      bs.cancel();

      if (listener != null)
        listener.notifyConnection(false);
    }
  }

  // I am a client and handle the connection
  private class ExecuteThread extends Thread
  {
    public void run()
    {
      dis = new DataInputStream(bc.getInputStream());
      dos = new DataOutputStream(bc.getOutputStream());

      isRunning = true;
      boolean isError = false;

      int value = 0;
      int size = 0;
      int[] data = null;
      int index = -1;

      while (isRunning)
      {
        try
        {
          value = dis.readInt(); // Blocking
        }
        catch (EOFException ex)
        {
          isError = true;
        }
        catch (IOException ex)
        {
          isError = true;
        }
        if (isError)
        {
          bc.disconnect();
          isRunning = false;
        }
        else
        {
          if (index == -1)
          {
            size = value;
            if (value > 0 && value <= 100)  // Outside: corrupted data
            {
              data = new int[size];
              index = 0;
            }
          }
          else
          {
            data[index++] = value;
            if (index == size)
            {
              index = -1;
              if (listener != null)
                listener.receiveDataBlock(data);
            }
          }
        }
      }
      if (listener != null)
        listener.notifyConnection(false);
      isConnected = false;
    }
  }

  // --------------- End of inner classes ------------------

  private BtPeerListener listener = null;
  private String nodeName;
  private BluetoothServer bs = null;
  private BluetoothClient bc = null;
  private DataInputStream dis;
  private DataOutputStream dos;
  private boolean isConnected = false;
  private volatile boolean isRunning = false;

  /**
   * Creates a BluetoothPeer instance that will connect to the given node.
   * Starts in client mode and tries to connect to the given node that must
   * run in server mode. If fails, start in server mode waiting for a incoming
   * client connection. In both cases, the constructor does not block.
   * @param nodeName the partner's Bluetooth name, if null, starts in server mode
   * @param listener the BtPeerListener that gets connnection and data notifications
   */
  public BluetoothPeer(String nodeName, BtPeerListener listener)
  {
    this.nodeName = nodeName;
    this.listener = listener;

    if (nodeName == null)
    {
      bs = new BluetoothServer(new MyBtListener());
      return;
    }

    bc = new BluetoothClient(nodeName);
    if (bc.connect())
    {
      isConnected = true;
      new ExecuteThread().start();
      while (!isRunning) {}  // Wait until thread is up and running
      listener.notifyConnection(true);
    }
    else
      bs = new BluetoothServer(new MyBtListener());
  }

  /**
   * Send a block of data to the connected node.
   * Data are queued and the method blocks when the queue is filled
   * (queue space about 1000 ints).
   * @param data the data block to send (size 1..100)
   * @return true, if successful; false, if not connected,
   * invalid data array (length 0) or stream write exception
   */
  public boolean sendDataBlock(int[] data)
  {
    if (!isConnected)
      return false;

    try
    {
      int size = data.length;
      if (size == 0)
        return false;
      dos.writeInt(size);
      for (int i = 0; i < size; i++)
        dos.writeInt(data[i]);
      dos.flush();
    }
    catch (IOException ex)
    {
      return false;
    }
    return true;
  }

  /**
   * Returns the connection status.
   * @return true, if connected, otherwise false
   */
  public boolean isConnected()
  {
    return isConnected;
  }

  /**
   * Releases a connection and inform the connected node's listener by calling
   * notifyConnection(false). If no connection is established, a waiting
   * server is canceled.
   */
  public void releaseConnection()
  {
    if (bc != null)
      bc.disconnect();
    if (bs != null)
      bs.cancel();
  }
}
