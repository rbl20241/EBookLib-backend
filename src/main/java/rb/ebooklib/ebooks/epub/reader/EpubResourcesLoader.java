package rb.ebooklib.ebooks.epub.reader;

import lombok.val;
import rb.ebooklib.ebooks.epub.domain.Resource;
import rb.ebooklib.ebooks.epub.domain.Resources;
import rb.ebooklib.ebooks.epub.service.MediatypeService;
import rb.ebooklib.ebooks.util.ResourceUtil;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
class EpubResourcesLoader {


    public static Resources loadResources(ZipInputStream in, String defaultHtmlEncoding) throws IOException {
        Resources result = new Resources();
        try {
//            val zipFile = new ZipFile("/Users/rbl20241/Temp/boeken/kunst en cultuur/Zwagerman, Joost - Americana.epub");
//            val files = zipFile.entries();
//            while (files.hasMoreElements()) {
//                val entry = (ZipEntry) files.nextElement();
//                if (entry.isDirectory()) {
//                    continue;
//                }
//                val resource = ResourceUtil.createResource(entry, in);
//                if (resource.getMediaType() == MediatypeService.XHTML) {
//                    resource.setInputEncoding(defaultHtmlEncoding);
//                }
//                result.add(resource);
//            }
            for (ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in.getNextEntry()) {
                if (zipEntry.isDirectory()) {
                    continue;
                }
                Resource resource = ResourceUtil.createResource(zipEntry, in);
                if (resource.getMediaType() == MediatypeService.XHTML) {
                    resource.setInputEncoding(defaultHtmlEncoding);
                }
                result.add(resource);
            }
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
            // do nothing
        }
        return result;
    }


}
