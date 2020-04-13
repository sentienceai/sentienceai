// I2CSensor.java

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
 * A sensor wrapper to allow easy access to I2C sensors, like the ultrasonic sensor.
 * Most of the code and the documentation
 * taken from the leJOS library (lejos.sourceforge.net, with thanks to the autor.
 */
public class I2CSensor extends Sensor implements SharedConstants
{
  private byte portId;
  private byte sensorType;
  protected lejos.nxt.I2CSensor is;
  protected lejos.nxt.SensorPort sp;

  /**
   * Creates a sensor instance of given type connected to the given port
   * using the given I2C device address (default address is 0x02).
   * @param port the port where the sensor is plugged-in
   * @param sensorType the type of the sensor
   * @param deviceAddress in standard Lego/NXT format (range 0x02-0xFE).
   * The low bit must always be zero.
   * Some data sheets (and older versions of leJOS) may use i2c 7 bit format
   * (0x01-0x7F) in which case this address must be shifted left one bit.
   */
  public I2CSensor(SensorPort port, byte sensorType, int deviceAddress)
  {
    this(port, sensorType);
    is.setAddress(deviceAddress);
  }

  /**
   * Creates a sensor instance of given type connected to the given port
   * with default device address 0x02.
   * @param port the port where the sensor is plugged-in
   * @param sensorType the type of the sensor
   */
  public I2CSensor(SensorPort port, byte sensorType)
  {
    super(port);
    this.sensorType = sensorType;
    portId = (byte) port.getId();
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
    is = new lejos.nxt.I2CSensor(sp);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: I2CSensor.init() called (Port: " +
        getPortLabel() + ")");

    sp.setTypeAndMode(sensorType, RAWMODE);
  }

  /**
   * Retrieves data from the sensor.
   * Data is read from registers in the sensor, usually starting at 0x00.
   * Just supply the register to start reading at, and the length of
   * bytes to read (16 maximum).
   * @param register the starting register used
   * @param buf the buffer where data are returned
   * @param len the length of data to read (minimum 1, maximum 16)
   * @return status zero=success, non-zero=failure
   */
  public int getData(int register, byte[] buf, int len)
  {
    checkConnect();
    return is.getData(register, buf, len);
  }

  /**
   * Sets a single byte in the I2C sensor.
   * @param register the data register in the I2C sensor
   * @param value the data sent to the sensor
   * @return status zero=success, non-zero=failure
   */
  public int sendData(int register, byte value)
  {
    checkConnect();
    return is.sendData(register, value);
  }

  /**
   * Send multiple values with a I2C write transaction.
   * @param register the starting register in the I2C sensor
   * @param buf the buffer where data are supplied
   * @param len the length the buffer (minimum 1, maximum 16)
   * @return status zero=success, non-zero=failure
   */
  public int sendData(int register, byte[] buf, int len)
  {
    checkConnect();
    return is.sendData(register, buf, len);
  }

  /**
   * Returns the product identifier (if available).
   * @return the product identifier (PID)
   */
  public String getProductID()
  {
    return is.getProductID();
  }

  /**
   * Returns the sensor version number (if available).
   * @return the sensor version number
   */
  public String getVersion()
  {
    return is.getVersion();
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.I2CSensor.
   * @return the reference of the I2CSensor
   */
  public lejos.nxt.I2CSensor getLejosI2CSensor()
  {
    return is;
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.SensorPort.
   * @return the reference of the SensorPort
   */
  public lejos.nxt.SensorPort getLejosPort()
  {
    return sp;
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("DEBUG: I2CSensor.cleanup() called (Port: " +
        getPortLabel() + ")");
  }

  private void checkConnect()
  {
    if (robot == null)
      new ShowError("I2CSensor (port: " + getPortLabel() +
        ") is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }
}
