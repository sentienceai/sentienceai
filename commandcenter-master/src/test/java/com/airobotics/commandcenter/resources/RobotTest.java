package com.airobotics.commandcenter.resources;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.BeaconSensor;
import com.airobotics.commandcenter.core.BeaconService;
import com.airobotics.commandcenter.core.InstructionService;
import com.airobotics.commandcenter.core.RobotService;
import com.airobotics.robot.Robot;
import com.airobotics.robot.api.IMotor;
import com.airobotics.robot.api.IQueue;
import com.airobotics.robot.api.IRFIDSensor;
import com.airobotics.robot.api.IRFIDSensorData;
import com.airobotics.robot.api.ITouchSensor;
import com.airobotics.robot.api.IUltrasonicSensor;
import com.airobotics.robot.api.RFIDListener;
import com.airobotics.robot.api.TouchListener;
import com.airobotics.robot.api.UltrasonicListener;
import com.airobotics.robot.api.core.RobotProperty;
import com.airobotics.robot.api.core.RobotState;
import com.airobotics.robot.nxt.NxtRFIDSensorData;
import com.airobotics.robot.util.HttpClient;
import com.vividsolutions.jts.geom.Coordinate;

public class RobotTest extends InstructionResourceTest {
	private static final String serverHost = "localhost";
	private static final int serverPort = 8080;

	protected Robot robot;
	private IMotor moverMotor;
	private IMotor directionMotor;
	private IMotor beaconMotor;
	private RobotState robotState;
	private IQueue<BeaconSensor> beaconSensorQueue;
	protected List<Coordinate> routePoints;
	private int routePointsIdx = -1;
	private double distanceToNextRoutePoint;
	private double directionToNextRoutePoint;
	private List<IRFIDSensorData> beaconSensorDataList;

	@SuppressWarnings("serial")
	// java.util.Queue in lejos is not an interface
	// so, a runtime exception happens with the simulator which uses a regular
	// JRE
	private static class QueueImpl<E> extends LinkedList<E> implements IQueue<E> {
	}

	@Before
	public void setUpRobotTest() throws Exception {
		routePointsIdx = -1;
		distanceToNextRoutePoint = 0;
		directionToNextRoutePoint = 0;
		beaconSensorQueue = new QueueImpl<BeaconSensor>();
		moverMotor = getMoverMotor();
		directionMotor = getDirectionMotor();
		beaconMotor = getBeaconMotor();
		robot = getRobot();
		robotState = getRobotState();
		robotState.intervalToCompleteRotationOfBeaconMotor = 1;
	}

	public Robot getRobot() {
		return new Robot(RobotProperty.getInstance(ResourceTestUtil.testRobot.getSerialNumber(), serverHost, serverPort,
				moverMotor, directionMotor, beaconMotor, getRFIDSensor(), getTouchSensor(), getUltrasonicSensor(),
				beaconSensorQueue, new HttpClient(), 100d));
	}

	public RobotState getRobotState() throws NoSuchFieldException, IllegalAccessException {
		Field robotStateField = robot.getClass().getDeclaredField("robotState");
		robotStateField.setAccessible(true);
		return (RobotState) robotStateField.get(robot);
	}

	public IMotor getBeaconMotor() {
		return new IMotor() {
			boolean isStalled = true;
			double currentAngle = -1;

			public double getCurrentPosition() {
				return currentAngle;
			}

			public void forward() {
				isStalled = false;
			}

			public void backward() {
			}

			public void stop() {
				isStalled = true;
				currentAngle = -1;
			}

			public void setSpeed(double speed) {
			}

			public void rotate(double angle) {
				isStalled = false;
				currentAngle = angle;
			}

			public boolean isMoving() {
				return !isStalled;
			}

			public void resetTachoCount() {
				currentAngle = -1;
			}

			public void rotate(double angle, boolean immediateReturn) {
			}
		};
	}

	public IMotor getMoverMotor() {
		return new IMotor() {
			public double getCurrentPosition() {
				return 0;
			}

			public void forward() {
			}

			public void backward() {
			}

			public void stop() {
			}

			public void setSpeed(double speed) {
			}

			public void rotate(double angle) {
				routeIdx = robotState.routeIdx;
				assertTrue((distanceToNextRoutePoint - angle) <= ResourceTestUtil.coverageDiameter);
			}

			public boolean isMoving() {
				return false;
			}

			public void resetTachoCount() {
			}

			public void rotate(double angle, boolean immediateReturn) {
			}
		};
	}

	public IMotor getDirectionMotor() {
		return new IMotor() {
			double currentAngle;

			public double getCurrentPosition() {
				return (int) Math.round(currentAngle);
			}

			public void forward() {
			}

			public void backward() {
			}

			public void stop() {
			}

			public void setSpeed(double speed) {
			}

			public void rotate(double angle) {
				currentAngle += angle;
				double directionAngle = RobotService.getDirection(ResourceTestUtil.testRobot.getWheelDiameter(),
						ResourceTestUtil.testRobot.getCoverageDiameter(), directionToNextRoutePoint);
				assertTrue(Math.abs(Math.abs(directionAngle) - Math.abs(angle)) < 10);
			}

			public boolean isMoving() {
				return false;
			}

			public void resetTachoCount() {
			}

			public void rotate(double angle, boolean immediateReturn) {
			}
		};
	}

	public IRFIDSensor getRFIDSensor() {
		return new IRFIDSensor() {
			public void addRFIDListener(RFIDListener rfidListener) {
			}

			@Override
			public void activate() {
			}

			@Override
			public void deactivate() {
			}

			@Override
			public List<IRFIDSensorData> readRfid() {
				return beaconSensorDataList;
			}

			@Override
			public void reset() {
				try {
					beaconSensorDataList = new LinkedList<IRFIDSensorData>();
					for (BeaconSensor beaconSensor : populateBeaconSensors())
						beaconSensorDataList
								.add(new NxtRFIDSensorData(beaconSensor.getSerialNumber(), beaconSensor.getAngle()));
				} catch (CloneNotSupportedException | InterruptedException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void setAngle(double angle) {
			}
		};
	}

	public ITouchSensor getTouchSensor() {
		return new ITouchSensor() {
			public void addTouchListener(TouchListener touchListener) {
			}
		};
	}

	public IUltrasonicSensor getUltrasonicSensor() {
		return new IUltrasonicSensor() {
			public void addUltrasonicListener(UltrasonicListener ultrasonicListener) {
			}
		};
	}

	public List<BeaconSensor> populateBeaconSensors() throws CloneNotSupportedException, InterruptedException {
		if (routePoints.size() <= ++routePointsIdx) {
			robotState.isPowerOffSignalPosted = true;
			return new ArrayList<BeaconSensor>();
		}

		Coordinate currentLocation = routePoints.get(routePointsIdx);
		Coordinate nextRoutePoint = getNextRoutePoint(currentLocation);
		List<Beacon> beacons = getSortedBeacons(currentLocation);
		List<BeaconSensor> beaconSensors = BeaconService.getBeaconSensors(beacons, currentLocation);

		setExpectedDistanceAndDirection(currentLocation, nextRoutePoint, beacons, beaconSensors);

		return beaconSensors;
	}

	private void setExpectedDistanceAndDirection(Coordinate currentLocation, Coordinate nextRoutePoint,
			List<Beacon> beacons, List<BeaconSensor> beaconSensors) {
		if (nextRoutePoint != null) {
			distanceToNextRoutePoint = Math.abs(getDistance(currentLocation, nextRoutePoint));
			directionToNextRoutePoint = InstructionService.getRelativeDirectionAngleFromCurrentLocationToNextLocation(
					currentLocation, nextRoutePoint, beacons.get(0).getLocation(), beaconSensors.get(0).getAngle(),
					directionMotor.getCurrentPosition());
		}
	}
}
