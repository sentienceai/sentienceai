// SuperProSensor.java

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
 * Class that represents a SuperPro prototype sensor
 * (HiTechnic NXT SuperPro Prototype Board).<br><br>
 * The following I2C register layout is assumed:<br>
 * Device address 0x10<br>
 * 0x00-0x07 Version number<br>
 * 0x08-0x0F Manufacturer<br>
 * 0x10-0x17 Sensor type<br>
 * 0x42 Analog input channel 0, pin A0, upper 8bits<br>
 * 0x43 Analog input channel 0, pin A0, lower 2bits<br>
 * 0x44 Analog input channel 1, pin A1, upper 8bits<br>
 * 0x45 Analog input channel 1, pin A1, lower 2bits<br>
 * 0x46 Analog input channel 2, pin A2, upper 8bits<br>
 * 0x47 Analog input channel 2, pin A2, lower 2bits<br>
 * 0x48 Analog input channel 3, pin A3, upper 8bits<br>
 * 0x49 Analog input channel 3, pin A3, lower 2bits<br>
 * 0x4C Digital input channels, bits 0..7<br>
 * 0x4D Digitag output channels, bits 0..7<br>
 * 0x4E Digital control, bits 0..7, low: input (default), high: output<br>
 * 0x50 Strobe output, bits 0..3<br>
 * 0x51 LED control, bits 0..3<br>
 * 0x52 Analog output channel 0 mode<br>
 * 0x53 Analog output channel 0 frequency, upper 5bits<br>
 * 0x54 Analog output channel 0 frequency, lower 8bits<br>
 * 0x55 Analog output channel 0 voltage, upper 8bits (exotic!)<br>
 * 0x56 Analog output channel 0 voltage, lower 2bits (exotic!)<br>
 * 0x57 Analog output channel 1 mode<br>
 * 0x58 Analog output channel 1 frequency, upper 5bits<br>
 * 0x59 Analog output channel 1 frequency, lower 8bits<br>
 * 0x5A Analog output channel 1 voltage, upper 8bits (exotic!)<br>
 * 0x5B Analog output channel 1 voltage, lower 2bits (exotic!)<br>
 *
 * Analog inputs in range 0..3.3V, 10 bit (0..1023)<br>
 * Digital inputs/outputs 0/3.3V, pin B0..B7, max. 12mA per output (high or low)<br>
 * Strobe outputs 0..3 general purpose digital output<br>
 * Strobe output WR: rectangle pulse of approx. 0.5 us length (3V approx.) at every
 * write action to digital out (B0..B7 ports)<br>
 * Strobe output RD: rectangle pulse of approx. 0.5 us length (3V approx.) at every
 * read action of digital in (B0..B7 ports)<br>
 * Analog output frequency 1..8191 Hz, analog output voltage 0..3.3V (0..1023)<br>
 * Analog output modes:<br>
 * 0: DC<br>
 * 1: sine wave<br>
 * 2: square wave<br>
 * 3: positive sawtooth<br>
 * 4: negative sawtooth<br>
 * 5: triangle wave<br>
 * 6: pulse width modulation<br>
 */


public class SuperProSensor extends I2CSensor
{
  /**
   * Enumeration for the on board LEDs.
   */
  public enum LED {red, blue};

  // -------------- Inner class SuperProSensorThread ------------
  private class SuperProSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private SuperProSensorThread()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("PrTh created");
    }

    public void run()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("PrTh started");

      int[] ain = new int[4];
      int[] din = new int[8];
      for (int i = 0; i < 4; i++)
        ain[i] = 0;
      for (int i = 0; i < 8; i++)
        din[i] = 0;
      int[] ain_new = new int[4];
      int[] din_new = new int[8];

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (prototypeListener != null)
        {
          Tools.delay(SharedConstants.PROTOTYPEPOLLDELAY);
          boolean rc = readSensor(ain_new, din_new);
          if (!rc)  // Error
            continue;
          boolean isAnalogChanged = false;
          for (int i = 0; i < 4; i++)
          {
            if (ain_new[i] == ain[i])
              ain_new[i] = -1;
            else
            {
              ain[i] = ain_new[i];
              isAnalogChanged = true;
            }
          }
          boolean isDigitalChanged = false;
          for (int i = 0; i < 8; i++)
          {
            if (din_new[i] == din[i])
              din_new[i] = -1;
            else
            {
              din[i] = din_new[i];
              isDigitalChanged = true;
            }
          }
          if (isAnalogChanged)
          {
            if (inAnalogCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'ain'(rej)");
            }
            else
            {
              inAnalogCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'ain'(" + getPortLabel() + ")");
              prototypeListener.ainChanged(getPort(), ain_new);
              inAnalogCallback = false;
            }
          }
          if (isDigitalChanged)
          {
            if (inDigitalCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'din'(rej)");
            }
            else
            {
              inDigitalCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'din'(" + getPortLabel() + ")");
              prototypeListener.dinChanged(getPort(), din_new);
              inDigitalCallback = false;
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
          DebugConsole.show("PrTh stop failed");
        else
          DebugConsole.show("PrTh stop ok");
    }
  }
  // -------------- End of inner classes -----------------------

  /**
   * Constant for the red onboard LED.
   */
  public static final LED redLED = LED.red;

  /**
   * Constant for the blue onboard LED.
   */
  public static final LED blueLED = LED.blue;
  //
  private final int REGISTERBASE = 0x42;
  private final int DIGITALIN    = 0x4C;
  private final int DIGITALOUT   = 0x4D;
  private final int IOCONTROL    = 0x4E;
  private final int STROBEOUT    = 0x50;
  private final int LEDCONTROL   = 0x51;
  private final int A0BASE       = 0x52;
  private final int A1BASE       = 0x57;
  private int control = 0; // Bits 0..7, all inputs
  private int ledControl = 0;  // both LEDs off
  private volatile static boolean inAnalogCallback = false;
  private volatile static boolean inDigitalCallback = false;
  private PrototypeListener prototypeListener = null;
  private SuperProSensorThread pst;
  private int pollDelay;
  private volatile static boolean isEvtEnabled = false;

  /**
   * Creates a sensor instance connected to the given port.
   * (default: all digital channels as input)
   * @param port the port where the sensor is plugged-in
   */
  public SuperProSensor(SensorPort port)
  {
    super(port, LOWSPEED_9V, 0x10);
    pst = new SuperProSensorThread();
  }

  /**
   * Creates a sensor instance connected to port S1.
   * (default: all digital channels as input)
   */
  public SuperProSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: SuperPro.init() called (Port: " +
        getPortLabel() + ")");
    super.init();
    sendData(IOCONTROL, (byte)0); // All inputs
    ledOff();
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: SuperPro.cleanup() called (Port: " +
        getPortLabel() + ")");
    isEvtEnabled = false;  // Disable events
    if (pst != null)
      pst.stopThread();
    sendData(IOCONTROL, (byte)0); // All inputs
  }

  /**
   * Sets the direction of the 8 digital input/output channels.
   * @param ioControl the direction of each channel. 0: input, 1: output
   * (default: all digital channels as input)
   */
  public void setDIO(int[] ioControl)
  {
    checkConnect();
    if (ioControl.length != 8)
    {
      new ShowError("setDIO len not 8");
      return;
    }
    for (int i = 0; i < ioControl.length; i++)
    {
      if (ioControl[i] < 0  || ioControl[i] > 1)
      {
        new ShowError("setDIO not 0 or 1");
        return;
      }
    }
    synchronized(this)
    {
      control = 0;
      int bitValue = 1;
      for (int i = 0; i < ioControl.length; i++)
      {
        control = control + bitValue * ioControl[i];
        bitValue *= 2;
      }
      sendData(IOCONTROL, (byte)control); // Set digital IO control
    }
  }

  /**
   * Reads the analog input of the sensor. Analog input data 0..1023
   * (corresponding to 0..3.3 V) are returned in the given ain.
   * @param ain an integer array of length 4 where to get the analog values
   */
  public void readAnalog(int[] ain)
  {
    int[] din = new int[8];
    read(ain, din);
  }

  /**
   * Reads the sensor. Digital input data 0 (corresponds to low state, 0 V) or 1
   * (corresponds to high state, 3.3 V) is returned in the given din.
   * For digital channels defined as outputs -1 is returned.
   * @param din an integer array of length 8 where to get the values
   */
  public void readDigital(int[] din)
  {
    int[] ain = new int[4];
    read(ain, din);
  }

  /**
   * Reads the sensor. Analog input data 0..1023 (corresponding to 0..3.3 V)
   * are returned in the given ain, digital input data (0, 1) in the given din.
   * For digital channels defined as outputs -1 is returned.
   * @param ain an integer array of length 4 where to get the analog values
   * @param din an integer array of length 8 where to get the digital values
   */
  public synchronized void read(int[] ain, int[] din)
  {
    checkConnect();
    if (ain.length != 4)
    {
      new ShowError("read len not 4");
      return;
    }
    if (din.length != 8)
    {
      new ShowError("read len not 8");
      return;
    }
    byte[] in = new byte[11];
    // Analog 0 (2 bytes)
    // Analog 1 (2 bytes)
    // Analog 2 (2 bytes)
    // Analog 3 (2 bytes)
    // not used (Analog 4 (2 bytes) for former Prototype sensor)
    // Digital (1 byte)

    getData(REGISTERBASE, in, 11);
    // Convert to int
    int[] data = new int[11];
    for (int i = 0; i < 11; i++)
      data[i] = in[i] & 0xFF;

    // Analog inputs: base -> upper 8 bits, base+1 -> lower 2 bits
    for (int i = 0; i < 4; i++)
      ain[i] = 4 * data[2*i] + data[2*i+1];

    // Digital inputs
    int bitValue = 1;
    for (int i = 0; i < 8; i++)
    {
      if ((control & bitValue) != 0)
        din[i] = -1;
      else
        din[i] = (data[10] & bitValue) / bitValue;
      bitValue *= 2;
    }

    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: read() values:\n" +
                        getAnalogValues(ain) + getDigitalValues(din));
  }

  private String getAnalogValues(int[] ain)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < 4; i++)
      sb.append("a[" + i + "]: " + ain[i] + "\n");
    return sb.toString();
  }

  private String getDigitalValues(int[] din)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < 8; i++)
      sb.append("d[" + i + "]: " + din[i] + "\n");
    return sb.toString();
  }

  /**
   * Writes the lower byte of the given value to the digital output channels.
   * Only channels set as output will be affected. Input channel bits are ignored.
   * @param value the lower 8 bits are written to the digital output channel.
   */
  public synchronized void write(int value)
  {
    checkConnect();
    sendData(DIGITALOUT, (byte)value);
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: write():\n" + value);
  }

  /**
   * Writes the given bit state (low/high) to the digital output channels.
   * Only channels set as output will be affected. Input channel bits are ignored.
   * @param dout an integer array of length 8 that holds the bit state: 0->low, 1->high
   */
  public synchronized void write(int[] dout)
  {
    checkConnect();
    if (dout.length != 8)
    {
      new ShowError("write len not 8");
      return;
    }
    for (int i = 0; i < 8; i++)
    {
      if (dout[i] < 0 || dout[i] > 1)
      {
        new ShowError("write not 0,1");
        return;
      }
    }
    int bitValue = 1;
    int value = 0;
    for (int i = 0; i < 8; i++)
    {
      if (dout[i] == 1)
        value = value + dout[i] * bitValue;
      bitValue *= 2;
    }
    sendData(DIGITALOUT, (byte)value);
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: write():\n" + value);
  }


/**
   * Writes the lower half byte (lower 4 bits) of the given value to the strobe output channels.
   * @param value the lower 4 bits are written to the strobe output channel.
   */
  public synchronized void writeStrobe(int value)
  {
    checkConnect();
    sendData(STROBEOUT, (byte)value);
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: writeStrobe():\n" + value);
  }

  /**
   * Writes the given bit state (low/high) to the strobe output channels.
   * @param sout an integer array of length 4 that holds the bit state: 0->low, 1->high
   */
  public synchronized void writeStrobe(int[] sout)
  {
    checkConnect();
    if (sout.length != 4)
    {
      new ShowError("writeStrobe len not 4");
      return;
    }
    for (int i = 0; i < 4; i++)
    {
      if (sout[i] < 0 || sout[i] > 1)
      {
        new ShowError("writeStrobe not 0,1");
        return;
      }
    }
    int bitValue = 1;
    int value = 0;
    for (int i = 0; i < 4; i++)
    {
      if (sout[i] == 1)
        value = value + sout[i] * bitValue;
      bitValue *= 2;
    }
    sendData(STROBEOUT, (byte)value);
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: writeStrobe():\n" + value);
  }

  /**
   * Turn on both onboard LEDs (red and blue).
   * When the program terminates, the current state of the LEDs is maintained.
   */
  public void ledOn()
  {
    ledControl = 3;
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: turnOn(red)");
    sendData(LEDCONTROL, (byte)ledControl);
  }

  /**
   * Turn the given onboard LED on.
   * When the program terminates, the current state of the LEDs is maintained.
   * @param led a value of enumeration LED
   */
  public synchronized void ledOn(LED led)
  {
    checkConnect();
    switch (led)
    {
      case red:
        ledControl = ledControl | 0x1;
        if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
           DebugConsole.show("DEBUG: turnOn(red)");
        break;
      case blue:
        ledControl = ledControl | 0x2;
        if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
          DebugConsole.show("DEBUG: turnOn(blue)");
        break;
    }
    sendData(LEDCONTROL, (byte)ledControl);
  }

  /**
   * Turn off both onboard LEDs (red and blue).
   * When the program terminates, the current state of the LEDs is maintained.
   */
  public void ledOff()
  {
    ledControl = 0;
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: turnOn(red)");
    sendData(LEDCONTROL, (byte)ledControl);
  }

  /**
   * Turn the given onboard LED off.
   * When the program terminates, the current state of the LEDs is maintained.
   * @param led a value of enumeration LED
   */
  public synchronized void ledOff(LED led)
  {
    checkConnect();
    switch (led)
    {
      case red:
        ledControl = ledControl & ~0x1;
        if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
           DebugConsole.show("DEBUG: turnOff(red)");
        break;
      case blue:
        ledControl = ledControl & ~0x2;
        if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
          DebugConsole.show("DEBUG: turnOff(blue)");
        break;
    }
    sendData(LEDCONTROL, (byte)ledControl);
  }

  private boolean readSensor(int[] ain, int[] din)
  {
    if (robot == null)
      return false;
    read(ain, din);
    return true;
  }

  /**
   * Enables the digital output. The following modes are available:<br>
   * 1: DC voltage level
   * 2: Sine wave
   * 3: Square wave
   * 4: Positive going sawtooth
   * 5: Negative going sawtooth
   * 5: Triangle wave
   * 6: PWM voltage
   * Once the output is enabled, it remains active even when the program
   * terminates.
   * @param port 0: for digital output O0, 1: for digital output O1
   * @param mode the mode (1..5)
   * @param frequency the frequency in Hertz (1..8191)
   * @param voltage the peak-to-peak voltage (0..1021 corresponding to 0..3.3V)
   */
  public synchronized void setAnalogOut(int port, int mode, int frequency, int voltage)
  {
    checkConnect();
    byte[] buf = new byte[5];
    buf[0] = (byte)mode;
    buf[1] = (byte)(frequency >> 8); // freq in 13 bits; msb
    buf[2] = (byte)frequency;  // lsb
    buf[3] = (byte)(voltage >> 2);  // voltage in 10 bits; upper 8 bits
    buf[4] = (byte)(voltage & 0x00000002); // lower 2 bits
    if (port == 0)
      sendData(A0BASE, buf, 5);
    if (port == 1)
      sendData(A1BASE, buf, 5);
  }

   /**
   * Registers the given prototype listener.
   * @param prototypeListener the SuperProListener to become registered.
   */
  public void addPrototypeListener(PrototypeListener prototypeListener)
  {
    this.prototypeListener = prototypeListener;
    if (!pst.isAlive())
    {
      pst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(SharedConstants.SENSOREVENTDELAY);
      isEvtEnabled = true;
    }
  }

  protected void startSuperProThread()
  {
    pst.start();
  }

  protected void stopSuperProThread()
  {
    if (pst.isAlive())
      pst.stopThread();
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("SuperProSensor (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
