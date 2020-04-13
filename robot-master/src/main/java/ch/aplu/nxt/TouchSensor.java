// TouchSensor.java

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
 * Class that represents a touch sensor.
 */
public class TouchSensor extends Sensor implements SharedConstants
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int RELEASED = 0;
    int PRESSED = 1;
  }

// -------------- Inner class TouchSensorThread ------------
  private class TouchSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private TouchSensorThread()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("TsTh created");
    }

    public void run()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("TsThread started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (touchListener != null)
        {
          Tools.delay(SharedConstants.TOUCHPOLLDELAY);
          boolean isActuated = isActuated();
          if (state == SensorState.RELEASED && isActuated)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'pressed'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'pressed'(" + getPortLabel() + ")");
              touchListener.pressed(getPort());
              state = SensorState.PRESSED;
              inCallback = false;
            }
          }
          if (state == SensorState.PRESSED && !isActuated)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'rleasd'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'rleasd'(" + getPortLabel() + ")");
              touchListener.released(getPort());
              state = SensorState.RELEASED;
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
          DebugConsole.show("TsTh stop failed");
        else
          DebugConsole.show("TsTh stop ok");
    }
  }
// -------------- End of inner classes -----------------------

  private lejos.nxt.TouchSensor ts;
  private lejos.nxt.SensorPort sp;
  private TouchListener touchListener = null;
  private int state = SensorState.RELEASED;
  private volatile static boolean inCallback = false;
  private TouchSensorThread tst;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public TouchSensor(SensorPort port)
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
    tst = new TouchSensorThread();
    ts = new lejos.nxt.TouchSensor(sp);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
   public TouchSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("ts.init()");
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("ts.cleanup()");
    isEvtEnabled = false;  // Disable events
    if (tst != null)
      tst.stopThread();
 }

  /**
   * Registers the given touch listener.
   * If the touch thread is not yet started, start it now.
   * @param touchListener the TouchListener to become registered.
   */
  public void addTouchListener(TouchListener touchListener)
  {
    this.touchListener = touchListener;
    if (!tst.isAlive())
    {
      tst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(SharedConstants.SENSOREVENTDELAY);
      isEvtEnabled = true;
     }
   }

  protected boolean isActuated()
  {
    if (robot == null)
      return false;
    return ts.isPressed();
  }

  /**
   * Polls the sensor.
   * @return true, if actuated, otherwise false
   */
  public boolean isPressed()
  {
    checkConnect();
    return ts.isPressed();
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.TouchSensor.
   * @return the reference of the TouchSensor
   */
  public lejos.nxt.TouchSensor getLejosSensor()
  {
    return ts;
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
      new ShowError("TouchSensor (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
