package com.projecki.fusion.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Literally redis sorted set. YES THIS IS THREADSAFE!!!!
 *
 * @param <T> The type of elements
 */
public class SortedScoredSet<T> implements Iterable<T> {

    // for thread safety
    private final ReentrantLock lock = new ReentrantLock(true);
    // internal collection for actual elements hidden from user
    private final List<ScoredElement<T>> internal = new ArrayList<>();

    /**
     * Removes all the elements from this scored sorted set.
     */
    public void clear() {
        lock.lock();
        try {
            this.internal.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes an item from this set if it was present by the means of {@code .equals()}
     *
     * @param item The item to remove
     * @return true if the ser contained the item and removed it
     */
    public boolean remove(T item) {
        lock.lock();
        boolean contained = false;
        try {
            Iterator<ScoredElement<T>> iterator = internal.iterator();
            while (iterator.hasNext()) {
                ScoredElement<T> next = iterator.next();
                if (next.item.equals(item)) {
                    iterator.remove();
                    contained = true;
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
        return contained;
    }

    /**
     * Removes a elements from this set if they match the given predicate.
     *
     * @param filter The predicate to match.
     * @return true if any elements were removed
     */
    public boolean removeIf(Predicate<T> filter) {
        lock.lock();
        boolean removed;
        try {
            removed = this.internal.removeIf(tScoredElement -> filter.test(tScoredElement.getItem()));
        } finally {
            lock.unlock();
        }
        return removed;
    }

    /**
     * Removes an item at the given index and returns it if present or empty if the index was out of bounds.
     *
     * @param index The index to remove the element at
     * @return An optional containing the removed item, empty if the index was out of bounds.
     */
    public Optional<T> remove(int index) {
        lock.lock();
        ScoredElement<T> removed = null;
        try {
            removed = this.internal.remove(index);
        } catch (IndexOutOfBoundsException ignored) {} finally {
            lock.unlock();
        }
        return removed == null ? Optional.empty() : Optional.of(removed.getItem());
    }

    /**
     * Gets all the elements in this scored sorted set in order as they are the at the time of calling this.
     * The returned list will not reflect changes in this scored sorted set.
     *
     * @return A list of all the elements in this set as they are now.
     */
    public List<T> getAll() {
        lock.lock();
        try {
            return new ArrayList<>(this.internal).stream().map(ScoredElement::getItem).collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an optional containing the item with the highest score in the set. Empty if the set is empty.
     *
     * @return An optional containing the highest scored item, empty if the set is empty.
     */
    public Optional<T> getHighest() {
        lock.lock();
        ScoredElement<T> found = null;
        try {
            found = this.internal.get(0);
        } catch (IndexOutOfBoundsException ignored) {} finally {
            lock.unlock();
        }
        return found == null ? Optional.empty() : Optional.of(found.getItem());
    }

    /**
     * Returns an optional containing the item with the lowest score in the set. Empty if the set is empty.
     *
     * @return An optional containing the lowest scored item, empty if the set is empty.
     */
    public Optional<T> getLowest() {
        lock.lock();
        ScoredElement<T> found = null;
        try {
            found = this.internal.get(internal.size() - 1);
        } catch (IndexOutOfBoundsException ignored) {} finally {
            lock.unlock();
        }
        return found == null ? Optional.empty() : Optional.of(found.getItem());
    }

    /**
     * Returns an optional containing the item at the specified index or empty if the index is out of bounds.
     *
     * @param index The index to get the item at.
     * @return An optional containing the item at that index or empty if the index is out of bounds.
     */
    public Optional<T> get(int index) {
        lock.lock();
        ScoredElement<T> found = null;
        try {
            found = this.internal.get(index);
        } catch (IndexOutOfBoundsException ignored) {} finally {
            lock.unlock();
        }
        return found == null ? Optional.empty() : Optional.of(found.getItem());
    }

    /**
     * Returns a collection containing all the items with the given score.
     *
     * @param score The score to look for items with
     * @return A collection containing the items of this score
     */
    public Collection<T> getAllOfScore(int score) {
        lock.lock();
        List<T> found = new ArrayList<>();
        try {
            for (ScoredElement<T> tScoredElement : this.internal) {
                if (tScoredElement.getScore() == score) {
                    found.add(tScoredElement.getItem());
                }
            }
        } finally {
            lock.unlock();
        }
        return found;
    }

    /**
     * Returns an optional containing the score of a given item, empty if the item was not present
     * by means of {@code .equals()}
     *
     * @param item The item to get the score of
     * @return An optional containing the score of the item or empty if the item was not found
     */
    public Optional<Integer> getScore(T item) {
        lock.lock();
        try {
            for (ScoredElement<T> tScoredElement : this.internal) {
                if (tScoredElement.equals(item)) {
                    return Optional.of(tScoredElement.getScore());
                }
            }
        } finally {
            lock.unlock(); // yes, you nerd, this will get executed despite the return statement above
        }
        return Optional.empty();
    }

    /**
     * @return Whether this queue is empty or not.
     */
    public boolean isEmpty() {
        return this.internal.isEmpty();
    }

    /**
     * @return The length or size of this queue.
     */
    public int size() {
        return this.internal.size();
    }

    /**
     * Returns whether this set contains the given item by means of {@code .equals()}
     *
     * @param item The item to check if contains
     * @return Whether this set contains the given item
     */
    public boolean contains(T item) {
        lock.lock();
        try {
            for (ScoredElement<T> tScoredElement : this.internal) {
                if (tScoredElement.getItem().equals(item)) {
                    return true;
                }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    /**
     * Adds an item to this set, overwriting any existing items by means of {@code .equals()}, with the given score.
     * Scores determine insertion order. Higher scores will appear higher in the set with {@code #.get(0)} returning
     * the item with the highest score. Items with duplicate scores will be kept in retained succession. Scores can
     * also be negative.
     *
     * @param item The item to add
     * @param score The score to give this item
     * @return the index at which this element was added. -1 if the element could no be added
     */
    public int put(T item, int score) {
        lock.lock();

        try {
            if (item == null) {
                return -1;
            }

            ScoredElement<T> toAdd = new ScoredElement<>(item, score);

            // no duplicates
            // score does not matter here as equals does not contain it
            internal.remove(toAdd);

            // yes this goes after the remove. trust me :)
            if (internal.isEmpty()) {
                internal.add(toAdd);
                return 0;
            }

            if (internal.size() == 1) {
                ScoredElement<T> existing = this.internal.get(0);
                if (existing.getScore() < score) {
                    internal.add(0, toAdd);
                    return 0;
                } else {
                    internal.add(toAdd);
                    return internal.size() - 1;
                }
            }

            for (int i = internal.size() - 1; i >= 0; i--) {
                ScoredElement<T> current = internal.get(i);
                if (current.getScore() >= score) {
                    if (i == this.internal.size() - 1) {
                        this.internal.add(toAdd);
                        return internal.size() - 1;
                    } else {
                        this.internal.add(i + 1, toAdd);
                        return i + 1;
                    }
                } else {
                    if (i == 0) {
                        this.internal.add(0, toAdd);
                        return 0;
                    }
                }
            }
            return -1;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return this.getAll().iterator();
    }

    static class ScoredElement<E> {
        private final E item;
        private final int score;

        public ScoredElement(E item, int score) {
            this.item = item;
            this.score = score;
        }

        public E getItem() {
            return item;
        }

        public int getScore() {
            return score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScoredElement<?> that = (ScoredElement<?>) o;
            return item.equals(that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item);
        }
    }
}
