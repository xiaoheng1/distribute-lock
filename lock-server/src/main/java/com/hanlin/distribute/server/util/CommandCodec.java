package com.hanlin.distribute.server.util;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;

/**
 * @author shaoyu
 * 编解码.
 */
public class CommandCodec {
    /**
     * encode the command,returns the byte array.
     * @param obj
     * @return
     */
    public static byte[] encodeCommand(Object obj) {
        try {
            return SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(obj);
        } catch (final CodecException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Decode the command object from byte array.
     * @param content
     * @param clazz
     * @return
     */
    public static <T> T decodeCommand(byte[] content, Class<T> clazz) {
        try {
            return SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(content, clazz.getName());
        } catch (final CodecException e) {
            throw new IllegalStateException(e);
        }
    }
}
