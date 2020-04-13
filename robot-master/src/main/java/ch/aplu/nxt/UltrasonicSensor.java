// UltrasonicSensor.java

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

/**
 * Class that represents an ultrasonic sensor.
 * Most of the code and the documentation
 * taken from the leJOS library (lejos.sourceforge.net, with thanks to the autor.
 */
public class UltrasonicSensor extends I2CSensor
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int NEAR = 0;
    int FAR = 1;
  }

  // -------------- Inner class UltrasonicSensorThread ------------
  private class UltrasonicSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private UltrasonicSensorThread()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("UlTh created");
    }

    public void run()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("UlTh started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (ultrasonicListener != null)
        {
          Tools.delay(SharedConstants.ULTRASONICPOLLDELAY);
          int level = getLevel();
          if (state == SensorState.NEAR && level > triggerLevel)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'far'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'far'(" + getPortLabel() + ")");
              ultrasonicListener.far(getPort(), level);
              state = SensorState.FAR;
              inCallback = false;
            }
          }
          if (state == SensorState.FAR && level <= triggerLevel)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'near'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'near'(" + getPortLabel() + ")");
              ultrasonicListener.near(getPort(), level);
              state = SensorState.NEAR;
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
          DebugConsole.show("UlTh stop failed");
        else
          DebugConsole.show("UlTh stop ok");
    }
  }
  // -------------- End of inner classes -----------------------

  private volatile static boolean inCallback = false;
  private UltrasonicListener ultrasonicListener = null;
  private lejos.nxt.UltrasonicSensor us;
  private UltrasonicSensorThread ust;
  private int state = SensorState.NEAR;
  private int triggerLevel;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public UltrasonicSensor(SensorPort port)
  {
    super(port, SharedConstants.LOWSPEED_9V);
    ust = new UltrasonicSensorThread();
    us = new lejos.nxt.UltrasonicSensor(sp);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public UltrasonicSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("us.init()");
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("us.cleanup()");
    isEvtEnabled = false;  // Disable events
    if (ust != null)
      ust.stopThread();
  }

  /**
   * Registers the given ultrasonic listener for the given trigger level.
   * If the touch thread is not yet started, start it now.
   * @param ultrasonicListener the UltrasonicListener to become registered.
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addUltrasonicListener(UltrasonicListener ultrasonicListener, int triggerLevel)
  {
    this.ultrasonicListener = ultrasonicListener;
    this.triggerLevel = triggerLevel;
    if (!ust.isAlive())
    {
      ust.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(SharedConstants.SENSOREVENTDELAY);
      isEvtEnabled = true;
    }
  }

  /**
   * Registers the given ultrasonic listener with default trigger level 20.
   * @param ultrasonicListener the UltrasonicListener to become registered.
   */
  public void addUltrasonicListener(UltrasonicListener ultrasonicListener)
  {
    addUltrasonicListener(ultrasonicListener, 20);
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
    return us.getDistance();
  }

  /**
   * Polls the sensor.
   * @return the current distance to the closest object in cm
   */
  public int getDistance()
  {
    checkConnect();
    return us.getDistance();
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.UltrasonicSensor.
   * @return the reference of the UltrasonicSensor
   */
  public lejos.nxt.UltrasonicSensor getLejosSensor()
  {
    return us;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("UltrasonicSensor (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
