package com.projecki.fusion.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Represents a node in a binary tree. This node could be the highest parent, the lowest child or anything in between.
 *
 * @param <T> The type of element this node will hold
 */
public class BinaryTreeNode<T> {

    public static final int RIGHT = 1;
    public static final int LEFT = 0;

    private @NotNull final T element;
    private @Nullable BinaryTreeNode<T> childLeft;
    private @Nullable BinaryTreeNode<T> childRight;

    /**
     * Constructs a new binary tree node. This node holds reference to its two children and can be accessed via
     * methods in this class
     *
     * @param element The prime element held by this node
     * @param childLeft A possible left child to this node
     * @param childRight A possible right child to this node
     */
    public BinaryTreeNode(@NotNull T element, @Nullable T childLeft, @Nullable T childRight) {
        Preconditions.checkNotNull(element, "Prime element cannot be null!");
        this.element = element;
        this.childLeft = childLeft == null ? null : new BinaryTreeNode<>(childLeft);
        this.childRight = childRight == null ? null : new BinaryTreeNode<>(childRight);
    }

    /**
     * Constructs a new binary tree node. This node has a non-null prime element and two null children. These children
     * can be set again via methods.
     *
     * @param element The non-null prime element of this node
     */
    public BinaryTreeNode(@NotNull T element) {
        this(element, null, null);
    }

    public void setChildLeft(@Nullable T childLeft) {
        this.childLeft = childLeft == null ? null : new BinaryTreeNode<>(childLeft);
    }

    public void setChildRight(@Nullable T childRight) {
        this.childRight = childRight == null ? null : new BinaryTreeNode<>(childRight);
    }

    public @NotNull T getElement() {
        return element;
    }

    public Optional<BinaryTreeNode<T>> getChildLeft() {
        return Optional.ofNullable(childLeft);
    }

    public Optional<BinaryTreeNode<T>> getChildRight() {
        return Optional.ofNullable(childRight);
    }

    /**
     * Traverses the lower nodes of this binary tree node by the given instructions, returning what it finds at the
     * target node or an empty optional if the path does not exist anymore.
     *
     * Traversing to the right is denoted by a 1
     * Traversing to the left is denoted by a 0
     *
     * @param directions The directions to follow to traverse this tree to the desired node where right=1 and left=0
     * @return An optional containing the node found at the given path or empty if the path doesn't exist.
     */
    public Optional<T> getChildDeep(int[] directions) {
        BinaryTreeNode<T> checking = this;
        for (int direction : directions) {
            Optional<BinaryTreeNode<T>> next = direction == RIGHT ? checking.getChildRight() : checking.getChildLeft();
            if (next.isPresent()) {
                checking = next.get();
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(checking.getElement());
    }
}
