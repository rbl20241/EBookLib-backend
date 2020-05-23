package rb.ebooklib.ebooks.util.io;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: rene
 * Date: 17-3-14
 * Time: 22:18
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("WeakerAccess")
public class XmlStreamReaderException extends IOException {

    private static final long serialVersionUID = 1L;

    private final String bomEncoding;

    private final String xmlGuessEncoding;

    private final String xmlEncoding;

    private final String contentTypeEncoding;

    /**
     * Creates an exception instance if the charset encoding could not be
     * determined.
     * <p>
     * Instances of this exception are thrown by the XmlStreamReader.
     *
     * @param msg message describing the reason for the exception.
     * @param bomEnc BOM encoding.
     * @param xmlGuessEnc XML guess encoding.
     * @param xmlEnc XML prolog encoding.
     */
    public XmlStreamReaderException(String msg, String bomEnc,
                                    String xmlGuessEnc, String xmlEnc) {
        this(msg, null, bomEnc, xmlGuessEnc, xmlEnc);
    }

    /**
     * Creates an exception instance if the charset encoding could not be
     * determined.
     * <p>
     * Instances of this exception are thrown by the XmlStreamReader.
     *  @param msg message describing the reason for the exception.
     * @param ctEnc encoding in the content-type.
     * @param bomEnc BOM encoding.
     * @param xmlGuessEnc XML guess encoding.
     * @param xmlEnc XML prolog encoding.
     */
    public XmlStreamReaderException(String msg, String ctEnc,
                                    String bomEnc, String xmlGuessEnc, String xmlEnc) {
        super(msg);
        contentTypeEncoding = ctEnc;
        bomEncoding = bomEnc;
        xmlGuessEncoding = xmlGuessEnc;
        xmlEncoding = xmlEnc;
    }

    /**
     * Returns the BOM encoding found in the InputStream.
     *
     * @return the BOM encoding, null if none.
     */
    public String getBomEncoding() {
        return bomEncoding;
    }

    /**
     * Returns the encoding guess based on the first bytes of the InputStream.
     *
     * @return the encoding guess, null if it couldn't be guessed.
     */
    public String getXmlGuessEncoding() {
        return xmlGuessEncoding;
    }

    /**
     * Returns the encoding found in the XML prolog of the InputStream.
     *
     * @return the encoding of the XML prolog, null if none.
     */
    public String getXmlEncoding() {
        return xmlEncoding;
    }

    /**
     * Returns the encoding in the content-type used to attempt determining the
     * encoding.
     *
     * @return the encoding in the content-type, null if there was not
     *         content-type, no encoding in it or the encoding detection did not
     *         involve HTTP.
     */
    public String getContentTypeEncoding() {
        return contentTypeEncoding;
    }
}