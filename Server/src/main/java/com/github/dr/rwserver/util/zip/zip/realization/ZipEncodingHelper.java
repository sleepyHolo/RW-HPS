package com.github.dr.rwserver.util.zip.zip.realization;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Static helper functions for robustly encoding filenames in zip files.
 * @author Apache
 */
public abstract class ZipEncodingHelper {

    /**
     * A class, which holds the high characters of a simple encoding
     * and lazily instantiates a Simple8BitZipEncoding instance in a
     * thread-safe manner.
     */
    private static class SimpleEncodingHolder {

        private final char[] highChars;
        private Simple8BitZipEncoding encoding;

        /**
         * Instantiate a simple encoding holder.
         *
         * @param highChars The characters for byte codes 128 to 255.
         *
         * @see Simple8BitZipEncoding#Simple8BitZipEncoding(char[])
         */
        SimpleEncodingHolder(final char[] highChars) {
            this.highChars = highChars;
        }

        /**
         * @return The associated {@link Simple8BitZipEncoding}, which
         *         is instantiated if not done so far.
         */
        public synchronized Simple8BitZipEncoding getEncoding() {
            if (this.encoding == null) {
                this.encoding = new Simple8BitZipEncoding(this.highChars);
            }
            return this.encoding;
        }
    }

    private static final Map<String, SimpleEncodingHolder> simpleEncodings;

    static {
        final Map<String, SimpleEncodingHolder> se = new HashMap<>();

        final char[] cp437_high_chars =
            new char[] {0x00c7, 0x00fc, 0x00e9, 0x00e2, 0x00e4, 0x00e0,
                        0x00e5, 0x00e7, 0x00ea, 0x00eb, 0x00e8, 0x00ef,
                        0x00ee, 0x00ec, 0x00c4, 0x00c5, 0x00c9, 0x00e6,
                        0x00c6, 0x00f4, 0x00f6, 0x00f2, 0x00fb, 0x00f9,
                        0x00ff, 0x00d6, 0x00dc, 0x00a2, 0x00a3, 0x00a5,
                        0x20a7, 0x0192, 0x00e1, 0x00ed, 0x00f3, 0x00fa,
                        0x00f1, 0x00d1, 0x00aa, 0x00ba, 0x00bf, 0x2310,
                        0x00ac, 0x00bd, 0x00bc, 0x00a1, 0x00ab, 0x00bb,
                        0x2591, 0x2592, 0x2593, 0x2502, 0x2524, 0x2561,
                        0x2562, 0x2556, 0x2555, 0x2563, 0x2551, 0x2557,
                        0x255d, 0x255c, 0x255b, 0x2510, 0x2514, 0x2534,
                        0x252c, 0x251c, 0x2500, 0x253c, 0x255e, 0x255f,
                        0x255a, 0x2554, 0x2569, 0x2566, 0x2560, 0x2550,
                        0x256c, 0x2567, 0x2568, 0x2564, 0x2565, 0x2559,
                        0x2558, 0x2552, 0x2553, 0x256b, 0x256a, 0x2518,
                        0x250c, 0x2588, 0x2584, 0x258c, 0x2590, 0x2580,
                        0x03b1, 0x00df, 0x0393, 0x03c0, 0x03a3, 0x03c3,
                        0x00b5, 0x03c4, 0x03a6, 0x0398, 0x03a9, 0x03b4,
                        0x221e, 0x03c6, 0x03b5, 0x2229, 0x2261, 0x00b1,
                        0x2265, 0x2264, 0x2320, 0x2321, 0x00f7, 0x2248,
                        0x00b0, 0x2219, 0x00b7, 0x221a, 0x207f, 0x00b2,
                        0x25a0, 0x00a0};

        final SimpleEncodingHolder cp437 = new SimpleEncodingHolder(cp437_high_chars);

        se.put("CP437", cp437);
        se.put("Cp437", cp437);
        se.put("cp437", cp437);
        se.put("IBM437", cp437);
        se.put("ibm437", cp437);

        final char[] cp850_high_chars =
            new char[] {0x00c7, 0x00fc, 0x00e9, 0x00e2, 0x00e4, 0x00e0,
                        0x00e5, 0x00e7, 0x00ea, 0x00eb, 0x00e8, 0x00ef,
                        0x00ee, 0x00ec, 0x00c4, 0x00c5, 0x00c9, 0x00e6,
                        0x00c6, 0x00f4, 0x00f6, 0x00f2, 0x00fb, 0x00f9,
                        0x00ff, 0x00d6, 0x00dc, 0x00f8, 0x00a3, 0x00d8,
                        0x00d7, 0x0192, 0x00e1, 0x00ed, 0x00f3, 0x00fa,
                        0x00f1, 0x00d1, 0x00aa, 0x00ba, 0x00bf, 0x00ae,
                        0x00ac, 0x00bd, 0x00bc, 0x00a1, 0x00ab, 0x00bb,
                        0x2591, 0x2592, 0x2593, 0x2502, 0x2524, 0x00c1,
                        0x00c2, 0x00c0, 0x00a9, 0x2563, 0x2551, 0x2557,
                        0x255d, 0x00a2, 0x00a5, 0x2510, 0x2514, 0x2534,
                        0x252c, 0x251c, 0x2500, 0x253c, 0x00e3, 0x00c3,
                        0x255a, 0x2554, 0x2569, 0x2566, 0x2560, 0x2550,
                        0x256c, 0x00a4, 0x00f0, 0x00d0, 0x00ca, 0x00cb,
                        0x00c8, 0x0131, 0x00cd, 0x00ce, 0x00cf, 0x2518,
                        0x250c, 0x2588, 0x2584, 0x00a6, 0x00cc, 0x2580,
                        0x00d3, 0x00df, 0x00d4, 0x00d2, 0x00f5, 0x00d5,
                        0x00b5, 0x00fe, 0x00de, 0x00da, 0x00db, 0x00d9,
                        0x00fd, 0x00dd, 0x00af, 0x00b4, 0x00ad, 0x00b1,
                        0x2017, 0x00be, 0x00b6, 0x00a7, 0x00f7, 0x00b8,
                        0x00b0, 0x00a8, 0x00b7, 0x00b9, 0x00b3, 0x00b2,
                        0x25a0, 0x00a0};

        final SimpleEncodingHolder cp850 = new SimpleEncodingHolder(cp850_high_chars);

        se.put("CP850", cp850);
        se.put("Cp850", cp850);
        se.put("cp850", cp850);
        se.put("IBM850", cp850);
        se.put("ibm850", cp850);
        simpleEncodings = Collections.unmodifiableMap(se);
    }

    /**
     * Grow a byte buffer, so it has a minimal capacity or at least
     * the double capacity of the original buffer
     *
     * @param b The original buffer.
     * @param newCapacity The minimal requested new capacity.
     * @return A byte buffer <code>r</code> with
     *         <code>r.capacity() = max(b.capacity() * 2,newCapacity)</code> and
     *         all the data contained in <code>b</code> copied to the beginning
     *         of <code>r</code>.
     *
     */
    static ByteBuffer growBuffer(final ByteBuffer b, final int newCapacity) {
    	prepareBufferForRead(b);
        final int c2 = b.capacity() * 2;
        final ByteBuffer on = ByteBuffer.allocate(Math.max(c2, newCapacity));

        on.put(b);
        return on;
    }
    
    /**
     * Prepares a buffer to be read after writing.
     * 
     * @param b The buffer
     */
    static void prepareBufferForRead(final Buffer b) {
    	// ByteBuffer has overridden methods in java 11 but not in java 8 
    	// so the Buffer is significant to get java 8 compatible classes
    	b.limit(b.position());
    	b.rewind();
    }


    /**
     * The hexadecimal digits <code>0,...,9,A,...,F</code> encoded as
     * ASCII bytes.
     */
    private static final byte[] HEX_DIGITS = new byte[] {
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41,
        0x42, 0x43, 0x44, 0x45, 0x46
    };

    /**
     * Append <code>%Uxxxx</code> to the given byte buffer.
     * The caller must assure, that <code>bb.remaining()&gt;=6</code>.
     *
     * @param bb The byte buffer to write to.
     * @param c The character to write.
     */
    static void appendSurrogate(final ByteBuffer bb, final char c) {

        bb.put((byte) '%');
        bb.put((byte) 'U');

        bb.put(HEX_DIGITS[(c >> 12) & 0x0f]);
        bb.put(HEX_DIGITS[(c >> 8) & 0x0f]);
        bb.put(HEX_DIGITS[(c >> 4) & 0x0f]);
        bb.put(HEX_DIGITS[c & 0x0f]);
    }


    /**
     * name of the encoding UTF-8
     */
    static final String UTF8 = "UTF8";

    /**
     * variant name of the encoding UTF-8 used for comparisons.
     */
    private static final String UTF_DASH_8 = "utf-8";

    /**
     * name of the encoding UTF-8
     */
    static final ZipEncoding UTF8_ZIP_ENCODING = new FallbackZipEncoding(UTF8);

    /**
     * Instantiates a zip encoding.
     *
     * @param name The name of the zip encoding. Specify {@code null} for
     *             the platform's default encoding.
     * @return A zip encoding for the given encoding name.
     */
    public static ZipEncoding getZipEncoding(final String name) {

        // fallback encoding is good enough for utf-8.
        if (isUTF8(name)) {
            return UTF8_ZIP_ENCODING;
        }

        if (name == null) {
            return new FallbackZipEncoding();
        }

        final SimpleEncodingHolder h = simpleEncodings.get(name);

        if (h != null) {
            return h.getEncoding();
        }

        try {

            final Charset cs = Charset.forName(name);
            return new NioZipEncoding(cs);

        } catch (final UnsupportedCharsetException e) {
            return new FallbackZipEncoding(name);
        }
    }

    /**
     * Whether a given encoding - or the platform's default encoding
     * if the parameter is null - is UTF-8.
     */
    static boolean isUTF8(String encoding) {
        if (encoding == null) {
            // check platform's default encoding
            encoding = System.getProperty("file.encoding");
        }
        return UTF8.equalsIgnoreCase(encoding)
            || UTF_DASH_8.equalsIgnoreCase(encoding);
    }
}
