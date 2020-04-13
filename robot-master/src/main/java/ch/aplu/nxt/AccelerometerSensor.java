// AccelerometerSensor.java

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
 * Class that represents a accelerometer sensor (HiTechnic).
 * The axis are defined as follows:<br>
 * Put sensor with white cover upside, cable connector to the left<br>
 * x-axis from right to left<br>
 * y-axis from back to front<br>
 * z-axis from top to bottom<br><br>
 *
 * The following register layout is assumed:<br>
 * 0x00-0x07 Version number<br>
 * 0x08-0x0F Manufacturer<br>
 * 0x10-0x17 Sensor type<br>
 * 0x42 X axis, upper 8bits<br>
 * 0x43 Y axis, upper 8bits<br>
 * 0x44 Z axis, upper 8bits<br>
 * 0x45 X axis, lower 2bits<br>
 * 0x46 Y axis, lower 2bits<br>
 * 0x47 Z axis, lower 2bits<br><br>
 * Values are 10 bit in two's complement
 */
public class AccelerometerSensor extends I2CSensor
{

  // -------------- Inner class AccelerometerSensorThread ------------
  private class AccelerometerSensorThread extends Thread
  {
    private volatile boolean isRunning = false;

    private AccelerometerSensorThread()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("AcTh created");
    }

    public void run()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("AcTh started");

      int[] data = new int[3];
      int[] data_old = new int[3];
      for (int i = 0; i < 3; i++)
        data_old[i] = 10000; // Never happens

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (accelerometerListener != null)
        {
          Tools.delay(SharedConstants.ACCELEROMETERPOLLDELAY);
          boolean rc = readSensor(data);
          if (!rc)  // Error
            continue;
          boolean isAccelerationChanged = false;
          for (int i = 0; i < 3; i++)
          {
            if (enabled[i] && data[i] != data_old[i])
              isAccelerationChanged = true;
            data_old[i] = data[i];
          }
          if (isAccelerationChanged)
          {
            if (inCallback)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'acc'(rej)");
            }
            else
            {
              inCallback = true;
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'acc'(" + getPortLabel() + ")");
              accelerometerListener.accelerationChanged(getPort(), data);
              inCallback = false;
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
          DebugConsole.show("AcTh stop failed");
        else
          DebugConsole.show("AcTh stop ok");
    }
  }
  // -------------- End of inner classes -----------------------

  private final int REGISTERBASE = 0x42;
  private volatile static boolean inCallback = false;
  private boolean isAccelerationChanged;
  private AccelerometerListener accelerometerListener = null;
  private AccelerometerSensorThread ast;
  private int pollDelay;
  private volatile static boolean isEvtEnabled = false;
  private boolean[] enabled = new boolean[3];

  /**
   * Creates a sensor instance connected to the given port.
   * @param port the port where the sensor is plugged-in
   */
  public AccelerometerSensor(SensorPort port)
  {
    super(port, LOWSPEED_9V);
    ast = new AccelerometerSensorThread();
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public AccelerometerSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Accelerometer.init() called (Port: " +
        getPortLabel() + ")");
    super.init();
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: Accelerometer.cleanup() called (Port: " +
        getPortLabel() + ")");
    isEvtEnabled = false;  // Disable events
    if (ast != null)
      ast.stopThread();
  }

  /**
   * Returns the current acceleration in x, y, z direction
   * (10 bit, two's complement, -512..511).
   */
  public synchronized int[] getAcceleration()
  {
    checkConnect();
    int[] values = new int[3];
    byte[] in = new byte[6];
    getData(REGISTERBASE, in, 6);

    // Convert to int
    int[] data = new int[6];
    for (int i = 0; i < 6; i++)
      data[i] = in[i] & 0xFF;

    // base -> upper 8 bits, base+3 -> lower 2 bits
    for (int i = 0; i < 3; i++)
    {
      values[i] = 4 * data[i] + data[3 + i];
      if (values[i] > 511)
        values[i] = values[i] - 1024;  // Two's complement
    }

    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: read() values:\n" +
                        getSensorValues(values));
    return values;
  }

  private String getSensorValues(int[] values)
  {
    StringBuffer sb = new StringBuffer();
    sb.append("ax = " + values[0] + "\n");
    sb.append("ay = " + values[1] + "\n");
    sb.append("az = " + values[2] + "\n");
    return sb.toString();
  }

  private boolean readSensor(int[] values)
  {
    if (robot == null)
      return false;
    int[] v = getAcceleration();
    for (int i = 0; i < 3; i++)
      values[i] = v[i];
    return true;
  }

  /**
   * Registers the given accelerometer listener.
   * @param listener the AccelerometerListener to become registered.
   * @param enableX if true, x-axis value change triggers an event
   * @param enableY if true, y-axis value change triggers an event
   * @param enableZ if true, z-axis value change triggers an event
   */
  public void addAccelerometerListener(AccelerometerListener listener,
     boolean enableX, boolean enableY, boolean enableZ)
  {
    accelerometerListener = listener;
    enabled[0] = enableX;
    enabled[1] = enableY;
    enabled[2] = enableZ;
    if (!ast.isAlive())
    {
      ast.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(SharedConstants.SENSOREVENTDELAY);
      isEvtEnabled = true;
    }
  }

  protected void startAccelerometerThread()
  {
    ast.start();
  }

  protected void stopAccelerometerThread()
  {
    if (ast.isAlive())
      ast.stopThread();
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("AccelerometerSensor (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
