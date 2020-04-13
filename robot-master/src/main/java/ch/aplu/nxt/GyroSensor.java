// GyroSensor.java

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

/**
 * Class that represents a gyro sensor from HiTechnic. The sensor reports
 * the angular velocity. To get the current rotation angle (heading)
 * you must use a separate thread to poll the sensor in a tight loop (of about 1 ms)
 * and integrate the reported values.
 * See also net.mosen.nxt.GyrsoSensor (author Kirk P. Thomson).
 */
public class GyroSensor extends Sensor implements SharedConstants
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int SLOW = 0;
    int FAST = 1;
  }

  // -------------- Inner class GyroSensorThread ------------
  private class GyroSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private GyroSensorThread()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("GyTh created");
    }

    public void run()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("GyTh started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (gyroListener != null)
        {
          Tools.delay(SharedConstants.GYROPOLLDELAY);
          int level = getLevel();
          if (state == SensorState.SLOW && level > triggerLevel)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'fast'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'fast'(" + getPortLabel() + ")");
              gyroListener.fast(getPort(), level);
              state = SensorState.FAST;
              inCallback = false;
            }
          }
          if (state == SensorState.FAST && level <= triggerLevel)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'slow'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'slow'(" + getPortLabel() + ")");
              gyroListener.slow(getPort(), level);
              state = SensorState.SLOW;
              inCallback = false;
            }
          }
        }
      }
    }

    private void stopThread()
    {
      isRunning = false;
      try
      {
        join(500);
      }
      catch (InterruptedException ex)
      {
      }
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        if (isAlive())
          DebugConsole.show("GyTh stop failed");
        else
          DebugConsole.show("GyTh stop ok");
    }
  }
// -------------- End of inner classes -----------------------

  private lejos.nxt.addon.GyroSensor gs;
  private lejos.nxt.SensorPort sp;
  private GyroListener gyroListener = null;
  private int state = SensorState.SLOW;
  private int triggerLevel;
  private volatile static boolean inCallback = false;
  private GyroSensorThread gst;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public GyroSensor(SensorPort port)
  {
    super(port);
    switch (port.getId())
    {
      case 0:
        sp = lejos.nxt.SensorPort.S1;
        break;
      case 1:
        sp = lejos.nxt.SensorPort.S2;
        break;
      case 2:
        sp = lejos.nxt.SensorPort.S3;
        break;
      case 3:
        sp = lejos.nxt.SensorPort.S4;
        break;
      default:
        sp = lejos.nxt.SensorPort.S1;
        break;
    }
    gst = new GyroSensorThread();
    gs = new lejos.nxt.addon.GyroSensor((ADSensorPort)sp);
    gs.setOffset(0);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public GyroSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("gs.init()");
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("gs.cleanup()");
    isEvtEnabled = false;  // Disable events
    if (gst != null)
      gst.stopThread();
 }

   /**
   * Registers the given gyro listener for the given trigger level.
   * @param gyroListener the GyroListener to become registered.
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addGyroListener(GyroListener gyroListener, int triggerLevel)
  {
    this.gyroListener = gyroListener;
    this.triggerLevel = triggerLevel;
    if (!gst.isAlive())
    {
      gst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(SharedConstants.SENSOREVENTDELAY);
      isEvtEnabled = true;
    }
  }

  /**
   * Registers the given gyro listener with default trigger level 500.
   * @param gyroListener the GyroListener to become registered.
   */
  public void addGyroListener(GyroListener gyroListener)
  {
    addGyroListener(gyroListener, 500);
  }

   /**
   * Sets a new trigger level and returns the previous one.
   * @param triggerLevel the new trigger level
   * @return the previous trigger level
   */
  public int setTriggerLevel(int triggerLevel)
  {
    int oldLevel = this.triggerLevel;
    this.triggerLevel = triggerLevel;
    return oldLevel;
  }

  private int getLevel()
  {
    if (robot == null)
      return -1;
    return gs.readValue();
  }

  /**
   * Polls the sensor.
   * @return the current value (in degrees per second with offset of about 600)
   */
  public int getValue()
  {
    checkConnect();
    return gs.readValue();
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.GyroSensor.
   * @return the reference of the GyroSensor
   */
  public lejos.nxt.addon.GyroSensor getLejosSensor()
  {
    return gs;
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.SensorPort.
   * @return the reference of the SensorPort
   */
  public lejos.nxt.SensorPort getLejosPort()
  {
    return sp;
  }

  private void checkConnect()
  {
    if (robot == null)
     new ShowError("GyroSensor is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }

}
