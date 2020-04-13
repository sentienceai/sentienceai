// SoundSensor.java

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
 * Class that represents a sound sensor.
 */
public class SoundSensor extends Sensor implements SharedConstants
{
  private interface SensorState // Simulate enum
  {
    int QUIET = 0;
    int LOUD = 1;
  }

  // -------------- Inner class SoundSensorThread ------------
  private class SoundSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private SoundSensorThread()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("SsTh created");
    }

    public void run()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("SsTh started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (soundListener != null)
        {
          Tools.delay(SharedConstants.SOUNDPOLLDELAY);
          int level = getLevel();
          if (state == SensorState.QUIET && level > triggerLevel)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'loud'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'loud'(" + getPortLabel() + ")");
              soundListener.loud(getPort(), level);
              state = SensorState.LOUD;
              inCallback = false;
            }
          }
          if (state == SensorState.LOUD && level <= triggerLevel)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'quiet'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'quiet'(" + getPortLabel() + ")");
              soundListener.quiet(getPort(), level);
              state = SensorState.QUIET;
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
          DebugConsole.show("SsTh stop failed");
        else
          DebugConsole.show("SsTh stop ok");
    }
  }
  // -------------- End of inner classes -----------------------

  private lejos.nxt.SoundSensor ss;
  private lejos.nxt.SensorPort sp;
  private SoundListener soundListener = null;
  private int state = SensorState.QUIET;
  private int triggerLevel;
  private volatile static boolean inCallback = false;
  private SoundSensorThread sst;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public SoundSensor(SensorPort port)
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
    sst = new SoundSensorThread();
    ss = new lejos.nxt.SoundSensor(sp, false);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public SoundSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("ss.init()");

  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("ss.cleanup()");
    isEvtEnabled = false;  // Disable events
    if (sst != null)
      sst.stopThread();
  }

  /**
   * Registers the given sound listener for the given trigger level.
   * @param soundListener the SoundListener to become registered.
   * @param triggerLevel the trigger level where the callback is triggered
   */
  public void addSoundListener(SoundListener soundListener, int triggerLevel)
  {
    this.soundListener = soundListener;
    this.triggerLevel = triggerLevel;
    if (!sst.isAlive())
    {
      sst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(SharedConstants.SENSOREVENTDELAY);
      isEvtEnabled = true;
    }
 }

  /**
   * Registers the given sound listener with default trigger level 50.
   * @param soundListener the SoundListener to become registered.
   */
  public void addSoundListener(SoundListener soundListener)
  {
    addSoundListener(soundListener, 50);
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
    return ss.readValue();
  }

  /**
   * Polls the sensor.
   * @return the current value the sensor reported: 0 (quiet) .. 100 (loud)
   */
  public int getValue()
  {
    checkConnect();
    return ss.readValue();
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.SoundSensor.
   * @return the reference of the SoundSensor
   */
  public lejos.nxt.SoundSensor getLejosSensor()
  {
    return ss;
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
      new ShowError("SoundSensor (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }

}
