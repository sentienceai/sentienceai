// ColorSensor.java

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

/**
 * Class that represents a color light sensor (Lego Color Sensor)
 */
public class ColorSensor extends Sensor implements SharedConstants
{
  private lejos.nxt.ColorSensor cs;
  private lejos.nxt.SensorPort sp;

  /**
   * Creates a sensor instance connected to the given port.
   * Uses the lejos.nxt.ColorLightSensor in type TYPE_COLORFULL
   * @param port the port where the sensor is plugged-in
   */
  public ColorSensor(SensorPort port)
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
    cs = new
      lejos.nxt.ColorSensor(sp, lejos.nxt.ColorSensor.TYPE_COLORFULL);
  }

  /**
   * Creates a sensor instance connected to port S1.
   * Uses the lejos.nxt.ColorSensor in type TYPE_COLORFULL
   */
  public ColorSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("cs.init()");
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("cs.cleanup()");
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.ColorSensor.
   * @return the reference of the ColorSensor
   */
  public lejos.nxt.ColorSensor getLejosSensor()
  {
    return cs;
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.SensorPort.
   * @return the reference of the SensorPort
   */
  public lejos.nxt.SensorPort getLejosPort()
  {
    return sp;
  }

  /**
   * Checks if given color lies within given color cube.
   * @param color a lejos.nxt.robotics.Color reference
   * @param colorCube an integer array of 6 boundary values in the order:
   * red_min, red_max, green_min, green_max, blue_min, blue_max
   * @return true, if the color is within the color cube; otherwise false
   */
  public boolean inColorCube(lejos.robotics.Color color, int[] colorCube)
  {
    if (color.getRed() >= colorCube[0] && color.getRed() <= colorCube[1]
    && color.getGreen() >= colorCube[2] && color.getGreen() <= colorCube[3]
    && color.getBlue() >= colorCube[4] && color.getBlue() <= colorCube[5])
      return true;
    return false;
  }

  /**
     Returns a lejos.robotics.Color reference to the the calibrated color reading.
     @return the calibrated color reading
   */
  public lejos.robotics.Color getColor()
  {
    checkConnect();
    return cs.getColor();
  }

  /**
   * Returns the intensity of the detected light.
   * @return light intensity (0..1023)
   */
  public int getLightValue()
  {
    checkConnect();
    return cs.getNormalizedLightValue();
  }

  /**
   * Turns the red, green or blue LED on or off.
   * @param color one of the color enumerations Color.RED, Color.GREEN, Color.BLUE, Color.NONE (turn off)
   */
  public void setFloodlight(int color)
  {
    checkConnect();
    cs.setFloodlight(color);
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("ColorLightSensor (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }
}