// RFIDListener.java

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
 * Class with declaration of callback methods for the rfid sensor.
 */
public interface RFIDListener
{
  /**
   * Called when the RFID sensor detects a new transponder.
   * This happens when the transponder id has changed since the last detection
   * or it the same transponder was removed and detected again.
   * @param port the port where the sensor is plugged in
   * @param id the id of the detected transponder
   */
   public void detected(SensorPort port, long id);

}
