package rb.ebooklib.ebooks.epub.domain;

import rb.ebooklib.ebooks.epub.service.MediatypeService;
import rb.ebooklib.util.StringUtil;

import java.util.*;

import static rb.ebooklib.ebooks.util.Constants.FRAGMENT_SEPARATOR_CHAR;

public class Resources  {

    private static final String IMAGE_PREFIX = "image_";
    private static final String ITEM_PREFIX = "item_";
    private int lastId = 1;

    private Map<String, Resource> resources = new HashMap<>();

    public Resource add(Resource resource) {
        fixResourceHref(resource);
        fixResourceId(resource);
        this.resources.put(resource.getHref(), resource);
        return resource;
    }

    private void fixResourceId(Resource resource) {
        String resourceId = resource.getId();

        // first try and create a unique id based on the resource's href
        if (StringUtil.isBlank(resource.getId())) {
            resourceId = StringUtil.substringBeforeLast(resource.getHref(), '.');
            resourceId = StringUtil.substringAfterLast(resourceId, '/');
        }

        resourceId = makeValidId(resourceId, resource);

        // check if the id is unique. if not: create one from scratch
        if (StringUtil.isBlank(resourceId) || containsId(resourceId)) {
            resourceId = createUniqueResourceId(resource);
        }
        resource.setId(resourceId);
    }

    private String makeValidId(String resourceId, Resource resource) {
        if (StringUtil.isNotBlank(resourceId) && ! Character.isJavaIdentifierStart(resourceId.charAt(0))) {
            resourceId = getResourceItemPrefix(resource) + resourceId;
        }
        return resourceId;
    }

    private String getResourceItemPrefix(Resource resource) {
        String result;
        if (MediatypeService.isBitmapImage(resource.getMediaType())) {
            result = IMAGE_PREFIX;
        } else {
            result = ITEM_PREFIX;
        }
        return result;
    }

    private String createUniqueResourceId(Resource resource) {
        int counter = lastId;
        if (counter == Integer.MAX_VALUE) {
            if (resources.size() == Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Resources contains " + Integer.MAX_VALUE + " elements: no new elements can be added");
            } else {
                counter = 1;
            }
        }
        String prefix = getResourceItemPrefix(resource);
        String result = prefix + counter;
        while (containsId(result)) {
            result = prefix + (++ counter);
        }
        lastId = counter;
        return result;
    }

    private boolean containsId(String id) {
        if (StringUtil.isBlank(id)) {
            return false;
        }
        for (Resource resource: resources.values()) {
            if (id.equals(resource.getId())) {
                return true;
            }
        }
        return false;
    }

    private Resource getById(String id) {
        if (StringUtil.isBlank(id)) {
            return null;
        }
        for (Resource resource: resources.values()) {
            if (id.equals(resource.getId())) {
                return resource;
            }
        }
        return null;
    }

    public Resource remove(String href) {
        return resources.remove(href);
    }

    private void fixResourceHref(Resource resource) {
        if(StringUtil.isNotBlank(resource.getHref())
                && ! resources.containsKey(resource.getHref())) {
            return;
        }
        if(StringUtil.isBlank(resource.getHref())) {
            if(resource.getMediaType() == null) {
                throw new IllegalArgumentException("Resource must have either a MediaType or a href");
            }
            int i = 1;
            String href = createHref(resource.getMediaType(), i);
            while(resources.containsKey(href)) {
                href = createHref(resource.getMediaType(), (++i));
            }
            resource.setHref(href);
        }
    }

    private String createHref(MediaType mediaType, int counter) {
        if(MediatypeService.isBitmapImage(mediaType)) {
            return "image_" + counter + mediaType.getDefaultExtension();
        } else {
            return "item_" + counter + mediaType.getDefaultExtension();
        }
    }


    public boolean isEmpty() {
        return resources.isEmpty();
    }

    public int size() {
        return resources.size();
    }

	public Map<String, Resource> getResourceMap() {
		return resources;
	}
	
	public Collection<Resource> getAll() {
        return resources.values();
    }


    public boolean containsByHref(String href) {
        if (StringUtil.isBlank(href)) {
            return false;
        }
        return resources.containsKey(StringUtil.substringBefore(href, FRAGMENT_SEPARATOR_CHAR));
    }

    public void set(Collection<Resource> resources) {
        this.resources.clear();
        addAll(resources);
    }

    private void addAll(Collection<Resource> resources) {
        for(Resource resource: resources) {
            fixResourceHref(resource);
            this.resources.put(resource.getHref(), resource);
        }
    }

    public void set(Map<String, Resource> resources) {
        this.resources = new HashMap<>(resources);
    }

    public Resource getByIdOrHref(String idOrHref) {
        Resource resource = getById(idOrHref);
        if (resource == null) {
            resource = getByHref(idOrHref);
        }
        return resource;
    }

   public Resource getByHref(String href) {
        if (StringUtil.isBlank(href)) {
            return null;
        }
        href = StringUtil.substringBefore(href, FRAGMENT_SEPARATOR_CHAR);
        return resources.get(href);
    }

    public Resource findFirstResourceByMediaType(MediaType mediaType) {
        return findFirstResourceByMediaType(resources.values(), mediaType);
    }

    private static Resource findFirstResourceByMediaType(Collection<Resource> resources, MediaType mediaType) {
        for (Resource resource: resources) {
            if (resource.getMediaType() == mediaType) {
                return resource;
            }
        }
        return null;
    }

	public List<Resource> getResourcesByMediaType(MediaType mediaType) {
		List<Resource> result = new ArrayList<>();
		if (mediaType == null) {
			return result;
		}
		for (Resource resource: getAll()) {
			if (resource.getMediaType() == mediaType) {
				result.add(resource);
			}
		}
		return result;
	}

	public List<Resource> getResourcesByMediaTypes(MediaType[] mediaTypes) {
		List<Resource> result = new ArrayList<>();
		if (mediaTypes == null) {
			return result;
		}
		
		// this is the fastest way of doing this according to 
		// http://stackoverflow.com/questions/1128723/in-java-how-can-i-test-if-an-array-contains-a-certain-value
		List<MediaType> mediaTypesList = Arrays.asList(mediaTypes);
		for (Resource resource: getAll()) {
			if (mediaTypesList.contains(resource.getMediaType())) {
				result.add(resource);
			}
		}
		return result;
	}

    public Collection<String> getAllHrefs() {
        return resources.keySet();
    }
}