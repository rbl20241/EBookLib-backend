package rb.ebooklib.ebooks.epub.domain;

import rb.ebooklib.ebooks.epub.service.MediatypeService;
import rb.ebooklib.ebooks.util.IOUtil;
import rb.ebooklib.ebooks.util.io.XmlStreamReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import static rb.ebooklib.ebooks.util.Constants.CHARACTER_ENCODING;

public class EpubResource extends Resource {

	public EpubResource(byte[] data, MediaType mediaType) {
		this(null, data, null, mediaType);
	}

	public EpubResource(InputStream in, String href) throws IOException {
		this(null, IOUtil.toByteArray(in), href, MediatypeService.determineMediaType(href));
	}

	private EpubResource(String id, byte[] data, String href, MediaType mediaType) {
		this(id, data, href, mediaType, CHARACTER_ENCODING);
	}


	private EpubResource(String id, byte[] data, String href, MediaType mediaType, String inputEncoding) {
		super(id, data, href, mediaType, inputEncoding);
	}
	
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(getData());
	}
	
	protected byte[] getData() throws IOException {
		return data;
	}

	public void close() {
	}

	public Reader getReader() throws IOException {
		return new XmlStreamReader(new ByteArrayInputStream(getData()), getInputEncoding());
	}
	
	public int hashCode() {
		return getHref().hashCode();
	}
	
	public boolean equals(Object resourceObject) {
		if (! (resourceObject instanceof EpubResource)) {
			return false;
		}
		return getHref().equals(((EpubResource) resourceObject).getHref());
	}
	
}
