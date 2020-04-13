package com.airobotics.commandcenter.resources;

import java.util.ArrayList;

import javax.ws.rs.client.ClientBuilder;

import org.junit.BeforeClass;

import com.airobotics.api.entities.Beacon;
import com.airobotics.api.entities.Boundary;
import com.airobotics.api.entities.Robot;
import com.airobotics.api.entities.Schedule;
import com.vividsolutions.jts.geom.Coordinate;

public class NxtRobotResourceTest extends RobotResourceTest {
	@SuppressWarnings("serial")
	@BeforeClass
	public static void setUpClass() throws Exception {
		ResourceTestUtil.port = RULE.getLocalPort();
		ResourceTestUtil.client = ClientBuilder.newClient();

		testRobot = new Robot("00165315430B", 43, 120);
		// 5'6" 11/16 (1885.95 mm)
		// (-71.35268598, 42.32378300), (-71.35266724, 42.32378086)
		// (-71.35268720, 42.32377233), (-71.35266846, 42.32377019)
		// 2'10" 11/16 (1073.15 mm)
		testBeacons = new ArrayList<Beacon>() {
			{
				add(new Beacon("cc370f19-b576-4cc8-9bb3-ae4a48b44f2c", new Coordinate(-71.35268720, 42.32378109))); // NW
				add(new Beacon("6d1a6146-0149-4586-89da-8dd4076353eb", new Coordinate(-71.35267501, 42.32378109))); // NE
				add(new Beacon("00434ea1-7236-4f0b-ab51-bce11d3c23d2", new Coordinate(-71.35267501, 42.32377233))); // SE
				add(new Beacon("c57c61af-521a-4261-b554-5939f1df1f42", new Coordinate(-71.35268720, 42.32377233))); // SW
			}
		};
		testSchedule = new Schedule();
		testBoundary = new Boundary();

		// 53' - 24' (4.41667 ft - 2 ft) (1346.2 mm - 609.6 mm)
		// 44 X 100 (3.66667 ft X 8.33333 ft) (1117.6 mm X 2540 mm)

		// NW 813.351466159 mm 1488.5855215 mm NE
		// 1427.25298 mm 1427.25298 mm
		// 275.690116 mm 275.690116 mm
		// SW 813.351466159 mm 1488.5855215 mm SE

		// NW 4.41667 8.08333333 NE
		// 10.3333333 10.3333333
		// 2 2
		// SW 4.41667 8.08333333 SE

		// 42.32378500, -71.35268500 //NW
		// 42.32378639, -71.35268475 (10.355)
		// 42.32377504, -71.35268672 (1.996)
		// 42.32377233, -71.35268720 //SW

		// 42.32378276, -71.35266175 //NE
		// 42.32378415, -71.35266150 (10.355)
		// 42.32377280, -71.35266347 (1.996)
		// 42.32377009, -71.35266395 //SE

		// 42.32377504, -71.35268672 (1.996 SW)
		// 42.32377425, -71.35267862 (4.412)
		// 42.32377361, -71.35267190 (8.071)
		// 42.32377280, -71.35266347 (1.996 SE)

		// 42.32378639, -71.35268475 (10.355 NW)
		// 42.32378560, -71.35267665 (4.412)
		// 42.32378496, -71.35266993 (8.071)
		// 42.32378415, -71.35266150 (10.355 NE)

		testBoundaryPoints = new ArrayList<Coordinate>() {
			{
				add(new Coordinate(-71.35268710, 42.32378099)); // NW
				add(new Coordinate(-71.35267511, 42.32378099)); // NE
				add(new Coordinate(-71.35267511, 42.32377243)); // SE
				add(new Coordinate(-71.35268710, 42.32377243)); // SW
			}
		};

		setUpTestUser();
		setUpTestRobot();
		setUpTestBeacons();
		setUpTestSchedule();
		setUpTestBoundaries();
		updateInternalLocations();
	}

	public static void interpolationByDistance(Coordinate p1, Coordinate p2, double d) {
		double len = p1.distance(p2);
		double ratio = d / len;
		double x = ratio * p2.x + (1.0 - ratio) * p1.x;
		double y = ratio * p2.y + (1.0 - ratio) * p1.y;
		System.out.println(x + ", " + y);
	}

	public static void pointAlong(Coordinate p0, Coordinate p1, double segmentLengthFraction) {
		double x = p0.x + segmentLengthFraction * (p1.x - p0.x);
		double y = p0.y + segmentLengthFraction * (p1.y - p0.y);
		System.out.println(x + ", " + y);
	}
}
