package com.airobotics.commandcenter.resources;

import com.fasterxml.jackson.core.JsonProcessingException;

import json.JSONException;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class InstructionResourceActiveInstructionTest extends InstructionResourceTest {
	@Test
	public void getInstruction_withRoutePoints_iterateAllRoutePoints() throws JsonProcessingException,
			UnsupportedEncodingException, JSONException, CloneNotSupportedException, InterruptedException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		assertIterateAllRoutePoints(route);
	}

	@Test
	public void getInstruction_withRouteWithMidPoint_iterateAllRoutePoints() throws JsonProcessingException,
			UnsupportedEncodingException, JSONException, CloneNotSupportedException, InterruptedException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		assertIterateAllRoutePoints(getRouteWithMidPoint());
	}

	@Test
	public void getInstruction_withRouteWithOffTrack_iterateAllRoutePoints() throws JsonProcessingException,
			UnsupportedEncodingException, JSONException, CloneNotSupportedException, InterruptedException,
			NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		assertIterateAllRoutePoints(getRouteWithOffTrack());
	}
}