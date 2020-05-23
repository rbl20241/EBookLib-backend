package rb.ebooklib.tree;

import rb.ebooklib.util.FilePath;

public class Node {
    FilePath value;
    Node left;
    Node right;

    Node(FilePath value) {
        this.value = new FilePath(value.getPath());
        left = null;
        right = null;
    }
}
