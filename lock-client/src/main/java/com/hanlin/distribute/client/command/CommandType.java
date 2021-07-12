package com.hanlin.distribute.client.command;

/**
 * 命令类型.
 * @author shaoyu
 */
public enum CommandType {
    LOCK, UNLOCK;

    public byte toByte() {
        switch (this) {
            case LOCK:
                return (byte) 0;
            case UNLOCK:
                return (byte) 1;
        }
        throw new IllegalArgumentException();
    }

    public static CommandType parseByte(byte b) {
        switch (b) {
            case 0:
                return LOCK;
            case 1:
                return UNLOCK;
        }
        throw new IllegalArgumentException();
    }
}