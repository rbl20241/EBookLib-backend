package rb.ebooklib.ebooks.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public abstract class ProxyInputStream extends FilterInputStream {

    ProxyInputStream(InputStream proxy) {
        super(proxy);
        // the proxy is stored in a protected superclass variable named 'in'
    }

    @Override
    public int read() throws IOException {
        try {
            beforeRead(1);
            int b = in.read();
            afterRead(b != -1 ? 1 : -1);
            return b;
        } catch (IOException e) {
            handleIOException(e);
            return -1;
        }
    }

    @Override
    public int read(byte[] bts) throws IOException {
        try {
            beforeRead(bts != null ? bts.length : 0);
            int n = in.read(Objects.requireNonNull(bts));
            afterRead(n);
            return n;
        } catch (IOException e) {
            handleIOException(e);
            return -1;
        }
    }

    @Override
    public int read(byte[] bts, int off, int len) throws IOException {
        try {
            beforeRead(len);
            int n = in.read(bts, off, len);
            afterRead(n);
            return n;
        } catch (IOException e) {
            handleIOException(e);
            return -1;
        }
    }

    @Override
    public long skip(long ln) throws IOException {
        try {
            return in.skip(ln);
        } catch (IOException e) {
            handleIOException(e);
            return 0;
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return super.available();
        } catch (IOException e) {
            handleIOException(e);
            return 0;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            in.close();
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        try {
            in.reset();
        } catch (IOException e) {
            handleIOException(e);
        }
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    protected void beforeRead(int n) {
    }

    protected void afterRead(int n) {
    }

    private void handleIOException(IOException e) throws IOException {
        throw e;
    }

}