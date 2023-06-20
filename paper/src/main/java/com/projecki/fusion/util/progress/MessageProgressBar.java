package com.projecki.fusion.util.progress;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

/**
 * @since May 09, 2022
 * @author Andavin
 */
public class MessageProgressBar {

    /**
     * Start creating a new progress bar using the specified
     * {@code section} character.
     *
     * @param sectionCount The amount of sections to include in
     *                     the progress bar.
     * @param section The section character.
     * @return The new {@link MessageProgressBar} builder.
     */
    public static MessageProgressBar of(int sectionCount, char section) {
        return of(sectionCount, String.valueOf(section));
    }

    /**
     * Start creating a new progress bar using the specified
     * {@link String section}.
     *
     * @param sectionCount The amount of sections to include in
     *                     the progress bar.
     * @param section The section {@link String}.
     * @return The new {@link MessageProgressBar} builder.
     */
    public static MessageProgressBar of(int sectionCount, String section) {
        return new MessageProgressBar(sectionCount, section);
    }

    private final String section;
    private final int sectionCount;

    private long progress, goal = 1;
    private TextColor completeColor = GREEN;
    private TextColor incompleteColor = RED;
    private final Map<TextDecoration, State> decorations = new EnumMap<>(TextDecoration.class);

    private Component prefix, suffix;
    private IntFunction<Component> percentSuffix;

    private MessageProgressBar(int sectionCount, String section) {
        this.sectionCount = sectionCount;
        this.section = requireNonNull(section, "section");
    }

    /**
     * The progress along the bar.
     *
     * @param progress The progress.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar progress(long progress) {
        this.progress = progress;
        return this;
    }

    /**
     * The goal to reach along the bar.
     *
     * @param goal The goal.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar goal(long goal) {
        this.goal = goal;
        return this;
    }

    /**
     * The {@link TextColor} to show for completed sections
     * of the bar.
     * <p>
     *     Defaults to {@link NamedTextColor#GREEN}
     * </p>
     *
     * @param completeColor The {@link TextColor complete color}.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar completeColor(TextColor completeColor) {
        this.completeColor = completeColor;
        return this;
    }

    /**
     * The {@link TextColor} to show for incomplete sections
     * of the bar.
     * <p>
     *     Defaults to {@link NamedTextColor#RED}
     * </p>
     *
     * @param incompleteColor The {@link TextColor incomplete color}.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar incompleteColor(TextColor incompleteColor) {
        this.incompleteColor = incompleteColor;
        return this;
    }

    /**
     * Show a default percentage suffix on the bar.
     *
     * @return This MessageProgressBar.
     */
    public MessageProgressBar percentSuffix() {
        return this.percentSuffix(percent -> text(percent + "%", WHITE));
    }

    /**
     * Show a percentage suffix on the bar.
     * <p>
     *     The value passed to the specified function will be
     *     a whole number percentage from {@code 0-100}.
     * </p>
     *
     * @param percentSuffix The {@link IntFunction} that should be used
     *                      to create the {@link Component percentage suffix}.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar percentSuffix(IntFunction<Component> percentSuffix) {
        this.percentSuffix = percentSuffix;
        return this;
    }

    /**
     * The {@link Component} to prefix the bar.
     *
     * @param prefix The {@link Component prefix}.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar prefix(Component prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * The {@link Component} to suffix the bar.
     *
     * @param suffix The {@link Component suffix}.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar suffix(Component suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * The {@link TextDecoration} to use on the bar sections.
     *
     * @param decoration The {@link TextDecoration}.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar decorate(TextDecoration decoration) {
        return this.decorate(decoration, State.TRUE);
    }

    /**
     * The {@link TextDecoration} to use on the bar sections.
     *
     * @param decoration The {@link TextDecoration}.
     * @param state The {@link State} to set.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar decorate(TextDecoration decoration, State state) {
        this.decorations.put(decoration, state);
        return this;
    }

    /**
     * The {@link TextDecoration TextDecorations} to use on the bar sections.
     *
     * @param decorations The {@link TextDecoration TextDecorations}.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar decorate(TextDecoration... decorations) {

        for (TextDecoration decoration : decorations) {
            this.decorations.put(decoration, State.TRUE);
        }

        return this;
    }

    /**
     * The {@link TextDecoration TextDecorations} to use on the bar sections.
     *
     * @param decorations The {@link TextDecoration TextDecorations}.
     * @return This MessageProgressBar.
     */
    public MessageProgressBar decorations(Map<TextDecoration, State> decorations) {
        this.decorations.putAll(decorations);
        return this;
    }

    /**
     * Create the {@link Component} progress bar using all
     * the value provided within this progress bar.
     *
     * @return The newly created {@link Component} progress bar.
     */
    public Component create() {

        if (goal <= 0) {
            goal = 1;
            progress = 1;
        } else {

            if (progress < 0) {
                progress = 0;
            } else if (progress > goal) {
                progress = goal;
            }
        }

        double percent = progress / (double) goal;
        int completeSectionCount = (int) (sectionCount * percent);
        int incompleteSectionCount = sectionCount - completeSectionCount;
        Component bar = text(section.repeat(completeSectionCount), completeColor).decorations(decorations)
                .append(text(section.repeat(incompleteSectionCount), incompleteColor).decorations(decorations));
        if (percentSuffix != null) {
            Component suffix = percentSuffix.apply((int) (percent * 100));
            bar = bar.append(space()).append(stripFormatting(bar, suffix));
        }

        if (prefix != null) {
            bar = prefix.append(space()).append(stripFormatting(prefix, bar));
        }

        if (suffix != null) {
            bar = bar.append(space()).append(stripFormatting(bar, suffix));
        }

        return bar;
    }

    private void defaultDecorations() {

        for (TextDecoration decoration : TextDecoration.values()) {
            this.decorations.put(decoration, State.FALSE);
        }
    }

    private Component stripFormatting(Component previous, Component component) {
        // Explicitly remove all the decorations from the component
        // that are present on the previous, but not on the component
        for (TextDecoration decoration : TextDecoration.values()) {

            if (previous.decoration(decoration) == State.TRUE &&
                component.decoration(decoration) == State.NOT_SET) {
                component = component.decoration(decoration, State.FALSE);
            }
        }

        return component.colorIfAbsent(WHITE);
    }
}
