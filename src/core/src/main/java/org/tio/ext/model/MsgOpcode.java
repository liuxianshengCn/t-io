package org.tio.ext.model;

import java.util.HashMap;
import java.util.Map;

public enum MsgOpcode {

  NOT_FIN((byte) 0), TEXT((byte) 1), BINARY((byte) 2), CLOSE((byte) 8), PING((byte) 9), PONG((byte) 10);

	private static final Map<Byte, MsgOpcode> map = new HashMap<>();

	static {
		for (MsgOpcode command : values()) {
			map.put(command.getCode(), command);
		}
	}

	public static MsgOpcode valueOf(byte code) {
		return map.get(code);
	}

	private final byte code;

	private MsgOpcode(byte code) {
		this.code = code;
	}

	public byte getCode() {
		return code;
	}

}
