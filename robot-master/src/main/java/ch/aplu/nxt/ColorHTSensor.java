// ColorHTSensor.java

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
 * Class that represents a color light sensor (HiTechnic Color Sensor)
 */
public class ColorHTSensor extends Sensor implements SharedConstants
{

  private lejos.nxt.addon.ColorHTSensor cs;
  private lejos.nxt.SensorPort sp;

  /**
   * Creates a sensor instance connected to the given port.
   * Uses the lejos.nxt.addon.ColorHTSensor
   * @param port the port where the sensor is plugged-in
   */
  public ColorHTSensor(SensorPort port)
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
      lejos.nxt.addon.ColorHTSensor(sp);
  }

  /**
   * Creates a sensor instance connected to port S1.
   * Uses the lejos.nxt.addon.ColorHTSensor
   */
  public ColorHTSensor()
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
   * Returns the reference of the the underlying lejos.nxt.addon.ColorHTSensor.
   * @return the reference of the ColorHTSensor
   */
  public lejos.nxt.addon.ColorHTSensor getLejosSensor()
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
   * Returns a Color reference to the the calibrated color reading.
   * The minimal poll period is about 500 ms.
   *  @return the calibrated color reading
   */
  public lejos.robotics.Color getColor()
  {
    checkConnect();
    return cs.getColor();
  }


  /**
   * Checks if given rgb values lies within given color cube.
   * @param rgb an integer array of 3 color value
   * @param colorCube an integer array of 6 boundary values in the order:
   * red_min, red_max, green_min, green_max, blue_min, blue_max
   * @return true, if the rgb values are within the color cube; otherwise false
   */
  public boolean inColorCube(int[] rgb, int[] colorCube)
  {
    boolean inDomain = true;
    for (int k = 0; k < 3; k++)
    {
      if (rgb[k] < colorCube[2*k] || rgb[k] > colorCube[2*k + 1])
      {
        inDomain = false;
        break;
      }
    }
    return inDomain;
  }

  /**
   * Returns the HiTechnic color index:<br>
   * 0: black<br>
   * 1: blue<br>
   * 2: blue<br>
   * 3: blue<br>
   * 4: green<br>
   * 5: yellow<br>
   * 6: yellow<br>
   * 7: red<br>
   * 8: red<br>
   * 9: red<br>
   * 10: pink<br>
   * 11: pink<br>
   * 12: light green<br>
   * 13: light yellow<br>
   * 14: light red<br>
   * 15: light red<br>
   * 16: light red<br>
   * 17: white<br>
   * @return HiTechnic color number
   */
  public int getColorID()
  {
    checkConnect();
    return cs.getColorID();
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("ColorLightSensorHi (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }

  /**
   * Puts the sensor into white balance calibration mode.
   * For best results the sensor should be pointed at a diffuse white
   * surface at a distance of approximately 15mm before calling this method.
   * After a fraction of a second the sensor lights will flash and the
   * calibration is done. When calibrated, the sensor keeps this information
   * in non-volatile memory (from lejos.nxt.addon.ColorSensor).
   * @return 0 if successful; otherwise -1
   */
  public int initWhiteBalance()
  {
    return cs.initWhiteBalance();
  }

 /**
  * Puts the sensor into black/ambient level calibration mode.
  * For best results the sensor should be pointed in a direction with no
  * obstacles for 50cm or so. This reading the sensor will use as a base
  * level for other readings. After a fraction of a second the sensor
  * lights will flash and the calibration is done. When calibrated,
  * the sensor keeps this information in non-volatile memory
  * (from lejos.nxt.addon.ColorSensor).
  * @return 0 if successful; otherwise -1
  */
  public int initBlackLevel()
  {
    return cs.initBlackLevel();
  }
}