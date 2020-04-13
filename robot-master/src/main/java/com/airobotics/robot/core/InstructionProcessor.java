package com.airobotics.robot.core;

import java.util.Hashtable;

import com.airobotics.api.entities.Instruction;
import com.airobotics.robot.api.core.RobotProperty;
import com.airobotics.robot.api.core.RobotState;

public class InstructionProcessor {
	private static Hashtable<Instruction.Type, IInstructionHandler> instructionHandlers;

	static {
		init();
	}

	private static synchronized void init() {
		instructionHandlers = new Hashtable<Instruction.Type, IInstructionHandler>();
		for (Instruction.Type type : Instruction.Type.values()) {
			instructionHandlers.put(type, getInstructionHandler(type));
		}
	}

	private static IInstructionHandler getInstructionHandler(Instruction.Type instructionType) {
		// return Class.forName("com.airobotics.robot.core." + instructionType +
		// "InstructionHandler").newInstance();
		switch (instructionType) {
		case None:
			return new NoneInstructionHandler();
		case Stop:
			return new StopInstructionHandler();
		case New:
			return new NewInstructionHandler();
		case Update:
			return new UpdateInstructionHandler();
		case Keep:
			return new KeepInstructionHandler();
		case Resend:
			return new ResendInstructionHandler();
		case Done:
			return new DoneInstructionHandler();
		}
		return new DefaultInstructionHandler();
	}

	public static void process(Instruction instruction, RobotProperty robotProperty, RobotState robotState) {
		if (instructionHandlers == null)
			init();

		IInstructionHandler instructionHandler = instructionHandlers.get(instruction.type);
		instructionHandler.process(instruction, robotProperty, robotState);
	}
}
