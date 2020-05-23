package rb.ebooklib.ebooks.util.io;

import java.io.*;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlStreamReader extends Reader {
    private static final int BUFFER_SIZE = 4096;

    private static final String UTF_8 = "UTF-8";

    private static final String US_ASCII = "US-ASCII";

    private static final String UTF_16BE = "UTF-16BE";

    private static final String UTF_16LE = "UTF-16LE";

    private static final String UTF_16 = "UTF-16";

    private static final String EBCDIC = "CP1047";

    private static final ByteOrderMark[] BOMS = new ByteOrderMark[] {
            ByteOrderMark.UTF_8,
            ByteOrderMark.UTF_16BE,
            ByteOrderMark.UTF_16LE
    };
    private static final ByteOrderMark[] XML_GUESS_BYTES = new ByteOrderMark[] {
            new ByteOrderMark(UTF_8,    0x3C, 0x3F, 0x78, 0x6D),
            new ByteOrderMark(UTF_16BE, 0x00, 0x3C, 0x00, 0x3F),
            new ByteOrderMark(UTF_16LE, 0x3C, 0x00, 0x3F, 0x00),
            new ByteOrderMark(EBCDIC,   0x4C, 0x6F, 0xA7, 0x94)
    };


    private final Reader reader;

    private final String encoding;

    private final String defaultEncoding;

    public XmlStreamReader(InputStream is, String httpContentType)
            throws IOException {
        this(is, httpContentType, true);
    }

    private XmlStreamReader(InputStream is, String httpContentType,
                            boolean lenient, String defaultEncoding) throws IOException {
        this.defaultEncoding = defaultEncoding;
        BOMInputStream bom = new BOMInputStream(new BufferedInputStream(is, BUFFER_SIZE), false, BOMS);
        BOMInputStream pis = new BOMInputStream(bom, true, XML_GUESS_BYTES);
        this.encoding = doHttpStream(bom, pis, httpContentType, lenient);
        this.reader = new InputStreamReader(pis, encoding);
    }

    private XmlStreamReader(InputStream is, String httpContentType,
                            @SuppressWarnings("SameParameterValue") boolean lenient) throws IOException {
        this(is, httpContentType, lenient, null);
    }

    @Override
    public int read(char[] buf, int offset, int len) throws IOException {
        return reader.read(buf, offset, len);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private String doHttpStream(BOMInputStream bom, BOMInputStream pis, String httpContentType,
                                boolean lenient) throws IOException {
        String bomEnc      = bom.getBOMCharsetName();
        String xmlGuessEnc = pis.getBOMCharsetName();
        String xmlEnc = getXmlProlog(pis, xmlGuessEnc);
        try {
            return calculateHttpEncoding(httpContentType, bomEnc,
                    xmlGuessEnc, xmlEnc, lenient);
        } catch (XmlStreamReaderException ex) {
            if (lenient) {
                return doLenientDetection(httpContentType, ex);
            } else {
                throw ex;
            }
        }
    }

    private String doLenientDetection(String httpContentType,
                                      XmlStreamReaderException ex) throws IOException {
        if (httpContentType != null && httpContentType.startsWith("text/html")) {
            httpContentType = httpContentType.substring("text/html".length());
            httpContentType = "text/xml" + httpContentType;
            try {
                return calculateHttpEncoding(httpContentType, ex.getBomEncoding(),
                        ex.getXmlGuessEncoding(), ex.getXmlEncoding(), true);
            } catch (XmlStreamReaderException ex2) {
                ex = ex2;
            }
        }
        String encoding = ex.getXmlEncoding();
        if (encoding == null) {
            encoding = ex.getContentTypeEncoding();
        }
        if (encoding == null) {
            encoding = (defaultEncoding == null) ? UTF_8 : defaultEncoding;
        }
        return encoding;
    }

    private String calculateRawEncoding(String bomEnc, String xmlGuessEnc,
                                        String xmlEnc) throws IOException {
        // BOM is Null
        if (bomEnc == null) {
            if (xmlGuessEnc == null || xmlEnc == null) {
                return (defaultEncoding == null ? UTF_8 : defaultEncoding);
            }
            if (xmlEnc.equals(UTF_16) &&
                    (xmlGuessEnc.equals(UTF_16BE) || xmlGuessEnc.equals(UTF_16LE))) {
                return xmlGuessEnc;
            }
            return xmlEnc;
        }

        // BOM is UTF-8
        if (bomEnc.equals(UTF_8)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(UTF_8)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_8)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return bomEnc;
        }

        // BOM is UTF-16BE or UTF-16LE
        if (bomEnc.equals(UTF_16BE) || bomEnc.equals(UTF_16LE)) {
            if (xmlGuessEnc != null && !xmlGuessEnc.equals(bomEnc)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            if (xmlEnc != null && !xmlEnc.equals(UTF_16) && !xmlEnc.equals(bomEnc)) {
                String msg = MessageFormat.format(RAW_EX_1, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return bomEnc;
        }

        // BOM is something else
        String msg = MessageFormat.format(RAW_EX_2, bomEnc, xmlGuessEnc, xmlEnc);
        throw new XmlStreamReaderException(msg, bomEnc, xmlGuessEnc, xmlEnc);
    }


    private String calculateHttpEncoding(String httpContentType,
                                         String bomEnc, String xmlGuessEnc, String xmlEnc,
                                         boolean lenient) throws IOException {

        // Lenient and has XML encoding
        if (lenient && xmlEnc != null) {
            return xmlEnc;
        }

        // Determine mime/encoding content types from HTTP Content Type
        String cTMime = getContentTypeMime(httpContentType);
        String cTEnc  = getContentTypeEncoding(httpContentType);
        boolean appXml  = isAppXml(cTMime);
        boolean textXml = isTextXml(cTMime);

        // Mime type NOT "application/xml" or "text/xml"
        if (!appXml && !textXml) {
            String msg = MessageFormat.format(HTTP_EX_3, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            throw new XmlStreamReaderException(msg, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
        }

        // No content type encoding
        if (cTEnc == null) {
            if (appXml) {
                return calculateRawEncoding(bomEnc, xmlGuessEnc, xmlEnc);
            } else {
                return (defaultEncoding == null) ? US_ASCII : defaultEncoding;
            }
        }

        // UTF-16BE or UTF-16LE content type encoding
        if (cTEnc.equals(UTF_16BE) || cTEnc.equals(UTF_16LE)) {
            if (bomEnc != null) {
                String msg = MessageFormat.format(HTTP_EX_1, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
                throw new XmlStreamReaderException(msg, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            }
            return cTEnc;
        }

        // UTF-16 content type encoding
        if (cTEnc.equals(UTF_16)) {
            if (bomEnc != null && bomEnc.startsWith(UTF_16)) {
                return bomEnc;
            }
            String msg = MessageFormat.format(HTTP_EX_2, cTMime, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
            throw new XmlStreamReaderException(msg, cTEnc, bomEnc, xmlGuessEnc, xmlEnc);
        }

        return cTEnc;
    }

    private static String getContentTypeMime(String httpContentType) {
        String mime = null;
        if (httpContentType != null) {
            int i = httpContentType.indexOf(";");
            if (i >= 0) {
                mime = httpContentType.substring(0, i);
            } else {
                mime = httpContentType;
            }
            mime = mime.trim();
        }
        return mime;
    }

    private static final Pattern CHARSET_PATTERN = Pattern
            .compile("charset=[\"']?([.[^; \"']]*)[\"']?");

    private static String getContentTypeEncoding(String httpContentType) {
        String encoding = null;
        if (httpContentType != null) {
            int i = httpContentType.indexOf(";");
            if (i > -1) {
                String postMime = httpContentType.substring(i + 1);
                Matcher m = CHARSET_PATTERN.matcher(postMime);
                encoding = (m.find()) ? m.group(1) : null;
                encoding = (encoding != null) ? encoding.toUpperCase() : null;
            }
        }
        return encoding;
    }

    private static final Pattern ENCODING_PATTERN = Pattern.compile(
            "<\\?xml.*encoding[\\s]*=[\\s]*((?:\".[^\"]*\")|(?:'.[^']*'))",
            Pattern.MULTILINE);

    private static String getXmlProlog(InputStream is, String guessedEnc)
            throws IOException {
        String encoding = null;
        if (guessedEnc != null) {
            byte[] bytes = new byte[BUFFER_SIZE];
            is.mark(BUFFER_SIZE);
            int offset = 0;
            int max = BUFFER_SIZE;
            int c = is.read(bytes, offset, max);
            int firstGT = -1;
            String xmlProlog = null;
            while (c != -1 && firstGT == -1 && offset < BUFFER_SIZE) {
                offset += c;
                max -= c;
                c = is.read(bytes, offset, max);
                xmlProlog = new String(bytes, 0, offset, guessedEnc);
                firstGT = xmlProlog.indexOf('>');
            }
            if (firstGT == -1) {
                if (c == -1) {
                    throw new IOException("Unexpected end of XML stream");
                } else {
                    throw new IOException(
                            "XML prolog or ROOT element not found on first "
                                    + offset + " bytes");
                }
            }
            int bytesRead = offset;
            if (bytesRead > 0) {
                is.reset();
                BufferedReader bReader = new BufferedReader(new StringReader(
                        xmlProlog.substring(0, firstGT + 1)));
                StringBuffer prolog = new StringBuffer();
                String line = bReader.readLine();
                while (line != null) {
                    prolog.append(line);
                    line = bReader.readLine();
                }
                Matcher m = ENCODING_PATTERN.matcher(prolog);
                if (m.find()) {
                    encoding = m.group(1).toUpperCase();
                    encoding = encoding.substring(1, encoding.length() - 1);
                }
            }
        }
        return encoding;
    }

    private static boolean isAppXml(String mime) {
        return mime != null &&
                (mime.equals("application/xml") ||
                        mime.equals("application/xml-dtd") ||
                        mime.equals("application/xml-external-parsed-entity") ||
                        (mime.startsWith("application/") && mime.endsWith("+xml")));
    }

    private static boolean isTextXml(String mime) {
        return mime != null &&
                (mime.equals("text/xml") ||
                        mime.equals("text/xml-external-parsed-entity") ||
                        (mime.startsWith("text/") && mime.endsWith("+xml")));
    }

    private static final String RAW_EX_1 =
            "Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] encoding mismatch";

    private static final String RAW_EX_2 =
            "Invalid encoding, BOM [{0}] XML guess [{1}] XML prolog [{2}] unknown BOM";

    private static final String HTTP_EX_1 =
            "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], BOM must be NULL";

    private static final String HTTP_EX_2 =
            "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], encoding mismatch";

    private static final String HTTP_EX_3 =
            "Invalid encoding, CT-MIME [{0}] CT-Enc [{1}] BOM [{2}] XML guess [{3}] XML prolog [{4}], Invalid MIME";

}