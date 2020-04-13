// CQueueData.java

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
 * Structured data for the circular queue. This implementation groups a
 * pair of integer to be used to exchange data in the Transceiver class.
 * @see CQueue
 * @see Transceiver
 */
public class CQueueData
{
  /**
   * First member of the integer pair.
   */
  public int state;
  /**
   * Second member of the integer pair.
   */
  public int label;

  /**
   * Creates a class instance with given integer pair.
   * @param state the first member of the pair
   * @param label the second member of the pair
   */
  public CQueueData(int state, int label)
  {
    this.state = state;
    this.label = label;
  }

  /**
   * Creates a clone of the given data.
   * @param fd the CQueueData instance to be cloned.
   */
  public CQueueData(CQueueData fd)
  {
    state = fd.state;
    label = fd.label;
  }
}

