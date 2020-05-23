package rb.ebooklib.ebooks.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class BOMInputStream extends ProxyInputStream {
    private final boolean include;
    private final List<ByteOrderMark> boms;
    private ByteOrderMark byteOrderMark;
    private int[] firstBytes;
    private int fbLength;
    private int fbIndex;
    private int markFbIndex;
    private boolean markedAtStart;

    public BOMInputStream(final InputStream delegate, final boolean include, final ByteOrderMark... boms) {
        super(delegate);
        if (boms == null || boms.length == 0) {
            throw new IllegalArgumentException("No BOMs specified");
        }
        this.include = include;
        this.boms = Arrays.asList(boms);
    }

    private ByteOrderMark getBOM() throws IOException {
        if (firstBytes == null) {
            int max = 0;
            for (ByteOrderMark bom : boms) {
                max = Math.max(max, bom.length());
            }
            firstBytes = new int[max];
            for (int i = 0; i < firstBytes.length; i++) {
                firstBytes[i] = in.read();
                fbLength++;
                if (firstBytes[i] < 0) {
                    break;
                }

                byteOrderMark = find();
                if (byteOrderMark != null) {
                    if (!include) {
                        fbLength = 0;
                    }
                    break;
                }
            }
        }
        return byteOrderMark;
    }

    public String getBOMCharsetName() throws IOException {
        getBOM();
        return (byteOrderMark == null ? null : byteOrderMark.getCharsetName());
    }

    private int readFirstBytes() throws IOException {
        getBOM();
        return (fbIndex < fbLength) ? firstBytes[fbIndex++] : -1;
    }

    private ByteOrderMark find() {
        for (ByteOrderMark bom : boms) {
            if (matches(bom)) {
                return bom;
            }
        }
        return null;
    }

    private boolean matches(final ByteOrderMark bom) {
        if (bom.length() != fbLength) {
            return false;
        }
        for (int i = 0; i < bom.length(); i++) {
            if (bom.get(i) != firstBytes[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int read() throws IOException {
        int b = readFirstBytes();
        return (b >= 0) ? b : in.read();
    }

    @Override
    public int read(final byte[] buf, int off, int len) throws IOException {
        int firstCount = 0;
        int b = 0;
        while ((len > 0) && (b >= 0)) {
            b = readFirstBytes();
            if (b >= 0) {
                buf[off++] = (byte) (b & 0xFF);
                len--;
                firstCount++;
            }
        }
        int secondCount = in.read(buf, off, len);
        return (secondCount < 0) ? (firstCount > 0 ? firstCount : -1) : firstCount + secondCount;
    }

    @Override
    public int read(final byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public synchronized void mark(final int readlimit) {
        markFbIndex = fbIndex;
        markedAtStart = (firstBytes == null);
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        fbIndex = markFbIndex;
        if (markedAtStart) {
            firstBytes = null;
        }

        in.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        while ((n > 0) && (readFirstBytes() >= 0)) {
            n--;
        }
        return in.skip(n);
    }
}
