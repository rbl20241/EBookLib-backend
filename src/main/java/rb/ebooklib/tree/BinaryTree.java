package rb.ebooklib.tree;

import rb.ebooklib.util.FilePath;

public class BinaryTree {
    Node root;

    private Node addRecursive(Node current, FilePath value) {
        if (current == null) {
            return new Node(value);
        }

        if (value.toString().compareTo(current.value.toString()) < 0) {
            current.left = addRecursive(current.left, value);
        } else if (value.toString().compareTo(current.value.toString()) > 0) {
            current.right = addRecursive(current.right, value);
        } else {
            // value already exists
            return current;
        }

        return current;
    }

    public void add(FilePath value) {
        root = addRecursive(root, value);
    }
}
