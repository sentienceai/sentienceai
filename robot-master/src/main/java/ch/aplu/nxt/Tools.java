// Tools.java

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

import lejos.nxt.*;
import lejos.nxt.comm.*;
import java.util.Vector;
import javax.bluetooth.*;
import java.util.ArrayList;

/**
 * Class with some useful helper methods.
 */
public class Tools
{
  private static long startTime = 0L;
  private static Thread t = null;
  private static boolean isSleeping;
  private static Object monitor = new Object();

  /**
   * Starts a timer or restart it by setting its time to zero.
   */
  public static void startTimer()
  {
    startTime = System.currentTimeMillis();
  }

  /**
   * Gets the timer's time.
   * @return the current time of the timer (in ms)
   */
  public static long getTime()
  {
    if (startTime == 0)
      return 0L;
    else
      return System.currentTimeMillis() - startTime;
  }

  /**
   * Suspends execution of the current thread for the given amount of time.
   * (Other threads may continue to run.)
   * @param duration the duration (in ms)
   */
  public static void delay(long duration)
  {
    try
    {
      Thread.currentThread().sleep(duration);
    }
    catch (InterruptedException ex)
    {
    }
  }

  // For compatiblity with J2ME
  protected static int round(double x)
  {
    return (int)(Math.floor(x + 0.5));
  }

  /**
   * Puts the current thread in a wait state until wakeUp() is called
   * or timeout (in ms) expires. If timeout <= 0 the method blocks infinitely
   * until wakeUp() is called. Only one thread may be in the wait state.<br>
   * Return values:<br>
   * true:  wakeUp was called before timeout expired ("no timeout occured")<br>
   * false: timeout expired before wakeUp was called ("timeout occured")<br>
   */
  public static boolean putSleep(final int timeout)
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

  /**
   * Same as putSleep(int timeout) with timeout = 0 (timeout disalbed).
   */
  public static boolean putSleep()
  {
    return putSleep(0);
  }

  /**
   * Wakes up the waiting thread.
   */
  public static void wakeUp()
  {
    if (t != null)
      t.interrupt();  // Stop delay(), this will stop the timeout thread
    synchronized(monitor)
    {
      monitor.notify();
    }
    isSleeping = false;
  }

  private static boolean waitTimeout(int time)
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
   * Waits until ENTER button is hit.
   */
  public static void waitEnter()
  {
    // leJOS 0.6, 0.7
    /*
    try
    {
      Button.ENTER.waitForPressAndRelease();
    }
    catch (InterruptedException ex)
    {}
    */

    // leJOS 0.85
    Button.ENTER.waitForPressAndRelease();
  }

  /**
   * Displays the prompt and waits until ENTER button is hit.
   */
  public static void waitEnter(String prompt)
  {
    if (prompt != null)
      LCD.drawString(prompt, 0, 0);
    waitEnter();
  }

  /**
   * Waits until ESCAPE button is hit.
   */
  public static void waitEscape()
  {
    // leJOS 0.6, 0.7
    /*
    try
    {
      Button.ESCAPE.waitForPressAndRelease();
    }
    catch (InterruptedException ex)
    {}
    */

    // leJOS 0.85
    Button.ESCAPE.waitForPressAndRelease();
  }

  /**
   * Displays the prompt and waits until ESCAPE button is hit.
   */
  public static void waitEscape(String prompt)
  {
    if (prompt != null)
      LCD.drawString(prompt, 0, 0);
    waitEscape();
  }

  /**
   * Gets the Bluetooth friendly name of the device.
   * @return the Bluetooth friendly name
   */
  public static String getBtName()
  {
    // Version leJOS 0.6, 0.7
//   byte name[] = Bluetooth.getFriendlyName();
//    return nameToString(name);
    // Version leJOS 0.85
    return Bluetooth.getFriendlyName();
  }

  /**
   * Gets the Bluetooth address of the device.
   * @return the Bluetooth address (as string)
   */
  public static String getBtAddress()
  {
    // Version leJOS 0.6, 0.7
//    byte address[] = Bluetooth.getLocalAddress();;
//    return Bluetooth.addressToString(address);
    // Version leJOS 0.85
    return Bluetooth.getLocalAddress();
  }

  /**
   * Returns a list of all registered Bluetooth devices.
   * Register a new device with the Lejos command processor.
   * @return a string array of all known devices
   */
  public static String[] getKnownBtDevices()
  {
    ArrayList<RemoteDevice> devices = Bluetooth.getKnownDevicesList();
    String[] list = new String[devices.size()];
    RemoteDevice rd;
    for (int i = 0; i < devices.size(); i++)
    {
      rd = devices.get(i);
      list[i] = rd.getFriendlyName(false);
    }
    return list;
  }

  public static boolean isBtDeviceKnown(String name)
  {
    ArrayList<RemoteDevice> devices = Bluetooth.getKnownDevicesList();
    RemoteDevice rd = null;
    for (int i = 0; i < devices.size(); i++)
    {
      rd = devices.get(i);
      String str = rd.getFriendlyName(false);
      if (str.equals(name))
        return true;
    }
    return false;
  }

  private static String nameToString(byte[] name)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < name.length && name[i] > 0; i++)
    {
      sb.append((char)name[i]);
    }
    return sb.toString();
  }
}



