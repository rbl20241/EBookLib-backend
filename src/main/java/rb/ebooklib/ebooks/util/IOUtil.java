package rb.ebooklib.ebooks.util;

import java.io.*;

public class IOUtil {
    private static final int IO_COPY_BUFFER_SIZE = 1024 * 4;

    public static byte[] toByteArray(Reader in, String encoding) throws IOException {
        StringWriter out = new StringWriter();
        copy(in, out);
        out.flush();
        return out.toString().getBytes(encoding);
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        copy(in, result);
        result.flush();
        return result.toByteArray();
    }

    public static byte[] toByteArray(InputStream in, int size ) throws IOException {
        try {
            ByteArrayOutputStream result;

            if ( size > 0 ) {
                result = new ByteArrayOutputStream(size);
            } else {
                result = new ByteArrayOutputStream();
            }

            copy(in, result);
            result.flush();
            return result.toByteArray();
        } catch ( OutOfMemoryError error ) {
            //Return null so it gets loaded lazily.
            return null;
        }

    }

    private static int calcNewNrReadSize(int nrRead, int totalNrNread) {
        if (totalNrNread < 0) {
            return totalNrNread;
        }
        if (totalNrNread > (Integer.MAX_VALUE - nrRead)) {
            return -1;
        } else {
            return (totalNrNread + nrRead);
        }
    }

    private static int copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[IO_COPY_BUFFER_SIZE];
        int readSize;
        int result = 0;
        while ((readSize = in.read(buffer)) >= 0) {
            out.write(buffer, 0, readSize);
            result = calcNewNrReadSize(readSize, result);
        }
        out.flush();
        return result;
    }

    private static int copy(Reader in, Writer out) throws IOException {
        char[] buffer = new char[IO_COPY_BUFFER_SIZE];
        int readSize;
        int result = 0;
        while ((readSize = in.read(buffer)) >= 0) {
            out.write(buffer, 0, readSize);
            result = calcNewNrReadSize(readSize, result);
        }
        out.flush();
        return result;
    }}
