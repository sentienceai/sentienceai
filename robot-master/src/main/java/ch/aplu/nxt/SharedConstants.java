// SharedConstants.java

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

/* History:
  V1.00 - Aug 2008:
    - First official release
  V1.01 - Aug 2008:
    - Added flag inCallback for all sensors to inhibit more than
      one callback at the same time
  V1.02 - Aug 2008:
    - Added sendFile(), startProgram(), findFirst(), findNext()
  V1.03 - Aug 2008:
    - Added parameter SensorPort in all sensor callback methods
  V1.04 - Oct 2009:
    - Source code included
    - JavaDoc revisted
    - Modified: Motor.continueTo() turns now to absolute position
    - Added: Motor.continueRelativeTo()
    - Modified: LightSensor init turns LED off
  V1.05 - Nov 2008:
    - Added CompassSensor, CompassAdapter, CompassListener
    - Bug fixed in ch.aplu.util.AlarmTimer callback notification
  V1.06 - Jan 2009:
    - Added PrototypeSensor, PrototypeAdapter, PrototypeListener
    - All SensorAdapters now implement their listener
  V1.07 - Feb 2009:
    - Compile Java SE V5 for backward compatibilty
  V1.08 - Apr 2009:
    - btc.setIOMode(NXTConnection.RAW) instead of btc.setIOMode(0)
      in BluetoothServer.java (compatible for leJOS 0.7)
  V1.09 - Apr 2009:
    - btc.setIOMode(NXTConnection.RAW) instead of btc.setIOMode(0)
      in BluetoothClient.java (compatible for leJOS 0.7)
  V1.10 - Jan 2010
    - Update of the Java documentation
    - Only for Version leJOS 0.85 released
  V1.11 - Feb 2010
    - Added RFID sensor
    - Added TurtleCrawler
    - Added Gear.isMoving()
  V1.12 - Apr 2010
    - Modified to public: SensorPort.getId(), SensorPort.getLabel()
    - Modified to public: MotorPort.getId(), MotorPort.getLabel()
    - Added: addXXXListener with default trigger level, setTriggerLevel()
    - Added ColorLightSensor
    - Fixed wrong error messages
    - Added check for port conflicts
    - Firmware previous to 0.85 no longer supported
  V1.13 - May 2010
    - Added SensorPort.equals() to compare port labels
    - Added Transceiver.releaseConnection(), Transceiver.isConnected()
    - Added TransceiverListener.isListening() to notify when server starts
  V1.14 - Sep 2010
    - Subsequent calls of motor and gear movement methods return immediately
      if motor or gear are already in the requested movement
    - Added dummy classes NxtContext, Obstacle with empty methods
      for source compatibility with NxtSim libary
  V1.15 - Apr 2011
    - Added classes to support HiTechnics sensors: accelerometer sensor,
      color sensor, gyro sensor
  V1.16 - Jul 2011
    - Gear timed movement methods simplified using Thread.sleep() due to
      crash if many subsequent calls follow in a tight loop
  V1.17 - Sep 2011
    - Ported to leJOS version 0.90 (modificatins in classes ColorSensor, ColorSensorHT
      and Motor)
  V1.18 - Feb 2012
    - Ported to leJOS version 0.91
    - Added: Class SuperProSensor (supports Hitechnic SuperPro Prototype board)
  V1.19 - March 2011
    - Ported to leJOS version 0.91beta-2
    - Motor.stop() now returns immediately, unlike lejos.nxt.Motor.stop(),
      but like lejos.nxt.Motor.stop(true)


*/

package ch.aplu.nxt;

/**
 * Public declarations of important global constants.
 */
public interface SharedConstants
{
  int DEBUG_LEVEL_OFF = 0;    // Elementary debug info
  int DEBUG_LEVEL_LOW = 1;    // Debug info when threads start/stop
  int DEBUG_LEVEL_MEDIUM = 2; // Debug info for important method calls its paramaters
  int DEBUG_LEVEL_HIGH = 3;   // Debug info for sendData()/readData()

  String ABOUT =
    "2003-2012 Aegidius Pluess\n" +
    "OpenSource Free Software\n" +
    "http://www.aplu.ch\n" +
    "All rights reserved";
  String VERSION = "1.19";

  String TITLE = "NxtJLib V" + VERSION + "   (www.aplu.ch)";
  String TITLEMP = "NxtJLib V" + VERSION + "\n(www.aplu.ch)";

  // ---------------- NxtJLib properties ---------------

  // Debug level is one of these values:
  //   0: Elementary debug info
  //   1: Debug info when threads are created/started/stopped
  //   2: Debug info for important method calls and its parameters
  //   3: Debug info for sendData()/readData()
  // Higher levels includes debug information of lower levels
  // Must be int
  int DEBUGLEVEL = DEBUG_LEVEL_OFF;

  // Default motor speed
  int MOTORSPEED = 50;

  // Factor to multiply NxtJLib speed to get NXJ speed
  int MOTORSPEEDMULTIPLIER = 9;

  // Factor between speed and velocity (in m/s): velocity = MotorSpeedFactor * speed
  double MOTORSPEEDFACTOR = 0.0044;

  // Default gear speed
  int GEARSPEED = 50;

  // Time to wait for next movement call before stopping the preceeding movement
  int BRAKEDELAY = 20;  // ms

  // Axe length (in m) used by Gear.leftArc(), Gear.rightArc()
  // Must be double
  double AXELENGTH = 0.05;

  // Turtle speed for straight movements
  int TURTLESPEED = 30;

  // Factor between turtle steps and motor rotation count
  double TURTLESTEPFACTOR = 10.0;

  // Turtle speed for rotations
  int TURTLEROTATIONSPEED = 10;

  // Factor between turtle angle (in degrees) and motor rotation count
  double TURTLEROTATIONFACTOR = 1.9;

  // TurtleCrawler speed for straight movements
  int CRAWLERSPEED = 30;

  // Factor between turtle crawler steps and motor rotation count
  double CRAWLERSTEPFACTOR = 10.0;

  // TurtleCrawler speed for rotations
  int CRAWLERROTATIONSPEED = 20;

  // Factor between turtle crawler angle (in degrees) and motor rotation count
  double CRAWLERROTATIONFACTOR = 5.7;

  // Wait time (in ms) between polling the sensor value when registering an event handler
  int MOTIONDETECTORPOLLDELAY = 100;

  // Wait time (in ms) between polling the sensor value when registering an event handler
  int COMPASSPOLLDELAY = 30;
  int ULTRASONICPOLLDELAY = 30;
  int PROTOTYPEPOLLDELAY = 30;
  int ACCELEROMETERPOLLDELAY = 30;
  int TOUCHPOLLDELAY = 30;
  int LIGHTPOLLDELAY = 30;
  int COLORPOLLDELAY = 500;
  int SOUNDPOLLDELAY = 30;
  int RFIDPOLLDELAY = 200;
  int GYROPOLLDELAY = 30;
  int INFRAREDSEEKERPOLLDELAY = 30;

  // Wait time (in ms) before enabling sensor events
  // Crash may result, if too short
  int SENSOREVENTDELAY = 500; // at least 200

  // ---------------- End of properties ----------------


  byte DIRECT_COMMAND_REPLY = 0x00;
  byte SYSTEM_COMMAND_REPLY = 0x01;
  byte REPLY_COMMAND = 0x02;
  byte DIRECT_COMMAND_NOREPLY = (byte)0x80;
  byte SYSTEM_COMMAND_NOREPLY = (byte)0x81;
  byte OPEN_READ = (byte)0x80;
  byte OPEN_WRITE = (byte)0x81;
  byte READ = (byte)0x82;
  byte WRITE = (byte)0x83;
  byte CLOSE = (byte)0x84;
  byte DELETE = (byte)0x85;
  byte FIND_FIRST = (byte)0x86;
  byte FIND_NEXT = (byte)0x87;
  byte GET_FIRMWARE_VERSION = (byte)0x88;
  byte OPEN_WRITE_LINEAR = (byte)0x89;
  byte OPEN_READ_LINEAR = (byte)0x8A;
  byte OPEN_WRITE_DATA = (byte)0x8B;
  byte OPEN_APPEND_DATA = (byte)0x8C;
  byte BOOT = (byte)0x97;
  byte SET_BRICK_NAME = (byte)0x98;
  byte GET_DEVICE_INFO = (byte)0x9B;
  byte DELETE_USER_FLASH = (byte)0xA0;
  byte POLL_LENGTH = (byte)0xA1;
  byte POLL = (byte)0xA2;
  byte POLL_BUFFER = (byte)0x00;
  byte HIGH_SPEED_BUFFER = (byte)0x01;
  byte START_PROGRAM = 0x00;
  byte STOP_PROGRAM = 0x01;
  byte PLAY_SOUND_FILE = 0x02;
  byte PLAY_TONE = 0x03;
  byte SET_OUTPUT_STATE = 0x04;
  byte SET_INPUT_MODE = 0x05;
  byte GET_OUTPUT_STATE = 0x06;
  byte GET_INPUT_VALUES = 0x07;
  byte RESET_SCALED_INPUT_VALUE = 0x08;
  byte MESSAGE_WRITE = 0x09;
  byte RESET_MOTOR_POSITION = 0x0A;
  byte GET_BATTERY_LEVEL = 0x0B;
  byte STOP_SOUND_PLAYBACK = 0x0C;
  byte KEEP_ALIVE = 0x0D;
  byte LS_GET_STATUS = 0x0E;
  byte LS_WRITE = 0x0F;
  byte LS_READ = 0x10;
  byte GET_CURRENT_PROGRAM_NAME = 0x11;
  byte MESSAGE_READ = 0x13;
  byte MOTORON = 0x01;
  byte BRAKE = 0x02;
  byte REGULATED = 0x04;
  byte REGULATION_MODE_IDLE = 0x00;
  byte REGULATION_MODE_MOTOR_SPEED = 0x01;
  byte REGULATION_MODE_MOTOR_SYNC = 0x02;
  byte MOTOR_RUN_STATE_IDLE = 0x00;
  byte MOTOR_RUN_STATE_RAMPUP = 0x10;
  byte MOTOR_RUN_STATE_RUNNING = 0x20;
  byte MOTOR_RUN_STATE_RAMPDOWN = 0x40;
  byte NO_SENSOR = 0x00;
  byte SWITCH = 0x01;
  byte TEMPERATURE = 0x02;
  byte REFLECTION = 0x03;
  byte ANGLE = 0x04;
  byte LIGHT_ACTIVE = 0x05;
  byte LIGHT_INACTIVE = 0x06;
  byte SOUND_DB = 0x07;
  byte SOUND_DBA = 0x08;
  byte CUSTOM = 0x09;
  byte LOWSPEED = 0x0A;
  byte LOWSPEED_9V = 0x0B;
  byte NO_OF_SENSOR_TYPES = 0x0C;
  byte RAWMODE = 0x00;
  byte BOOLEANMODE = 0x20;
  byte TRANSITIONCNTMODE = 0x40;
  byte PERIODCOUNTERMODE = 0x60;
  byte PCTFULLSCALEMODE = (byte)0x80;
  byte CELSIUSMODE = (byte)0xA0;
  byte FAHRENHEITMODE = (byte)0xC0;
  byte ANGLESTEPSMODE = (byte)0xE0;
  byte SLOPEMASK = 0x1F;
  byte MODEMASK = (byte)0xE0;
}
