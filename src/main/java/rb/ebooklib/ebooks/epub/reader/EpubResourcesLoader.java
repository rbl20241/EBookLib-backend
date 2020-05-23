package rb.ebooklib.ebooks.epub.reader;

import rb.ebooklib.ebooks.epub.domain.Resource;
import rb.ebooklib.ebooks.epub.domain.Resources;
import rb.ebooklib.ebooks.epub.service.MediatypeService;
import rb.ebooklib.ebooks.util.ResourceUtil;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
class EpubResourcesLoader {


    public static Resources loadResources(ZipInputStream in, String defaultHtmlEncoding) throws IOException {
        Resources result = new Resources();
        try {
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
            // do nothing
        }
        return result;
    }


}
