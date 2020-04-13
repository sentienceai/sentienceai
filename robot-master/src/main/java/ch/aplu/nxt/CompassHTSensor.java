// CompassHTSensor.java

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
 * Class that represents a compass sensor from HiTechnics.
 * Most of the code and the documentation
 * taken from the leJOS library (lejos.sourceforge.net, with thanks to the autor.
 */
public class CompassHTSensor extends I2CSensor
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int TOLEFT = 0;
    int TORIGHT = 1;
  }

  // -------------- Inner class CompassSensorThread ------------
  private class CompassSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private CompassSensorThread()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("CpTh created");
    }

    public void run()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("CpTh started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (compassListener != null)
        {
          Tools.delay(SharedConstants.COMPASSPOLLDELAY);
          double level = getLevel();
          if (state == SensorState.TOLEFT && isRight(level, triggerLevel))
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'toRight'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'toRight'(" + getPortLabel() + ")");
              compassListener.toRight(getPort(), level);
              state = SensorState.TORIGHT;
              inCallback = false;
            }
          }
          if (state == SensorState.TORIGHT && !isRight(level, triggerLevel))
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'toLeft'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'toLeft'(" + getPortLabel() + ")");
              compassListener.toLeft(getPort(), level);
              state = SensorState.TOLEFT;
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
          DebugConsole.show("CpTh stop failed");
        else
          DebugConsole.show("CpTh stop ok");
    }

    private boolean isRight(double level, double triggerLevel)
    {
      if (triggerLevel <= 180)
      {
        if (level >= triggerLevel && level < 180 + triggerLevel)
          return true;
        return false;
      }
      else
      {
        if (level > triggerLevel - 180 && level < triggerLevel)
          return false;
        return true;
      }
    }
  }
  // -------------- End of inner classes -----------------------

  private volatile static boolean inCallback = false;
  private CompassListener compassListener = null;
  private lejos.nxt.addon.CompassHTSensor cs;
  private CompassSensorThread cst;
  private int state = SensorState.TOLEFT;
  private double triggerLevel;
  private volatile static boolean isEvtEnabled = false;
  private boolean isCalibrating = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public CompassHTSensor(SensorPort port)
  {
    super(port, SharedConstants.LOWSPEED_9V);
    cst = new CompassSensorThread();
    cs = new lejos.nxt.addon.CompassHTSensor(sp);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public CompassHTSensor()
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
    if (cst != null)
      cst.stopThread();
  }

  /**
   * Registers the given compass listener for the given trigger level.
   * If the touch thread is not yet started, start it now.
   * If the trigger level is outside the interval 0..<360, returns immediately.
   * @param compassListener the CompassListener to become registered.
   * @param triggerLevel the trigger level where the callback is triggered (0..<360)
   */
  public void addCompassListener(CompassListener compassListener, double triggerLevel)
  {
    if (triggerLevel < 0 || triggerLevel >= 360)
      return;
    this.compassListener = compassListener;
    this.triggerLevel = triggerLevel;
    if (!cst.isAlive())
    {
      cst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(SharedConstants.SENSOREVENTDELAY);
      isEvtEnabled = true;
    }
  }

   /**
   * Registers the given compass listener with default trigger level 180.
   * @param compassListener the CompassListener to become registered.
   */
  public void addCompassListener(CompassListener compassListener)
  {
    addCompassListener(compassListener, 180);
  }

  /**
   * Sets a new trigger level and returns the previous one.
   * @param triggerLevel the new trigger level
   * @return the previous trigger level
   */
  public double setTriggerLevel(double triggerLevel)
  {
    double oldLevel = this.triggerLevel;
    this.triggerLevel = triggerLevel;
    return oldLevel;
  }

  private double getLevel()
  {
   if (robot == null)
     return -1;
   return cs.getDegrees();
  }

  /**
   * Polls the sensor.
   * @return the current compass reading (0..<360 clockwise, 0 to north)
   */
  public double getDegrees()
  {
    checkConnect();
    return cs.getDegrees();
  }

  /**
   * Starts calibration process.
   * Needs at least 2 full rotations with at least 20 s per rotation
   * before stopCalibration() is called.
   * @see #stopCalibration()
   */
  public void startCalibration()
  {
    checkConnect();
    if (isCalibrating)
      return;
    isCalibrating = true;
    cs.startCalibration();
  }

  /**
   * Stops calibration process.
   * @see #startCalibration()
   */
  public void stopCalibration()
  {
    checkConnect();
    if (!isCalibrating)
      return;
    isCalibrating = false;
    cs.stopCalibration();
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.CompassSensor.
   * @return the reference of the CompassSensor
   */
  public lejos.nxt.addon.CompassHTSensor getLejosSensor()
  {
    return cs;
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("CompassSensor (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
