package rb.ebooklib.util;

import java.nio.file.Path;

public class FilePath  {

    /**
     * Wrapper for the path with overwritte toString method. We only want to see the last path part as tree node, not the entire path.
     */
    private final Path path;
    final String text;

    public FilePath(Path path) {

        this.path = path;

        if( path.getNameCount() == 0) {
            this.text = path.toString();
        }
        else {
            this.text = path.getName( path.getNameCount() - 1).toString();
        }
    }

    public Path getPath() {
        return path;
    }

    public String toString() {
        return text;

    }
}