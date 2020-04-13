// AccelerometerListener.java

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
 * Class with declarations of callback methods for the prototype sensor.
 */
public interface AccelerometerListener
{
   /**
    * Called when at least one of the x,y,z values changes.
    * @param port the port where the sensor is plugged in
    * @param values the value of the x-, y-, z-axis acceleration: -512..511
    */
   public void accelerationChanged(SensorPort port, int[] values);
}
