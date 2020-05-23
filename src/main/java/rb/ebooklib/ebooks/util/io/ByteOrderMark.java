package rb.ebooklib.ebooks.util.io;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: rene
 * Date: 17-3-14
 * Time: 22:10
 * To change this template use File | Settings | File Templates.
 */
public class ByteOrderMark implements Serializable {

    private static final long serialVersionUID = 1L;

    /** UTF-8 BOM */
    public static final ByteOrderMark UTF_8    = new ByteOrderMark("UTF-8",    0xEF, 0xBB, 0xBF);
    /** UTF-16BE BOM (Big Endian) */
    public static final ByteOrderMark UTF_16BE = new ByteOrderMark("UTF-16BE", 0xFE, 0xFF);
    /** UTF-16LE BOM (Little Endian) */
    public static final ByteOrderMark UTF_16LE = new ByteOrderMark("UTF-16LE", 0xFF, 0xFE);

    private final String charsetName;
    private final int[] bytes;

    /**
     * Construct a new BOM.
     *
     * @param charsetName The name of the charset the BOM represents
     * @param bytes The BOM's bytes
     * @throws IllegalArgumentException if the charsetName is null or
     * zero length
     * @throws IllegalArgumentException if the bytes are null or zero
     * length
     */
    public ByteOrderMark(String charsetName, int... bytes) {
        if (charsetName == null || charsetName.length() == 0) {
            throw new IllegalArgumentException("No charsetName specified");
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("No bytes specified");
        }
        this.charsetName = charsetName;
        this.bytes = new int[bytes.length];
        System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
    }

    /**
     * Return the name of the {@link java.nio.charset.Charset} the BOM represents.
     *
     * @return the character set name
     */
    public String getCharsetName() {
        return charsetName;
    }

    /**
     * Return the length of the BOM's bytes.
     *
     * @return the length of the BOM's bytes
     */
    public int length() {
        return bytes.length;
    }

    /**
     * The byte at the specified position.
     *
     * @param pos The position
     * @return The specified byte
     */
    public int get(int pos) {
        return bytes[pos];
    }

    /**
     * Indicates if this BOM's bytes equals another.
     *
     * @param obj The object to compare to
     * @return true if the bom's bytes are equal, otherwise
     * false
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ByteOrderMark)) {
            return false;
        }
        ByteOrderMark bom = (ByteOrderMark)obj;
        if (bytes.length != bom.length()) {
            return false;
        }
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != bom.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the hashcode for this BOM.
     *
     * @return the hashcode for this BOM.
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = getClass().hashCode();
        for (int b : bytes) {
            hashCode += b;
        }
        return hashCode;
    }

    /**
     * Provide a String representation of the BOM.
     *
     * @return the length of the BOM's bytes
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append('[');
        builder.append(charsetName);
        builder.append(": ");
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append("0x");
            builder.append(Integer.toHexString(0xFF & bytes[i]).toUpperCase());
        }
        builder.append(']');
        return builder.toString();
    }

}