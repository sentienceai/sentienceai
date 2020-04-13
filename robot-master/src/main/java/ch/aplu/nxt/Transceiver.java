// Transceiver.java

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

package ch.aplu.nxt;

import ch.aplu.bluetooth.*;

/**
 * Class that implements a Bluetooth transmitter/receiver module to
 * exchange status information between two NxtRobots.<br><br>
 * The Bluetooth names of each remote device have to be added manually to
 * the known Bluetooth device list using the Lejos command processor.
 */
public class Transceiver extends Part
  implements BtPeerListener
{
  private String recipient;
  private int bufSize;
  private BluetoothPeer bp;
  private TransceiverListener listener = null;
  private boolean isConnected = false;
  private Object monitor = new Object();
  private Thread t = null;
  private boolean isSleeping;

  /**
   * Creates a transceiver instance to prepare a Bluetooth connection
   * with another transceiver running at a remote NXT with given Bluetooth name.
   * Uses the given size of the receiving circular FIFO buffer.
   * @param recipient the Bluetooth name of the remote transceiver
   * @param bufSize the maximal number of integer data the buffer may hold
   */
  public Transceiver(String recipient, int bufSize)
  {
    this.recipient = recipient;
    this.bufSize = bufSize;
  }

  /**
   * Creates a transceiver instance to prepare a Bluetooth connection
   * with another transceiver running at a remote NXT with given Bluetooth name.
   * Uses a default bufferSize of 100 for the receiving circular FIFO buffer.
   * @param recipient the Bluetooth name of the remote transceiver
   */
  public Transceiver(String recipient)
  {
    this(recipient, 100);
  }

  /**
   * Returns the Bluetooth name of the remote transceiver.
   * @return Bluetooth name of remote node
   */
  public String getRecipient()
  {
    return recipient;
  }

  /**
   * Add a listener to get connnection and data notifications.
   * @param listener the TransceiverListener that gets connnection and data notifications
   */
  public void addTransceiverListener(TransceiverListener listener)
  {
    this.listener = listener;
  }

  /**
   * Turns the transceiver on. First try to connect to a
   * remote listening transceiver. If it fails, listen
   * for a remote connection trial. Blocks until the connection is established.
   * If verbose = true, displays connecting information.
   * @param verbose if true, displays information at System.out
   */
  public void switchOn(boolean verbose)
  {
    if (verbose)
      System.out.println("Try connect " + recipient);
    bp = new BluetoothPeer(recipient, this);
    if (!bp.isConnected())
    {
      if (verbose)
        System.out.println("Listening...");
      if (listener != null)
         listener.isListening();
      putSleep();
    }
    isConnected = bp.isConnected();
    if (verbose)
      System.out.println(isConnected? "Connected" : "Canceled");
  }

  /**
   * Same as switchOn(true) (verbose mode).
   */
  public void switchOn()
  {
    switchOn(true);
  }

  /**
   * Turns the transceiver off and releases all resources.
   * If a remote transceiver is connected, it will be notified that the
   * connection is closed.
   */
  public void switchOff()
  {
    if (!isConnected)
      return;
    wakeUp();
    isConnected = false;
    bp.releaseConnection();
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("Transceiver.init()");
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("Transceiver.clean()");
    if (bp != null)
      bp.releaseConnection();
  }

  /**
   * For internal use only.
   */
  public void receiveDataBlock(int[] data)
  {
    if (listener != null)
    {
      if (data.length != 2)  // Error, should not happen
        listener.received(-1, 0);
      else
        listener.received(data[0], data[1]);
    }
  }

  /**
   * For internal use only.
   */
  public void notifyConnection(boolean connected)
  {
     if (connected)
       wakeUp();
     if (listener != null)
       listener.notifyConnection(connected);
  }

  /**
   * Send a state/value information to a connected remote transceiver.
   * Returns immediately if not connected.
   * Data are queued and the method blocks when the queue is filled
   * (queue space about 1000 ints).
   * @param state an integer state number
   * @param value an integer state label
   * @return true,if successful, otherwise false
   */
  public boolean send(int state, int value)
  {
    if (!isConnected)
      return false;
    int[] data = {state, value};
    return bp.sendDataBlock(data);
  }


  private boolean putSleep(final int timeout)
  {
    final Thread currentThread = Thread.currentThread();
    if (timeout > 0)
    {
      t = new Thread()  // Timeout thread
      {
        public void run()
        {
          boolean rc = waitTimeout(timeout);
          if (rc)
            currentThread.interrupt();
        }
      };
      t.start();
    }

    isSleeping = true;
    synchronized(monitor)
    {
      try
      {
        monitor.wait();
      }
      catch (InterruptedException ex)
      {
        return false;
      }
    }
    return true;
  }

  private boolean putSleep()
  {
    return putSleep(0);
  }

  private void wakeUp()
  {
    if (t != null)
      t.interrupt();  // Stop delay(), this will stop the timeout thread
    synchronized(monitor)
    {
      monitor.notify();
    }
    isSleeping = false;
  }

  private boolean waitTimeout(int time)
  {
    try
    {
      t.sleep(time);
    }
    catch (InterruptedException ex)
    {
      return false;
    }
    return true;
  }

  /**
   * Releases a connection and inform the connected node's listener by calling
   * notifyConnection(false). If no connection is established, a waiting
   * server is canceled.
   */
  public void releaseConnection()
  {
    if (bp != null)
    {
      wakeUp();
      bp.releaseConnection();
    }
  }

  /**
   * Returns the connection status.
   * @return true, if connected, otherwise false
   */
  public boolean isConnected()
  {
    return bp != null && bp.isConnected();
  }

}

