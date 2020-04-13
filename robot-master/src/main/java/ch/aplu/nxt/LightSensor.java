// LightSensor.java

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
 * Class that represents a light sensor.
 */
public class LightSensor extends Sensor implements SharedConstants
{
  private interface SensorState // Simulate enum for J2ME compatibility
  {
    int DARK = 0;
    int BRIGHT = 1;
  }

  // -------------- Inner class LightSensorThread ------------
  private class LightSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private LightSensorThread()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("LsTh created");
    }

    public void run()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("LsTh started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (lightListener != null)
        {
          Tools.delay(SharedConstants.LIGHTPOLLDELAY);
          int level = getLevel();
          if (state == SensorState.DARK && level > triggerLevel)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'bright'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'bright'(" + getPortLabel() + ")");
              lightListener.bright(getPort(), level);
              state = SensorState.BRIGHT;
              inCallback = false;
            }
          }
          if (state == SensorState.BRIGHT && level <= triggerLevel)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'dark'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'dark'(" + getPortLabel() + ")");
              lightListener.dark(getPort(), level);
              state = SensorState.DARK;
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
          DebugConsole.show("LsTh stop failed");
        else
          DebugConsole.show("LsTh stop ok");
    }
  }
  // -------------- End of inner classes -----------------------

  private lejos.nxt.LightSensor ls;
  private lejos.nxt.SensorPort sp;
  private LightListener lightListener = null;
  private int state = SensorState.DARK;
  private int triggerLevel;
  private volatile static boolean inCallback = false;
  private LightSensorThread lst;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public LightSensor(SensorPort port)
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
    lst = new LightSensorThread();
    ls = new lejos.nxt.LightSensor(sp, false);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
   public LightSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("ls.init()");
    activate(false);
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("ls.cleanup()");
    activate(false);
    isEvtEnabled = false;  // Disable events
    if (lst != null)
      lst.stopThread();
  }

  /**
   * Registers the given light listener for the given trigger level.
   * @param lightListener the LightListener to become registered.
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addLightListener(LightListener lightListener, int triggerLevel)
  {
    this.lightListener = lightListener;
    this.triggerLevel = triggerLevel;
    if (!lst.isAlive())
    {
      lst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(SharedConstants.SENSOREVENTDELAY);
      isEvtEnabled = true;
    }
  }

  /**
   * Registers the given light listener with default trigger level 500.
   * @param lightListener the LightListener to become registered.
   */
  public void addLightListener(LightListener lightListener)
  {
    addLightListener(lightListener, 500);
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

  /**
   * Turns on/off the LED used for reflecting light back into the sensor.
   * @param enable if true, turn the LED on, otherwise turn it off
   */
  public void activate(boolean enable)
  {
    checkConnect();
    ls.setFloodlight(enable);
  }

  private int getLevel()
  {
    if (robot == null)
      return -1;
    return ls.readNormalizedValue();
  }

  /**
   * Polls the sensor.
   * @return the current value the sensor reported: 0 (dark) .. 1023 (bright)
   */
  public int getValue()
  {
    checkConnect();
    return ls.readNormalizedValue();
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.LightSensor.
   * @return the reference of the LightSensor
   */
  public lejos.nxt.LightSensor getLejosSensor()
  {
    return ls;
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
      new ShowError("LightSensor (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }


}