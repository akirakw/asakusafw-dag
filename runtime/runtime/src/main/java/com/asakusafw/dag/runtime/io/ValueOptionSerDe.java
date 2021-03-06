/**
 * Copyright 2011-2016 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.dag.runtime.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;

import com.asakusafw.dag.utils.common.Invariants;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Serializes/deserializes {@link ValueOption} objects.
 */
@SuppressWarnings("deprecation")
public final class ValueOptionSerDe {

    private static final int UNSIGNED_NULL = -1;

    private static final byte NULL_HEADER = 0;

    private static final byte NON_NULL_HEADER = 1;

    private static final Map<Class<? extends ValueOption<?>>, SerDe> SERDE;
    static {
        Map<Class<? extends ValueOption<?>>, SerDe> m = new HashMap<>();
        register(m, BooleanOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, ByteOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, ShortOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, IntOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, LongOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, FloatOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, DoubleOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, DecimalOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, DateOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, DateTimeOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        register(m, StringOption.class, ValueOptionSerDe::serialize, ValueOptionSerDe::deserialize);
        SERDE = m;
    }

    private static <T extends ValueOption<T>> void register(
            Map<Class<? extends ValueOption<?>>, SerDe> registry,
            Class<T> type, IoAction<T, DataOutput> serializer, IoAction<T, DataInput> deserializer) {
        registry.put(type, new SerDe() {
            @Override
            public void serialize(ValueOption<?> value, DataOutput output) throws IOException {
                serializer.perform(type.cast(value), output);
            }
            @Override
            public void deserialize(ValueOption<?> value, DataInput input) throws IOException {
                deserializer.perform(type.cast(value), input);
            }
        });
    }

    private ValueOptionSerDe() {
        return;
    }

    /**
     * Serializes {@link ValueOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serializeAny(ValueOption<?> option, DataOutput output) throws IOException {
        SerDe serde = Invariants.requireNonNull(SERDE.get(option.getClass()));
        serde.serialize(option, output);
    }

    /**
     * Deserializes {@link ValueOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserializeAny(ValueOption<?> option, DataInput input) throws IOException {
        SerDe serde = Invariants.requireNonNull(SERDE.get(option.getClass()));
        serde.deserialize(option, input);
    }

    /**
     * Serializes {@link BooleanOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(BooleanOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.write(UNSIGNED_NULL);
        } else {
            output.writeBoolean(option.get());
        }
    }

    /**
     * Deserializes {@link BooleanOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(BooleanOption option, DataInput input) throws IOException {
        byte b = input.readByte();
        if (b == UNSIGNED_NULL) {
            option.setNull();
        } else {
            option.modify(b != 0);
        }
    }

    /**
     * Serializes {@link ByteOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(ByteOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.write(NULL_HEADER);
        } else {
            output.write(NON_NULL_HEADER);
            output.writeByte(option.get());
        }
    }

    /**
     * Deserializes {@link ByteOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(ByteOption option, DataInput input) throws IOException {
        byte header = input.readByte();
        if (header == NULL_HEADER) {
            option.setNull();
        } else {
            option.modify(input.readByte());
        }
    }

    /**
     * Serializes {@link ShortOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(ShortOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.write(NULL_HEADER);
        } else {
            output.write(NON_NULL_HEADER);
            output.writeShort(option.get());
        }
    }

    /**
     * Deserializes {@link ShortOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(ShortOption option, DataInput input) throws IOException {
        byte header = input.readByte();
        if (header == NULL_HEADER) {
            option.setNull();
        } else {
            option.modify(input.readShort());
        }
    }

    /**
     * Serializes {@link IntOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(IntOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.write(NULL_HEADER);
        } else {
            output.write(NON_NULL_HEADER);
            output.writeInt(option.get());
        }
    }

    /**
     * Deserializes {@link IntOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(IntOption option, DataInput input) throws IOException {
        byte header = input.readByte();
        if (header == NULL_HEADER) {
            option.setNull();
        } else {
            option.modify(input.readInt());
        }
    }

    /**
     * Serializes {@link LongOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(LongOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.write(NULL_HEADER);
        } else {
            output.write(NON_NULL_HEADER);
            output.writeLong(option.get());
        }
    }

    /**
     * Deserializes {@link LongOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(LongOption option, DataInput input) throws IOException {
        byte header = input.readByte();
        if (header == NULL_HEADER) {
            option.setNull();
        } else {
            option.modify(input.readLong());
        }
    }

    /**
     * Serializes {@link FloatOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(FloatOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.write(NULL_HEADER);
        } else {
            output.write(NON_NULL_HEADER);
            output.writeFloat(option.get());
        }
    }

    /**
     * Deserializes {@link FloatOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(FloatOption option, DataInput input) throws IOException {
        byte header = input.readByte();
        if (header == NULL_HEADER) {
            option.setNull();
        } else {
            option.modify(input.readFloat());
        }
    }

    /**
     * Serializes {@link DoubleOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(DoubleOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.write(NULL_HEADER);
        } else {
            output.write(NON_NULL_HEADER);
            output.writeDouble(option.get());
        }
    }

    /**
     * Deserializes {@link DoubleOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(DoubleOption option, DataInput input) throws IOException {
        byte header = input.readByte();
        if (header == NULL_HEADER) {
            option.setNull();
        } else {
            option.modify(input.readDouble());
        }
    }

    /**
     * Serializes {@link DateOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(DateOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.writeInt(UNSIGNED_NULL);
        } else {
            int value = option.get().getElapsedDays();
            output.writeInt(value < 0 ? 0 : value);
        }
    }

    /**
     * Deserializes {@link DateOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(DateOption option, DataInput input) throws IOException {
        int value = input.readInt();
        if (value <= UNSIGNED_NULL) {
            option.setNull();
        } else {
            option.modify(value);
        }
    }

    /**
     * Serializes {@link DateTimeOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(DateTimeOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.writeLong(UNSIGNED_NULL);
        } else {
            long value = option.get().getElapsedSeconds();
            output.writeLong(value < 0 ? 0 : value);
        }
    }

    /**
     * Deserializes {@link DateTimeOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(DateTimeOption option, DataInput input) throws IOException {
        long value = input.readLong();
        if (value == UNSIGNED_NULL) {
            option.setNull();
        } else {
            option.modify(value);
        }
    }

    /**
     * Serializes {@link StringOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(StringOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            writeCompactInt(UNSIGNED_NULL, output);
        } else {
            Text entity = option.get();
            int length = entity.getLength();
            writeCompactInt(length, output);
            output.write(entity.getBytes(), 0, length);
        }
    }

    /**
     * Deserializes {@link StringOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(StringOption option, DataInput input) throws IOException {
        int length = readCompactInt(input);
        if (length == UNSIGNED_NULL) {
            option.setNull();
        } else {
            if (option.isNull() == false) {
                Text entity = option.get();
                if (length == entity.getLength()) {
                    // optimize for same-length text properties
                    input.readFully(entity.getBytes(), 0, length);
                    return;
                }
            } else {
                // set as non-null
                option.reset();
            }
            byte[] buffer = getLocalBuffer(length, Integer.MAX_VALUE);
            input.readFully(buffer, 0, length);
            option.modify(buffer, 0, length);
        }
    }

    private static final byte DECIMAL_NULL = 0;

    private static final byte DECIMAL_PRESENT_MASK = 1 << 0;

    private static final byte DECIMAL_PLUS_MASK = 1 << 1;

    private static final byte DECIMAL_COMPACT_MASK = 1 << 2;

    /**
     * Serializes {@link DecimalOption} object.
     * @param option the target object
     * @param output the target output
     * @throws IOException if I/O error was occurred while serializing the object
     */
    public static void serialize(DecimalOption option, DataOutput output) throws IOException {
        if (option.isNull()) {
            output.writeByte(DECIMAL_NULL);
        } else {
            BigDecimal decimal = option.get();
            BigInteger unscaled = decimal.unscaledValue();
            int signum = unscaled.signum();
            BigInteger unsigned = unscaled.abs();
            unscaled = null;

            int bits = unsigned.bitLength();
            if (bits <= Long.SIZE - 1) {
                output.writeByte(DECIMAL_PRESENT_MASK
                        | (signum >= 0 ? DECIMAL_PLUS_MASK : 0)
                        | DECIMAL_COMPACT_MASK);
                writeCompactInt(decimal.scale(), output);
                writeCompactLong(unsigned.longValueExact(), output);
            } else {
                output.writeByte(DECIMAL_PRESENT_MASK
                        | (signum >= 0 ? DECIMAL_PLUS_MASK : 0));
                writeCompactInt(decimal.scale(), output);
                byte[] bytes = unsigned.toByteArray();
                assert bytes.length != 0;
                writeCompactInt(bytes.length, output);
                output.write(bytes);
            }
        }
    }

    /**
     * Deserializes {@link DecimalOption} object.
     * @param option the target object
     * @param input the source input
     * @throws IOException if I/O error was occurred while deserializing the object
     */
    public static void deserialize(DecimalOption option, DataInput input) throws IOException {
        byte head = input.readByte();
        if (head == DECIMAL_NULL) {
            option.setNull();
            return;
        }
        boolean compact = (head & DECIMAL_COMPACT_MASK) != 0;
        boolean plus = (head & DECIMAL_PLUS_MASK) != 0;
        int scale = readCompactInt(input);
        if (compact) {
            long unscaled = readCompactLong(input);
            assert unscaled >= 0;
            option.modify(BigDecimal.valueOf(plus ? unscaled : -unscaled, scale));
        } else {
            int length = readCompactInt(input);
            assert length != 0; // '0' must be compact form
            byte[] buffer = getLocalBuffer(length, length * 4);
            int offset = buffer.length - length;
            input.readFully(buffer, offset, length);
            if (offset > 0) {
                Arrays.fill(buffer, 0, offset, (byte) 0);
            }
            option.modify(new BigDecimal(new BigInteger(plus ? +1 : -1, buffer), scale));
        }
    }

    static final byte COMPACT_INT_HEAD_MIN = Byte.MIN_VALUE + 4;

    static int readCompactInt(DataInput input) throws IOException {
        byte b0 = input.readByte();
        if (b0 >= COMPACT_INT_HEAD_MIN) {
            return b0;
        }
        int scale = COMPACT_INT_HEAD_MIN - b0;
        assert 1 <= scale && scale <= 4;
        switch (scale) {
        case 1:
            return input.readByte();
        case 2:
            return input.readShort();
        case 3:
            return input.readInt();
        case 4:
            throw new NumberFormatException();
        default:
            throw new AssertionError();
        }
    }

    static void writeCompactInt(int value, DataOutput output) throws IOException {
        if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
            if (value >= COMPACT_INT_HEAD_MIN) {
                output.writeByte(value);
            } else {
                output.writeByte(COMPACT_INT_HEAD_MIN - 1);
                output.writeByte(value);
            }
        } else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
            output.writeByte(COMPACT_INT_HEAD_MIN - 2);
            output.writeShort(value);
        } else {
            output.writeByte(COMPACT_INT_HEAD_MIN - 3);
            output.writeInt(value);
        }
    }

    static long readCompactLong(DataInput input) throws IOException {
        byte b0 = input.readByte();
        if (b0 >= COMPACT_INT_HEAD_MIN) {
            return b0;
        }
        int scale = COMPACT_INT_HEAD_MIN - b0;
        assert 1 <= scale && scale <= 4;
        switch (scale) {
        case 1:
            return input.readByte();
        case 2:
            return input.readShort();
        case 3:
            return input.readInt();
        case 4:
            return input.readLong();
        default:
            throw new AssertionError();
        }
    }

    static void writeCompactLong(long value, DataOutput output) throws IOException {
        if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
            if (value >= COMPACT_INT_HEAD_MIN) {
                output.writeByte((int) value);
            } else {
                output.writeByte(COMPACT_INT_HEAD_MIN - 1);
                output.writeByte((int) value);
            }
        } else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
            output.writeByte(COMPACT_INT_HEAD_MIN - 2);
            output.writeShort((int) value);
        } else if (Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE) {
            output.writeByte(COMPACT_INT_HEAD_MIN - 3);
            output.writeInt((int) value);
        } else {
            output.writeByte(COMPACT_INT_HEAD_MIN - 4);
            output.writeLong(value);
        }
    }

    private static final ThreadLocal<byte[]> BUFFERS = ThreadLocal.withInitial(() -> new byte[256]);
    private static byte[] getLocalBuffer(int minSize, int maxSize) {
        byte[] buffer = BUFFERS.get();
        if (buffer.length < minSize || buffer.length > maxSize) {
            buffer = new byte[minSize];
            BUFFERS.set(buffer);
        }
        return buffer;
    }

    private interface SerDe {
        void serialize(ValueOption<?> value, DataOutput output) throws IOException;
        void deserialize(ValueOption<?> value, DataInput input) throws IOException;
    }

    @FunctionalInterface
    private interface IoAction<V, T> {
        void perform(V value, T target) throws IOException;
    }
}
