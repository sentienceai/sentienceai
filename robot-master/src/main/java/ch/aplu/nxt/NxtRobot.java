// NxtRobot.java

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

import java.util.*;
import lejos.nxt.*;

/**
 * Class that represents a NXT robot brick. Parts (e.g. motors, sensors) may
 * be assembled into the robot to make it doing the desired job.
 */
public class NxtRobot implements SharedConstants
{

  private Vector<Part> parts = new Vector<Part>();

  private static int debugLevel;

  /**
   * Creates an instance of NxtRobot and and delays execution until the user presses the
   * ENTER button.
   * @param waitStart if true, the execution is stopped until the ENTER button is hit.
   */
  public NxtRobot(boolean waitStart)
  {
    if (waitStart)
    {
      LCD.clear();
      LCD.drawString("Press ENTER", 0, 0);
      Tools.waitEnter();
      LCD.drawString("Program started", 0, 0);
    }
    debugLevel = SharedConstants.DEBUGLEVEL;
    if (debugLevel > DEBUG_LEVEL_OFF)
      DebugConsole.show("DebugLevel: " + debugLevel);
  }

  /**
   * Creates an instance of NxtRobot continues execution normally.
   */
  public NxtRobot()
  {
    this(false);
  }


  protected static int getDebugLevel()
  {
    return debugLevel;
  }

  /**
   * Assembles the given part into the robot.
   * If already connected, initialize the part.
   * @param part the part to assemble
   */
  public void addPart(Part part)
  {
    if (part instanceof Sensor)
    {
      for (int i = 0; i < parts.size(); i++)
      {
        if (parts.elementAt(i) instanceof Sensor)
        {
          if (((Sensor)parts.elementAt(i)).getPortId() == ((Sensor)part).getPortId())
            new ShowError("Port " + ((Sensor)part).getPortLabel() + " conflict");
        }
      }
    }

    if (part instanceof Motor)
    {
      for (int i = 0; i < parts.size(); i++)
      {
        if (parts.elementAt(i) instanceof Motor)
        {
          if (((Motor)parts.elementAt(i)).getPortId() == ((Motor)part).getPortId())
            new ShowError("Port " + ((Motor)part).getPortLabel() + " conflict");
        }
      }
    }

    part.setRobot(this);
    parts.addElement(part);
    part.init();
  }

  /**
   * Returns the battery level.
   * @return voltage (in Volt)
   */
  public double getBatteryLevel()
  {
    return Battery.getVoltage();
  }

  /**
   * Returns library version information.
   * @return library version
   */
  public static String getVersion()
  {
    return VERSION;
  }

  /**
   * Calls cleanup() of all sensors, stops any running motor and terminates the running program.
   */
  public void exit()
  {
    // Clean up sensors
    for (int i = 0; i < parts.size(); i++)
      ((Part)(parts.elementAt(i))).cleanup();
    playDisconnectMelody();
    if (debugLevel > DEBUG_LEVEL_OFF)
    {
      DebugConsole.show("DEBUG wait...");
      Tools.delay(5000);
    }
  }

  /**
   * Plays a tone of given frequency (in Hz) for the given duration (in ms).
   * The method returns immediately. In order to play a melody, the program
   * should be delayed (using Tools.delay()) for an appropriate duration.
   * @param frequency the frequency in Hertz
   * @param duration the duration in milliseconds
   */

  public void playTone(int frequency, int duration)
  {
    Sound.playTone(frequency, duration);
  }

  protected synchronized void playConnectMelody()
  {
    Tools.delay(1000);
    Sound.playTone(600, 100);
    Tools.delay(100);
    Sound.playTone(900, 100);
    Tools.delay(100);
    Sound.playTone(750, 200);
    Tools.delay(200);
  }

  protected synchronized void playDisconnectMelody()
  {
    Tools.delay(1000);
    Sound.playTone(900, 100);
    Tools.delay(100);
    Sound.playTone(750, 100);
    Tools.delay(100);
    Sound.playTone(600, 200);
    Tools.delay(200);
  }
}
