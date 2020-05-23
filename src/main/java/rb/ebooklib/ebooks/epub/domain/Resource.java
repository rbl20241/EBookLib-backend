package rb.ebooklib.ebooks.epub.domain;

import rb.ebooklib.ebooks.epub.service.MediatypeService;
import rb.ebooklib.ebooks.util.StringUtil;
import rb.ebooklib.ebooks.util.io.XmlStreamReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import static rb.ebooklib.ebooks.util.Constants.CHARACTER_ENCODING;
import static rb.ebooklib.ebooks.util.IOUtil.toByteArray;

public abstract class Resource {
	
	private String id;
	private String title;
	private String href;
	private String originalHref;
	private MediaType mediaType;
	private String inputEncoding;
	protected byte[] data;

	private Resource(String href) {
		this(null, new byte[0], href, MediatypeService.determineMediaType(href));
	}

	public Resource(byte[] data, MediaType mediaType) {
		this(null, data, null, mediaType);
	}
	
	private Resource(byte[] data, String href) {
		this(null, data, href, MediatypeService.determineMediaType(href), CHARACTER_ENCODING);
	}

	private Resource(Reader in, String href) throws IOException {
		this(null, toByteArray(in, CHARACTER_ENCODING), href, MediatypeService.determineMediaType(href), CHARACTER_ENCODING);
	}

	private Resource(InputStream in, String href) throws IOException {
		this(null, toByteArray(in), href, MediatypeService.determineMediaType(href));
	}

	private Resource(String id, byte[] data, String href, MediaType mediaType) {
		this(id, data, href, mediaType, CHARACTER_ENCODING);
	}

	public Resource(String id, byte[] data, String href, MediaType mediaType, String inputEncoding) {
		this.id = id;
		this.href = href;
		this.originalHref = href;
		this.mediaType = mediaType;
		this.inputEncoding = inputEncoding;
		this.data = data;
	}

	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(getData());
	}
	
	protected byte[] getData() throws IOException {
		return data;
	}

	public void close() {
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public long getSize() {
		return data.length;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getOriginalHref() {
		return originalHref;
	}

	public void setOriginalHref(String originalHref) {
		this.originalHref = originalHref;
	}

	public String getInputEncoding() {
		return inputEncoding;
	}
	
	public void setInputEncoding(String encoding) {
		this.inputEncoding = encoding;
	}
	
	public Reader getReader() throws IOException {
		return new XmlStreamReader(new ByteArrayInputStream(getData()), getInputEncoding());
	}
	
	public int hashCode() {
		return href.hashCode();
	}

	public boolean equals(Object resourceObject) {
		if (! (resourceObject instanceof Resource)) {
			return false;
		}
		return href.equals(((Resource) resourceObject).getHref());
	}
	
	public MediaType getMediaType() {
		return mediaType;
	}
	
	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String toString() {
		return StringUtil.toString("id", id,
				"title", title,
				"encoding", inputEncoding,
				"mediaType", mediaType,
				"href", href,
				"size", (data == null ? 0 : data.length));
	}
}
