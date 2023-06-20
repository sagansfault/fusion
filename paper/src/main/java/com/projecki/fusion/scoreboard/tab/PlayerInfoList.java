package com.projecki.fusion.scoreboard.tab;

import com.projecki.unversioned.PlayerInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.*;
import static com.projecki.fusion.scoreboard.tab.CustomTabList.*;

/**
 * A {@link List} that holds {@link PlayerInfo} and is bound
 * by x and y indices which reference a {@link CustomTabList} to display
 * the {@link PlayerInfo} within.
 *
 * @since May 23, 2022
 * @author Andavin
 */
public class PlayerInfoList extends AbstractList<PlayerInfo> implements Comparable<PlayerInfoList>, Cloneable {

    /**
     * Create a new {@link PlayerInfoList} with the specified
     * starting index and size.
     *
     * @param x The x (column) index of the starting row.
     * @param y The y (row) index of the starting row.
     * @param size The size of the list. This is the max amount of
     *             entries that will be displayed at any one time.
     * @return The newly created {@link PlayerInfoList}.
     */
    public static PlayerInfoList of(int x, int y, int size) {
        return of(x, y, size, false);
    }

    /**
     * Create a new {@link PlayerInfoList} with the specified
     * starting index and size.
     *
     * @param x The x (column) index of the starting row.
     * @param y The y (row) index of the starting row.
     * @param size The size of the list. This is the max amount of
     *             entries that will be displayed at any one time.
     * @param displayTruncation If {@code true}, when the amount of entries exceeds the
     *                          {@code size}, the final entry will be displayed as
     *                          {@code "... excess entries more"}.
     * @return The newly created {@link PlayerInfoList}.
     */
    public static PlayerInfoList of(int x, int y, int size, boolean displayTruncation) {
        checkArgument(0 <= x && x < COLUMNS, "invalid x index: %s", x);
        checkArgument(0 <= y && y < ROWS, "invalid y index: %s", y);
        int firstIndex = x * ROWS + y;
        int lastIndex = firstIndex + size - 1;
        checkArgument(lastIndex < TOTAL_ROWS, "invalid size: %s", size);
        return new PlayerInfoList(
                firstIndex, lastIndex,
                size, displayTruncation
        );
    }

    private int index;
    private CustomTabList binding;

    private final int maxSize;
    private final int startIndex, endIndex;
    private final boolean displayTruncation;
    private final List<PlayerInfo> info;

    private PlayerInfoList(int startIndex, int endIndex,
                           int maxSize, boolean displayTruncation) {
        this.maxSize = maxSize;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.info = new ArrayList<>(maxSize);
        this.displayTruncation = displayTruncation;
    }

    /**
     * Get the maximum amount of {@link PlayerInfo} that
     * can be displayed from this list at once.
     *
     * @return The max display size.
     */
    public int maxSize() {
        return maxSize;
    }

    @Override
    public int size() {
        return info.size();
    }

    @Override
    public PlayerInfo get(int index) {
        return info.get(index);
    }

    @Override
    public boolean add(PlayerInfo info) {
        // Add it immediately
        checkNotNull(info);
        this.info.add(info);
        int row = this.increment();
        if (row != -1 && row < TOTAL_ROWS && binding != null) {
            this.updateDisplay();
            return true;
        }

        return false;
    }

    @Override
    public void add(int index, PlayerInfo info) {

        checkNotNull(info);
        this.info.add(index, info);
        if (index < maxSize) {
            // Update if the index is being displayed
            this.updateDisplay();
        }
    }

    @Override
    public PlayerInfo set(int index, PlayerInfo info) throws IndexOutOfBoundsException {

        checkNotNull(info);
        PlayerInfo previous = this.info.set(index, info);
        if (index < maxSize) {
            // Update if the index is being displayed
            this.updateDisplay();
        }

        return previous;
    }

    @Override
    public PlayerInfo remove(int index) throws IndexOutOfBoundsException {

        checkNotNull(info);
        int size = this.info.size();
        PlayerInfo info = this.info.remove(index);
        if (size <= maxSize) {
            // Only decrement if size was at max or less
            // otherwise the display is still at max
            int row = this.decrement();
            if (row != -1 && binding != null) {
                binding.set(row, (PlayerInfo) null); // Set to default
            }
        }
        // Only update if the index was already being displayed
        if (index < maxSize) {
            this.updateDisplay();
        }

        return info;
    }

    /**
     * Clear this list and replace it with the {@link PlayerInfo}
     * in the specified list.
     * <p>
     *     After execution of this method, the {@link PlayerInfo}
     *     within this list will be exactly that of the specified list.
     * </p>
     *
     * @param info The list of {@link PlayerInfo} to replace with.
     */
    public void replace(List<PlayerInfo> info) {

        if (info.isEmpty()) {
            this.clear();
            return;
        }

        int size = this.info.size();
        this.index = startIndex;
        this.info.clear();
        Int2ObjectMap<PlayerInfo> display = binding != null ?
                new Int2ObjectOpenHashMap<>(Math.max(size, info.size())) : null;
        int lastRow = startIndex;
        for (PlayerInfo playerInfo : info) {

            this.info.add(playerInfo);
            int row = this.increment();
            if (row != -1 && row < TOTAL_ROWS && display != null) {
                display.put(row, playerInfo);
                lastRow = row;
            }
        }

        if (display != null) {
            // Clear up any excess
            int diff = size - info.size();
            if (diff > 0) {

                for (int i = 0, row = increment(lastRow, endIndex);
                     i < diff && row != -1;
                     i++, row = increment(row, endIndex)) {
                    display.put(row, null);
                }
            }
            // More than can be displayed
            if (displayTruncation && maxSize > 3 && info.size() > display.size()) {
                PlayerInfo extra = PlayerInfo.of("... and " + (info.size() - display.size() + 1) + " more");
                display.put(lastRow, extra);
            }

            this.binding.set(display);
        }
    }

    @Override
    public void clear() {

        int size = info.size();
        this.index = startIndex;
        this.info.clear();
        if (size > 0 && binding != null) {

            Int2ObjectMap<PlayerInfo> display = new Int2ObjectOpenHashMap<>(size);
            for (int i = 0, row = startIndex;
                 i < size && row != -1;
                 i++, row = increment(row, endIndex)) {
                display.put(row, null);
            }

            this.binding.set(display);
        }
    }

    @Override
    public int compareTo(@NotNull PlayerInfoList o) {
        // This object is less than o
        if (endIndex < o.startIndex) {
            return startIndex - o.startIndex;
        }
        // This object is greater than o
        if (o.endIndex < startIndex) {
            return o.startIndex - startIndex;
        }
        // The lists overlap and are therefore "equal"
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof PlayerInfoList l &&
                l.startIndex == startIndex &&
                l.endIndex == endIndex &&
                l.maxSize == maxSize &&
                l.displayTruncation == displayTruncation;
    }

    @Override
    public int hashCode() {
        return startIndex * 31 + endIndex * 31 + maxSize * 31 +
                Boolean.hashCode(displayTruncation);
    }

    @Override
    public String toString() {
        return "(" + startIndex / ROWS + ", " + startIndex % ROWS + ") -> " +
                "(" + endIndex / ROWS + ", " + endIndex % ROWS + ")";
    }

    @Override
    public PlayerInfoList clone() {
        try {
            PlayerInfoList list = (PlayerInfoList) super.clone();
            list.binding = null;
            return list;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    void bind(CustomTabList list) throws IllegalStateException {
        checkState(binding == null, "already bound");
        checkNotNull(list);
        this.binding = list;
    }

    void updateDisplay() {

        if (binding == null) {
            return;
        }

        int size = info.size();
        if (size == 0) {
            return;
        }

        int lastRow = startIndex;
        Int2ObjectMap<PlayerInfo> display = new Int2ObjectOpenHashMap<>(size);
        for (int i = 0, row = startIndex;
             i < size && row != -1;
             i++, row = increment(row, endIndex)) {
            display.put(row, info.get(i));
            lastRow = row;
        }
        // More than can be displayed
        if (displayTruncation && maxSize > 3 && size > display.size()) {
            // Add 1 because we're replacing one that could have been displayed
            // Will never be less than 2
            PlayerInfo extra = PlayerInfo.of("... and " + (size - display.size() + 1) + " more");
            display.put(lastRow, extra);
        }

        this.binding.set(display);
    }

    private int increment() {
        return index < endIndex ? index++ : -1;
    }

    private int decrement() {
        return index > startIndex ? index-- : -1;
    }

    private static int increment(int row, int endIndex) {
        return row < endIndex ? row + 1 : -1;
    }
}
