// RFIDSensor.java

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
 * Class that represents a RFID sensor from CODATEX (www.codatex.com).
 */
public class RFIDSensor extends Sensor implements SharedConstants
{

  // -------------- Inner class RFIDSensorThread ------------
  private class RFIDSensorThread extends Thread
  {
    private volatile boolean isRunning = false;
    private long oldId = 0;

    private RFIDSensorThread()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("RFTh created");
    }

    public void run()
    {
      if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
        DebugConsole.show("RFTh started");

      isRunning = true;
      while (isRunning)
      {
        if (!isEvtEnabled)
          continue;

        if (rfidListener != null)
        {
          Tools.delay(SharedConstants.RFIDPOLLDELAY);
          long id = getTransponderId();
          if (id > 0)
          {
            if (id != oldId)
            {
              if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_LOW)
                DebugConsole.show("Evt'detected'(" + getPortLabel() + ")");
              rfidListener.detected(getPort(), id);
              oldId = id;
            }
          }
          else
            oldId = 0;
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
          DebugConsole.show("RFTh stop failed");
        else
          DebugConsole.show("RFTh stop ok");
    }
  }
// -------------- End of inner classes -----------------------

  private lejos.nxt.addon.RFIDSensor rs;
  private lejos.nxt.SensorPort sp;
  private RFIDListener rfidListener = null;
  private volatile static boolean inCallback = false;
  private RFIDSensorThread rst;
  private volatile static boolean isEvtEnabled = false;
  private boolean isContinuous = false;

  /**
   * Creates a sensor instance connected to the given port.
   * The sensor may be in two modes: Single Read or Continuous Read
   * (default: Single Read). In Single Read mode the sensor is
   * somewhat slower because it returns into a sleep state after
   * 2 seconds of inactivity for power saving reasons. It is woke up
   * automatically at the next call of getTransponderId(). To change the
   * mode, use setContMode().
   * @param port the port where the sensor is plugged-in
   */
  public RFIDSensor(SensorPort port)
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
    rst = new RFIDSensorThread();
    rs = new lejos.nxt.addon.RFIDSensor(sp);
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public RFIDSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("rs.init()");
  }

  protected void cleanup()
  {
    if (NxtRobot.getDebugLevel() >= DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("rs.cleanup()");
    isEvtEnabled = false;  // Disable events
    rs.stop();
    if (rst != null)
      rst.stopThread();
 }

  /**
   * Registers the given rfid listener.
   * If the rfid thread is not yet started, start it now.
   * @param rfidListener the RFIDListener to become registered.
   */
  public void addRFIDListener(RFIDListener rfidListener)
  {
    this.rfidListener = rfidListener;
    setContMode(true);
    if (!rst.isAlive())
    {
      rst.start();
      // Wait a moment until enabling events, otherwise crash may result
      Tools.delay(SharedConstants.SENSOREVENTDELAY);
      isEvtEnabled = true;
     }
   }

  /**
   * Returns the reference of the the underlying lejos.nxt.RFIDSensor.
   * @return the reference of the TouchSensor
   */
  public lejos.nxt.addon.RFIDSensor getLejosSensor()
  {
    return rs;
  }

  /**
   * Returns the reference of the the underlying lejos.nxt.SensorPort.
   * @return the reference of the SensorPort
   */
  public lejos.nxt.SensorPort getLejosPort()
  {
    return sp;
  }

  private void checkConnect()
  {
    if (robot == null)
     new ShowError("RFIDSensor is not a part of the NxtRobot.\n" +
        "Call addPart() to assemble it.");
  }

  /**
   * Returns the product identifier (if available).
   * @return the product identifier (PID)
   */
  public String getProductID()
  {
    checkConnect();
    return rs.getProductID();
  }

  /**
   * Returns the sensor version number (if available).
   * @return the sensor version number
   */
  public String getVersion()
  {
    checkConnect();
    return rs.getVersion();
  }

  /**
   * Returns the serial number of the RFID sensor.
   * @return the 12 byte serial number or null, if sensor not available.
   */
  public byte[] getSerialNo()
  {
    return rs.getSerialNo();
  }

  /**
   * Selects between Single Read and Continous Read mode.
   * @param continuous if true, the sensor is put in Continous Read mode; otherwise
   * it is put in Single Read mode.
   */
  public void setContMode(boolean continuous)
  {
    if (isContinuous && !continuous)
       rs.stop();
    isContinuous = continuous;
  }

  /**
   * Polls the sensor. If the sensor is in Single Read mode and in sleep
   * state, it is automatically woke up.
   * @return the current transponder id (0: no transponder detected)
   */
  public long getTransponderId()
  {
    checkConnect();
    return rs.readTransponderAsLong(isContinuous);
  }
}
