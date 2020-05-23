package rb.ebooklib.ebooks.epub.domain;

public class SpineReference extends ResourceReference {

    @SuppressWarnings("unused")
    private boolean linear;

    public SpineReference(Resource resource) {
        this(resource, true);
    }


    @SuppressWarnings("SameParameterValue")
    private SpineReference(Resource resource, boolean linear) {
        super(resource);
        this.linear = linear;
    }

    public void setLinear(boolean linear) {
        this.linear = linear;
    }

}