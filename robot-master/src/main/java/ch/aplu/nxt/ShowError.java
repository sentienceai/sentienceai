// ShowError.java, leJOS version
// Platform (Java SE, ME, leJOS) dependent code
// Should be visible in package only. Not included in Javadoc

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

import lejos.nxt.*;

/**
 * Class to display error messages.
 */
public class ShowError
{
  // Shows a single line message (not longer than 16 characters) and loops forever.
  public ShowError(String msg)
  {
    LCD.clear();
    Tools.delay(200);
    Sound.playTone(2000, 100);
    Tools.delay(200);
    Sound.playTone(1000, 100);
    Tools.delay(200);
    System.out.println(msg);
    while (true) {}
  }
}
