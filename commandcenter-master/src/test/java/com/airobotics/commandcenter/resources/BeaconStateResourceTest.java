package com.airobotics.commandcenter.resources;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.BeforeClass;
import org.junit.Test;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.BeaconState;
import com.airobotics.core.util.AngleUtil;
import com.vividsolutions.jts.geom.Coordinate;

public class BeaconStateResourceTest extends ResourceTest {
	private static final String TEST_DEVICE_ID = "test_device_id";
	protected static List<Beacon> testBeacons;
	protected static List<String> testBeaconIds;

	@SuppressWarnings("serial")
	@BeforeClass
	public static void setUpClass() throws Exception {
		testBeacons = ResourceTestUtil.getBeaconsOfTestRobot(ResourceTestUtil.testRobot);
		testBeaconIds = new ArrayList<String>() {
			{
				add(testBeacons.get(6).getId());
				add(testBeacons.get(5).getId());
				add(testBeacons.get(4).getId());
				add(testBeacons.get(7).getId());
			}
		};
		setScanOrderByTestBeaconIds();
	}

	private static void setScanOrderByTestBeaconIds() {
		Response response = ResourceTestUtil.getResourcePostResponse("beacons/scan-order", testBeaconIds);
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
	}

	@Test
	public void setScanCompleted_withScanOrderByTestBeaconIds_scanOrderByTestBeaconIds() {
		for (int i = 0; i < testBeaconIds.size(); i++) {
			String beaconId = testBeaconIds.get(i);
			verifyBeaconRunningState(beaconId, true);
			for (int j = 0; j < testBeaconIds.size(); j++)
				if (i != j)
					verifyBeaconRunningState(testBeaconIds.get(j), false);
			setScanCompleted(beaconId);
		}
	}

	private void verifyBeaconRunningState(String beaconId, boolean isRunning) {
		Response response = ResourceTestUtil.getResourceGetResponse("beacons/" + beaconId + "/state", "", "");
		BeaconState beaconState = response.readEntity(BeaconState.class);
		assertTrue(beaconState.isRunning == isRunning);
	}

	private void setScanCompleted(String beaconId) {
		BeaconState beaconState = new BeaconState();
		beaconState.isRunning = false;
		Response response = ResourceTestUtil.getResourcePutResponse("beacons/" + beaconId + "/state", beaconState);
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
	}

	@Test
	public void setDeviceAngle_withAnglesScannedByBeacons_retrieveDeviceLocation() {
		setDeviceAngle(testBeaconIds.get(0), 6680);
		setDeviceAngle(testBeaconIds.get(1), 10693);
		setDeviceAngle(testBeaconIds.get(2), 24680);
		setDeviceAngle(testBeaconIds.get(3), 28693);
		verifyLocation();
	}

	private void setDeviceAngle(String beaconId, int angle) {
		BeaconState beaconState = new BeaconState();
		beaconState.deviceId = TEST_DEVICE_ID;
		beaconState.angle = angle;
		Response response = ResourceTestUtil.getResourcePostResponse("beacons/" + beaconId + "/state", beaconState);
		assertTrue(response.getStatus() == Status.OK.getStatusCode());
	}

	private void verifyLocation() {
		Response response = ResourceTestUtil
				.getResourceGetResponse("robots/" + TEST_DEVICE_ID + "/instructions/location", "", "");
		Coordinate location = response.readEntity(Coordinate.class);
		assertTrue(location.x == -71.35262750999999);
		assertTrue(location.y == 42.3238025);
	}
}
