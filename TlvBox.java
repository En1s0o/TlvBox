package io.en1s0o.tlvbox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TlvBox - Type Length Value Box
 *
 * @author Eniso
 */
@SuppressWarnings("unused")
public class TlvBox {

    private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private final Map<Integer, byte[]> map = new HashMap<>();

    public void deserialize(byte[] buffer, int offset) {
        int parsed = 0;
        int size = buffer.length;
        while (parsed < size) {
            // type
            int type = ByteBuffer.wrap(buffer, offset + parsed, Integer.BYTES)
                    .order(DEFAULT_BYTE_ORDER).getInt();
            parsed += Integer.BYTES;

            // length
            int length = ByteBuffer.wrap(buffer, offset + parsed, Integer.BYTES)
                    .order(DEFAULT_BYTE_ORDER).getInt();
            parsed += Integer.BYTES;

            // payload
            byte[] payload = new byte[length];
            System.arraycopy(buffer, parsed, payload, 0, length);
            map.put(type, payload);
            parsed += length;
        }
    }

    public Optional<byte[]> serialize() throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            for (Integer key : map.keySet()) {
                byte[] payload = map.get(key);
                byte[] type = ByteBuffer.allocate(Integer.BYTES)
                        .order(DEFAULT_BYTE_ORDER).putInt(key).array();
                byte[] length = ByteBuffer.allocate(Integer.BYTES)
                        .order(DEFAULT_BYTE_ORDER).putInt(payload.length).array();
                os.write(type);
                os.write(length);
                os.write(payload);
            }
            return Optional.of(os.toByteArray());
        }
    }

    public Optional<byte[]> getByte(int type) {
        return Optional.ofNullable(map.get(type));
    }

    public TlvBox putByte(int type, byte[] value) {
        map.put(type, value);
        return this;
    }

    public Optional<Short> getShort(int type) {
        byte[] payload = map.get(type);
        if (payload == null) {
            return Optional.empty();
        }
        return Optional.of(ByteBuffer.wrap(payload).order(DEFAULT_BYTE_ORDER).getShort());
    }

    public TlvBox putShort(int type, short value) {
        map.put(type, ByteBuffer.allocate(Short.BYTES)
                .order(DEFAULT_BYTE_ORDER).putShort(value).array());
        return this;
    }

    public Optional<Integer> getInt(int type) {
        byte[] payload = map.get(type);
        if (payload == null) {
            return Optional.empty();
        }
        return Optional.of(ByteBuffer.wrap(payload).order(DEFAULT_BYTE_ORDER).getInt());
    }

    public TlvBox putInt(int type, int value) {
        map.put(type, ByteBuffer.allocate(Integer.BYTES)
                .order(DEFAULT_BYTE_ORDER).putInt(value).array());
        return this;
    }

    public Optional<Long> getLong(int type) {
        byte[] payload = map.get(type);
        if (payload == null) {
            return Optional.empty();
        }
        return Optional.of(ByteBuffer.wrap(payload).order(DEFAULT_BYTE_ORDER).getLong());
    }

    public TlvBox putLong(int type, long value) {
        map.put(type, ByteBuffer.allocate(Long.BYTES)
                .order(DEFAULT_BYTE_ORDER).putLong(value).array());
        return this;
    }

    public Optional<Float> getFloat(int type) {
        byte[] payload = map.get(type);
        if (payload == null) {
            return Optional.empty();
        }
        return Optional.of(ByteBuffer.wrap(payload).order(DEFAULT_BYTE_ORDER).getFloat());
    }

    public TlvBox putFloat(int type, float value) {
        map.put(type, ByteBuffer.allocate(Float.BYTES)
                .order(DEFAULT_BYTE_ORDER).putFloat(value).array());
        return this;
    }

    public Optional<Double> getDouble(int type) {
        byte[] payload = map.get(type);
        if (payload == null) {
            return Optional.empty();
        }
        return Optional.of(ByteBuffer.wrap(payload).order(DEFAULT_BYTE_ORDER).getDouble());
    }

    public TlvBox putDouble(int type, double value) {
        map.put(type, ByteBuffer.allocate(Double.BYTES)
                .order(DEFAULT_BYTE_ORDER).putDouble(value).array());
        return this;
    }

    public Optional<String> getString(int type) {
        return getByte(type).map(b -> new String(b, StandardCharsets.UTF_8));
    }

    public TlvBox putString(int type, String value) {
        map.put(type, value.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public Optional<TlvBox> getTlvBox(int type) {
        Optional<byte[]> payload = getByte(type);
        if (payload.isPresent()) {
            TlvBox box = new TlvBox();
            try {
                box.deserialize(payload.get(), 0);
                return Optional.of(box);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public TlvBox putTlvBox(int type, TlvBox box) throws IOException {
        box.serialize().ifPresent(b -> map.put(type, b));
        return this;
    }

}
