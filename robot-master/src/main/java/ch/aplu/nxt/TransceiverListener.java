// TransceiverListener.java

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
 * Declarations of notification methods called from the class Transceiver.
 */
public interface TransceiverListener
{

  /**
   * Called when incoming data is received.
   * @param state an integer state number sent with Transceiver.send()
   * @param value an integer state label sent with Transceiver.send()
   */
  public void received(int state, int value);

  /**
   * Called when the connection is established/lost.
   * @param connected true, if the connection is established; false, if the connection is lost
   */
  public void notifyConnection(boolean connected);


  /**
   * Called when the client could not connect and a listening server is started.
   */
  public void isListening();
}