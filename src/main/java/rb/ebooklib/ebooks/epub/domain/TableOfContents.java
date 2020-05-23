package rb.ebooklib.ebooks.epub.domain;

import java.util.*;

import static org.springframework.util.AntPathMatcher.DEFAULT_PATH_SEPARATOR;

public class TableOfContents {

    private List<TOCReference> tocReferences;

    public TableOfContents() {
        this(new ArrayList<>());
    }

    public TableOfContents(List<TOCReference> tocReferences) {
        this.tocReferences = tocReferences;
    }

    public List<TOCReference> getTocReferences() {
        return tocReferences;
    }

    public void setTocReferences(List<TOCReference> tocReferences) {
        this.tocReferences = tocReferences;
    }

    public TOCReference addSection(Resource resource, String path) {
        return addSection(resource, path, DEFAULT_PATH_SEPARATOR);
    }

    private TOCReference addSection(Resource resource, String path, String pathSeparator) {
        String[] pathElements = path.split(pathSeparator);
        return addSection(resource, pathElements);
    }

    private static TOCReference findTocReferenceByTitle(String title, List<TOCReference> tocReferences) {
        for (TOCReference tocReference: tocReferences) {
            if (title.equals(tocReference.getTitle())) {
                return tocReference;
            }
        }
        return null;
    }

    private TOCReference addSection(Resource resource, String[] pathElements) {
        if (pathElements == null || pathElements.length == 0) {
            return null;
        }
        TOCReference result = null;
        List<TOCReference> currentTocReferences = this.tocReferences;
        for (String currentTitle : pathElements) {
            result = findTocReferenceByTitle(currentTitle, currentTocReferences);
            if (result == null) {
                result = new TOCReference(currentTitle, null);
                currentTocReferences.add(result);
            }
            currentTocReferences = result.getChildren();
        }
        result.setResource(resource);
        return result;
    }

    public TOCReference addSection(Resource resource, int[] pathElements, String sectionTitlePrefix, String sectionNumberSeparator) {
        if (pathElements == null || pathElements.length == 0) {
            return null;
        }
        TOCReference result = null;
        List<TOCReference> currentTocReferences = this.tocReferences;
        for (int i = 0; i < pathElements.length; i++) {
            int currentIndex = pathElements[i];
            if (currentIndex > 0 && currentIndex < (currentTocReferences.size() - 1)) {
                result = currentTocReferences.get(currentIndex);
            } else {
                result = null;
            }
            if (result == null) {
                paddTOCReferences(currentTocReferences, pathElements, i, sectionTitlePrefix, sectionNumberSeparator);
                result = currentTocReferences.get(currentIndex);
            }
            currentTocReferences = result.getChildren();
        }
        result.setResource(resource);
        return result;
    }

    private void paddTOCReferences(List<TOCReference> currentTocReferences,
                                   int[] pathElements, int pathPos, String sectionPrefix, String sectionNumberSeparator) {
        for (int i = currentTocReferences.size(); i <= pathElements[pathPos]; i++) {
            String sectionTitle = createSectionTitle(pathElements, pathPos, i, sectionPrefix,
                    sectionNumberSeparator);
            currentTocReferences.add(new TOCReference(sectionTitle, null));
        }
    }

    private String createSectionTitle(int[] pathElements, int pathPos, int lastPos,
                                      String sectionPrefix, String sectionNumberSeparator) {
        StringBuilder title = new StringBuilder(sectionPrefix);
        for (int i = 0; i < pathPos; i++) {
            if (i > 0) {
                title.append(sectionNumberSeparator);
            }
            title.append(pathElements[i] + 1);
        }
        if (pathPos > 0) {
            title.append(sectionNumberSeparator);
        }
        title.append(lastPos + 1);
        return title.toString();
    }

    public TOCReference addTOCReference(TOCReference tocReference) {
        if (tocReferences == null) {
            tocReferences = new ArrayList<>();
        }
        tocReferences.add(tocReference);
        return tocReference;
    }

    public List<Resource> getAllUniqueResources() {
        Set<String> uniqueHrefs = new HashSet<>();
        List<Resource> result = new ArrayList<>();
        getAllUniqueResources(uniqueHrefs, result, tocReferences);
        return result;
    }

    private static void getAllUniqueResources(Set<String> uniqueHrefs, List<Resource> result, List<TOCReference> tocReferences) {
        for (TOCReference tocReference: tocReferences) {
            Resource resource = tocReference.getResource();
            if (resource != null && ! uniqueHrefs.contains(resource.getHref())) {
                uniqueHrefs.add(resource.getHref());
                result.add(resource);
            }
            getAllUniqueResources(uniqueHrefs, result, tocReference.getChildren());
        }
    }

    public int size() {
        return getTotalSize(tocReferences);
    }

    private static int getTotalSize(Collection<TOCReference> tocReferences) {
        int result = tocReferences.size();
        for (TOCReference tocReference: tocReferences) {
            result += getTotalSize(tocReference.getChildren());
        }
        return result;
    }

    public int calculateDepth() {
        return calculateDepth(tocReferences, 0);
    }

    private int calculateDepth(List<TOCReference> tocReferences, int currentDepth) {
        int maxChildDepth = 0;
        for (TOCReference tocReference: tocReferences) {
            int childDepth = calculateDepth(tocReference.getChildren(), 1);
            if (childDepth > maxChildDepth) {
                maxChildDepth = childDepth;
            }
        }
        return currentDepth + maxChildDepth;
    }
}
