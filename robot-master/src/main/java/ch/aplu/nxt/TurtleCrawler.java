  // TurtleCrawler.java

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

/**
 * Implementation of the basic Logo turtle movements for a NXT crawler-mounted vehicle.
 */
package ch.aplu.nxt;


public class TurtleCrawler extends TurtleRobot
{

  /**
   * Creates TurtleCrawler instance and delays execution until the user
   * presses the ENTER button.
   * @param waitStart if true, the execution is stopped until the ENTER button is hit.
   */
  public TurtleCrawler(boolean waitStart)
  {
    super(waitStart);
    stepFactor = SharedConstants.CRAWLERSTEPFACTOR;
    rotationFactor = SharedConstants.CRAWLERROTATIONFACTOR;
    turtleSpeed = SharedConstants.CRAWLERSPEED;
    rotationSpeed = SharedConstants.CRAWLERROTATIONSPEED;
    gear.setSpeed(turtleSpeed);
  }

  /**
   * Creates a TurtleCrawler instance.
   */
  public TurtleCrawler()
  {
    this(false);
  }
}
