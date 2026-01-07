package top.mddata.sdk.core.sign;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @author runzhi
 */
public class StreamUtil {
    /** 默认缓存区 */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     *
     * 写入流
     * @param in 输入
     * @param out 输出
     * @throws IOException IO异常
     */
    public static void io(InputStream in, OutputStream out) throws IOException {
        io(in, out, -1);
    }

    /**
     * 写入流
     * @param in 输入
     * @param out 输出
     * @param bufferSize 缓存区
     * @throws IOException IO异常
     */
    public static void io(InputStream in, OutputStream out, int bufferSize) throws IOException {
        if (bufferSize == -1) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }

        byte[] buffer = new byte[bufferSize];
        int amount;

        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }

    /**
     * 写入流
     * @param in 输入
     * @param out 输出
     * @throws IOException IO异常
     */
    public static void io(Reader in, Writer out) throws IOException {
        io(in, out, -1);
    }

    /**
     * 写入流
     * @param in 输入
     * @param out 输出
     * @param bufferSize 缓存区
     * @throws IOException IO异常
     */
    public static void io(Reader in, Writer out, int bufferSize) throws IOException {
        if (bufferSize == -1) {
            bufferSize = DEFAULT_BUFFER_SIZE >> 1;
        }

        char[] buffer = new char[bufferSize];
        int amount;

        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }

    public static OutputStream synchronizedOutputStream(OutputStream out) {
        return new SynchronizedOutputStream(out);
    }

    public static OutputStream synchronizedOutputStream(OutputStream out, Object lock) {
        return new SynchronizedOutputStream(out, lock);
    }

    /**
     * 将流转为字符串
     * @param in 输入流
     * @return 字符串
     * @throws IOException 流异常
     */
    public static String readText(InputStream in) throws IOException {
        return readText(in, null, -1);
    }

    /**
     *
     * 将流转为字符串
     * @param in 输入流
     * @param encoding 字符编码
     * @return 字符串
     * @throws IOException 流异常
     */
    public static String readText(InputStream in, String encoding) throws IOException {
        return readText(in, encoding, -1);
    }

    /**
     *
     * 将流转为字符串
     * @param in 输入流
     * @param encoding 字符编码
     * @param bufferSize 缓冲区
     * @return 字符串
     * @throws IOException 流异常
     */
    public static String readText(InputStream in, String encoding, int bufferSize)
            throws IOException {
        Reader reader = (encoding == null) ? new InputStreamReader(in) : new InputStreamReader(in,
                encoding);

        return readText(reader, bufferSize);
    }

    public static String readText(Reader reader) throws IOException {
        return readText(reader, -1);
    }

    /**
     * 将流转为字符串
     * @param reader 输入流
     * @param bufferSize 缓冲区
     * @return 字符串
     * @throws IOException 流异常
     */
    public static String readText(Reader reader, int bufferSize) throws IOException {
        StringWriter writer = new StringWriter();

        io(reader, writer, bufferSize);
        return writer.toString();
    }

    private static class SynchronizedOutputStream extends OutputStream {
        private OutputStream out;
        private Object lock;

        SynchronizedOutputStream(OutputStream out) {
            this(out, out);
        }

        SynchronizedOutputStream(OutputStream out, Object lock) {
            this.out = out;
            this.lock = lock;
        }

        @Override
        public void write(int datum) throws IOException {
            synchronized (lock) {
                out.write(datum);
            }
        }

        @Override
        public void write(byte[] data) throws IOException {
            synchronized (lock) {
                out.write(data);
            }
        }

        @Override
        public void write(byte[] data, int offset, int length) throws IOException {
            synchronized (lock) {
                out.write(data, offset, length);
            }
        }

        @Override
        public void flush() throws IOException {
            synchronized (lock) {
                out.flush();
            }
        }

        @Override
        public void close() throws IOException {
            synchronized (lock) {
                out.close();
            }
        }
    }
}
