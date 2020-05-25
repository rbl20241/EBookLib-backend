package rb.ebooklib.ebooks.epub.service;

import rb.ebooklib.ebooks.epub.domain.MediaType;
import rb.ebooklib.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class MediatypeService {

    public static final MediaType XHTML = new MediaType("application/xhtml+xml", ".xhtml", new String[] {".htm", ".html", ".xhtml"});
    public static final MediaType EPUB = new MediaType("application/epub+zip", ".epub");
    public static final MediaType NCX = new MediaType("application/x-dtbncx+xml", ".ncx");
    public static final MediaType PDF = new MediaType("application/pdf", ".pdf");

    public static final MediaType JAVASCRIPT = new MediaType("text/javascript", ".js");
    public static final MediaType CSS = new MediaType("text/css", ".css");

    // images
    public static final MediaType JPG = new MediaType("image/jpeg", ".jpg", new String[] {".jpg", ".jpeg"});
    public static final MediaType PNG = new MediaType("image/png", ".png");
    public static final MediaType GIF = new MediaType("image/gif", ".gif");

    public static final MediaType SVG = new MediaType("image/svg+xml", ".svg");

    // fonts
    public static final MediaType TTF = new MediaType("application/x-truetype-font", ".ttf");
    public static final MediaType OPENTYPE = new MediaType("application/vnd.ms-opentype", ".otf");
    public static final MediaType WOFF = new MediaType("application/font-woff", ".woff");

    // audio
    public static final MediaType MP3 = new MediaType("audio/mpeg", ".mp3");
    public static final MediaType OGG = new MediaType("audio/ogg", ".ogg");

    // video
    public static final MediaType MP4 = new MediaType("video/mp4", ".mp4");

    public static final MediaType SMIL = new MediaType("application/smil+xml", ".smil");
    public static final MediaType XPGT = new MediaType("application/adobe-page-template+xml", ".xpgt");
    public static final MediaType PLS = new MediaType("application/pls+xml", ".pls");


    public static final MediaType[] mediatypes = new MediaType[] {
            XHTML, EPUB, JPG, PNG, GIF, CSS, SVG, TTF, NCX, XPGT, OPENTYPE, WOFF, SMIL, PLS, JAVASCRIPT, MP3, MP4, OGG
    };

    public static final Map<String, MediaType> mediaTypesByName = new HashMap<>();
    static {
        for (MediaType mediatype : mediatypes) {
            mediaTypesByName.put(mediatype.getName(), mediatype);
        }
    }

    public static boolean isBitmapImage(MediaType mediaType) {
        return mediaType == JPG || mediaType == PNG || mediaType == GIF;
    }

    /**
     * Gets the MediaType based on the file extension.
     * Null of no matching extension found.
     *
     * @param filename
     * @return the MediaType based on the file extension.
     */
    public static MediaType determineMediaType(String filename) {
        for (MediaType mediaType: mediaTypesByName.values()) {
            for(String extension: mediaType.getExtensions()) {
                if(StringUtil.endsWithIgnoreCase(filename, extension)) {
                    return mediaType;
                }
            }
        }
        return null;
    }

    public static MediaType getMediaTypeByName(String mediaTypeName) {
        return mediaTypesByName.get(mediaTypeName);
    }
}

//public class MediatypeService {
//    public static final MediaType XHTML = new MediaType("application/xhtml+xml", ".xhtml", new String[] {".htm", ".html", ".xhtml"});
//    public static final MediaType EPUB = new MediaType("application/epub+zip", ".epub");
//    public static final MediaType NCX = new MediaType("application/x-dtbncx+xml", ".ncx");
//
//    private static final MediaType JAVASCRIPT = new MediaType("text/javascript", ".js");
//    private static final MediaType CSS = new MediaType("text/css", ".css");
//
//    // images
//    private static final MediaType JPG = new MediaType("image/jpeg", ".jpg", new String[] {".jpg", ".jpeg"});
//    private static final MediaType PNG = new MediaType("image/png", ".png");
//    private static final MediaType GIF = new MediaType("image/gif", ".gif");
//
//    private static final MediaType SVG = new MediaType("image/svg+xml", ".svg");
//
//    // fonts
//    private static final MediaType TTF = new MediaType("application/x-truetype-font", ".ttf");
//    private static final MediaType OPENTYPE = new MediaType("application/vnd.ms-opentype", ".otf");
//    private static final MediaType WOFF = new MediaType("application/font-woff", ".woff");
//
//    // audio
//    private static final MediaType MP3 = new MediaType("audio/mpeg", ".mp3");
//    private static final MediaType MP4 = new MediaType("audio/mp4", ".mp4");
//    private static final MediaType OGG = new MediaType("audio/ogg", ".ogg");
//
//    private static final MediaType SMIL = new MediaType("application/smil+xml", ".smil");
//    private static final MediaType XPGT = new MediaType("application/adobe-page-template+xml", ".xpgt");
//    private static final MediaType PLS = new MediaType("application/pls+xml", ".pls");
//
//    private static final MediaType[] mediatypes = new MediaType[] {
//            XHTML, EPUB, JPG, PNG, GIF, CSS, SVG, TTF, NCX, XPGT, OPENTYPE, WOFF, SMIL, PLS, JAVASCRIPT, MP3, MP4, OGG
//    };
//
//    private static final Map<String, MediaType> mediaTypesByName = new HashMap<>();
//    static {
//        for (MediaType mediatype : mediatypes) {
//            mediaTypesByName.put(mediatype.getName(), mediatype);
//        }
//    }
//
//    public static boolean isBitmapImage(MediaType mediaType) {
//        return mediaType == JPG || mediaType == PNG || mediaType == GIF;
//    }
//
//    /**
//     * Gets the MediaType based on the file extension.
//     * Null of no matching extension found.
//     *
//     * @param filename
//     * @return the MediaType based on the file extension.
//     */
//    public static MediaType determineMediaType(String filename) {
//        for (MediaType mediatype : mediatypes) {
//            for (String extension : mediatype.getExtensions()) {
//                if (StringUtil.endsWithIgnoreCase(filename, extension)) {
//                    return mediatype;
//                }
//            }
//        }
//        return null;
//    }
//
//    public static MediaType getMediaTypeByName(String mediaTypeName) {
//        return mediaTypesByName.get(mediaTypeName);
//    }}
