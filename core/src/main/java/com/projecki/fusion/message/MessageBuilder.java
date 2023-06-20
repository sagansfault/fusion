package com.projecki.fusion.message;

import com.projecki.fusion.component.ComponentBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * @deprecated use {@link ComponentBuilder} in the {@code chat}
 * package instead of this class.
 */
@Deprecated(forRemoval = true)
public class MessageBuilder {

    private Component component;

    private MessageBuilder(Component component) {
        this.component = component;
    }

    /**
     * Constructs a new message builder instance with an empty component and returns it.
     *
     * @return An empty message builder
     */
    public static MessageBuilder builder() {
        return builder(Component.empty());
    }

    /**
     * Constructs a new builder with the formatted content passed into a legacy component deserializer.
     * The formattable content passed in can contain legacy formatting or no formatting at all (&chello!)
     *
     * @param content the, possibly legacy formattable, content to start this builder with
     * @return A builder with the passed in formattable content
     */
    public static MessageBuilder builder(String content) {
        return builder(content, true);
    }

    /**
     * Constructs a new builder with the formatted content passed into a legacy component deserializer.
     * The formattable content passed in can contain legacy formatting or no formatting at all (&chello!)
     *
     * @param content the, possibly legacy formattable, content to start this builder with
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return A builder with the passed in formattable content
     */
    public static MessageBuilder builder(String content, boolean legacy) {
        return builder(legacy ? LegacyComponentSerializer.legacyAmpersand().deserialize(content) : Component.text(content));
    }

    /**
     * Constructs a new builder with the formatted content passed into a legacy component deserializer.
     * The formattable content passed in can contain legacy formatting or no formatting at all (&chello!)
     *
     * @param content the, possibly legacy formattable, content to start this builder with
     * @param color the color to apply on top of the formattable content.
     * @return A builder with the passed in formattable content
     */
    public static MessageBuilder builder(String content, TextColor color) {
        return builder(content, color, true);
    }

    /**
     * Constructs a new builder with the formatted content passed into a legacy component deserializer.
     * The formattable content passed in can contain legacy formatting or no formatting at all (&chello!)
     *
     * @param content the, possibly legacy formattable, content to start this builder with
     * @param color the color to apply on top of the formattable content.
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return A builder with the passed in formattable content
     */
    public static MessageBuilder builder(String content, TextColor color, boolean legacy) {
        return builder(legacy ? LegacyComponentSerializer.legacyAmpersand().deserialize(content).color(color) : Component.text(content).color(color));
    }

    /**
     * Constructs a new builder with the passed in component
     *
     * @param component The component to start this builder with
     * @return A new builder with the passed in component
     */
    public static MessageBuilder builder(Component component) {
        return new MessageBuilder(component);
    }

    /**
     * Constructs a new builder with the formatted content passed into a legacy component deserializer.
     * The formattable content passed in can contain legacy formatting or no formatting at all (&chello!)
     *
     * @param content the, possibly legacy formattable, content to start this builder with
     * @param decoration the decoration to apply
     * @return A builder with the passed in formattable content
     */
    public static MessageBuilder builder(String content, TextDecoration decoration) {
        return builder(content, decoration, true);
    }

    /**
     * Constructs a new builder with the formatted content passed into a legacy component deserializer.
     * The formattable content passed in can contain legacy formatting or no formatting at all (&chello!)
     *
     * @param content the, possibly legacy formattable, content to start this builder with
     * @param decoration the decoration to apply
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return A builder with the passed in formattable content
     */
    public static MessageBuilder builder(String content, TextDecoration decoration, boolean legacy) {
        return builder(legacy ? LegacyComponentSerializer.legacyAmpersand().deserialize(content).decorate(decoration) :
                Component.text(content).decorate(decoration));
    }

    /**
     * Constructs a new builder with the formatted content passed into a legacy component deserializer.
     * The formattable content passed in can contain legacy formatting or no formatting at all (&chello!)
     *
     * @param content the, possibly legacy formattable, content to start this builder with
     * @param color the color to apply on top of the formattable content.
     * @param decoration the decoration to apply
     * @return A builder with the passed in formattable content
     */
    public static MessageBuilder builder(String content, TextColor color, TextDecoration decoration) {
        return builder(content, color, decoration, true);
    }

    /**
     * Constructs a new builder with the formatted content passed into a legacy component deserializer.
     * The formattable content passed in can contain legacy formatting or no formatting at all (&chello!)
     *
     * @param content the, possibly legacy formattable, content to start this builder with
     * @param color the color to apply on top of the formattable content.
     * @param decoration the decoration to apply
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return A builder with the passed in formattable content
     */
    public static MessageBuilder builder(String content, TextColor color, TextDecoration decoration, boolean legacy) {
        return builder(legacy ?
                LegacyComponentSerializer.legacyAmpersand().deserialize(content).color(color).decorate(decoration) :
                Component.text(content).color(color).decorate(decoration));
    }

    /**
     * Prefixes the whole message with the provided formattable string. This string can contain formatting codes and
     * will be converted to a component using the legacy component deserializer (&chello!).
     * This prefix can be applied at any time and will always be applied to the front of the main component with no
     * carry over for colors to subsequent components
     *
     * @param prefix The formattable prefix to prefix this message with
     * @return this builder instance
     */
    public MessageBuilder prefix(String prefix) {
        return this.prefix(prefix, true);
    }

    /**
     * Prefixes the whole message with the provided formattable string. This string can contain formatting codes and
     * will be converted to a component using the legacy component deserializer (&chello!).
     * This prefix can be applied at any time and will always be applied to the front of the main component with no
     * carry over for colors to subsequent components
     *
     * @param prefix The formattable prefix to prefix this message with
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return this builder instance
     */
    public MessageBuilder prefix(String prefix, boolean legacy) {
        this.component = Component.empty()
                .append(legacy ? LegacyComponentSerializer.legacySection().deserialize(prefix) : Component.text(prefix))
                .append(component);
        return this;
    }

    /**
     * Prefixes the whole message with the provided formattable string. This string can contain formatting codes and
     * will be converted to a component using the legacy component deserializer (&chello!).
     * This prefix can be applied at any time and will always be applied to the front of the main component with no
     * carry over for colors to subsequent components
     *
     * @param prefix The formattable prefix to prefix this message with
     * @param color the color to apply on top of this formattable content
     * @return this builder instance
     */
    public MessageBuilder prefix(String prefix, TextColor color) {
        return this.prefix(prefix, color, true);
    }

    /**
     * Prefixes the whole message with the provided formattable string. This string can contain formatting codes and
     * will be converted to a component using the legacy component deserializer (&chello!).
     * This prefix can be applied at any time and will always be applied to the front of the main component with no
     * carry over for colors to subsequent components
     *
     * @param prefix The formattable prefix to prefix this message with
     * @param color the color to apply on top of this formattable content
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return this builder instance
     */
    public MessageBuilder prefix(String prefix, TextColor color, boolean legacy) {
        this.component = Component.empty()
                .append(legacy ? LegacyComponentSerializer.legacySection().deserialize(prefix).color(color) : Component.text(prefix).color(color))
                .append(component);
        return this;
    }

    /**
     * Prefixes the whole message with the provided formattable string. This string can contain formatting codes and
     * will be converted to a component using the legacy component deserializer (&chello!).
     * This prefix can be applied at any time and will always be applied to the front of the main component with no
     * carry over for colors to subsequent components
     *
     * @param prefix The formattable prefix to prefix this message with
     * @param color the color to apply on top of this formattable content
     * @param decoration the decoration to decorate this formattable content with
     * @return this builder instance
     */
    public MessageBuilder prefix(String prefix, TextColor color, TextDecoration decoration) {
        return this.prefix(prefix, color, decoration, true);
    }

    /**
     * Prefixes the whole message with the provided formattable string. This string can contain formatting codes and
     * will be converted to a component using the legacy component deserializer (&chello!).
     * This prefix can be applied at any time and will always be applied to the front of the main component with no
     * carry over for colors to subsequent components
     *
     * @param prefix The formattable prefix to prefix this message with
     * @param color the color to apply on top of this formattable content
     * @param decoration the decoration to decorate this formattable content with
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return this builder instance
     */
    public MessageBuilder prefix(String prefix, TextColor color, TextDecoration decoration, boolean legacy) {
        this.component = Component.empty()
                .append(legacy ?
                        LegacyComponentSerializer.legacySection().deserialize(prefix).color(color).decorate(decoration) :
                        Component.text(prefix).color(color).decorate(decoration))
                .append(component);
        return this;
    }

    /**
     * Prefixes the whole message with the provided formattable string. This string can contain formatting codes and
     * will be converted to a component using the legacy component deserializer (&chello!).
     * This prefix can be applied at any time and will always be applied to the front of the main component with no
     * carry over for colors to subsequent components
     *
     * @param prefix The formattable prefix to prefix this message with
     * @param decoration the decoration to decorate this formattable content with
     * @return this builder instance
     */
    public MessageBuilder prefix(String prefix, TextDecoration decoration) {
        return this.prefix(prefix, decoration, true);
    }

    /**
     * Prefixes the whole message with the provided formattable string. This string can contain formatting codes and
     * will be converted to a component using the legacy component deserializer (&chello!).
     * This prefix can be applied at any time and will always be applied to the front of the main component with no
     * carry over for colors to subsequent components
     *
     * @param prefix The formattable prefix to prefix this message with
     * @param decoration the decoration to decorate this formattable content with
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return this builder instance
     */
    public MessageBuilder prefix(String prefix, TextDecoration decoration, boolean legacy) {
        this.component = Component.empty()
                .append(legacy ? LegacyComponentSerializer.legacySection().deserialize(prefix).decorate(decoration) : Component.text(prefix).decorate(decoration))
                .append(component);
        return this;
    }

    /**
     * Prefixes the whole message with the provided component.
     * This prefix can be applied at any time and will always be applied to the front of the main component with no
     * carry over for colors to subsequent components
     *
     * @param prefix The component to prefix this message with
     * @return this builder instance
     */
    public MessageBuilder prefix(Component prefix) {
        this.component = Component.empty()
                .append(prefix)
                .append(this.component);
        return this;
    }

    /**
     * Append formattable content to this message builder. This string content can contain legacy formatting in it and
     * will be converted (&chello!)
     *
     * @param content The formattable content to deserialize into a component and append to this message builder
     * @return this builder instance
     */
    public MessageBuilder content(String content) {
        return this.content(content, true);
    }

    /**
     * Append formattable content to this message builder. This string content can contain legacy formatting in it and
     * will be converted (&chello!)
     *
     * @param content The formattable content to deserialize into a component and append to this message builder
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return this builder instance
     */
    public MessageBuilder content(String content, boolean legacy) {
        this.component = Component.empty()
                .append(this.component)
                .append(legacy ? LegacyComponentSerializer.legacyAmpersand().deserialize(content) : Component.text(content));
        return this;
    }

    /**
     * Append a component to this message builder.
     *
     * @param component The component to append
     * @return this builder instance
     */
    public MessageBuilder content(Component component) {
        this.component = Component.empty()
                .append(this.component)
                .append(component);
        return this;
    }

    /**
     * Appends content to this message builder with the given color. The text passed in can also include
     * legacy-ampersand formatting (&chello!)
     *
     * @param content Content to append that can include legacy ampersand formatting (&chello!)
     * @param color The color to apply to this message on top of existing formatting
     * @return this builder instance
     */
    public MessageBuilder content(String content, TextColor color) {
        return this.content(content, color, true);
    }

    /**
     * Appends content to this message builder with the given color. The text passed in can also include
     * legacy-ampersand formatting (&chello!)
     *
     * @param content Content to append that can include legacy ampersand formatting (&chello!)
     * @param color The color to apply to this message on top of existing formatting
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return this builder instance
     */
    public MessageBuilder content(String content, TextColor color, boolean legacy) {
        this.component = Component.empty()
                .append(this.component)
                .append(legacy ?
                        LegacyComponentSerializer.legacyAmpersand().deserialize(content).color(color) :
                        Component.text(content, color));
        return this;
    }

    /**
     * Appends content to this message builder with the given color. The text passed in can also include
     * legacy-ampersand formatting (&chello!)
     *
     * @param content Content to append that can include legacy ampersand formatting (&chello!)
     * @param color The color to apply to this message on top of existing formatting
     * @param decoration the decoration to decorate this formattable content with
     * @return this builder instance
     */
    public MessageBuilder content(String content, TextColor color, TextDecoration decoration) {
        return this.content(content, color, decoration, true);
    }

    /**
     * Appends content to this message builder with the given color. The text passed in can also include
     * legacy-ampersand formatting (&chello!)
     *
     * @param content Content to append that can include legacy ampersand formatting (&chello!)
     * @param color The color to apply to this message on top of existing formatting
     * @param decoration the decoration to decorate this formattable content with
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return this builder instance
     */
    public MessageBuilder content(String content, TextColor color, TextDecoration decoration, boolean legacy) {
        this.component = Component.empty()
                .append(this.component)
                .append(legacy ?
                        LegacyComponentSerializer.legacyAmpersand().deserialize(content).color(color).decorate(decoration) :
                        Component.text(content).color(color).decorate(decoration));
        return this;
    }

    /**
     * Appends content to this message builder with the given color. The text passed in can also include
     * legacy-ampersand formatting (&chello!)
     *
     * @param content Content to append that can include legacy ampersand formatting (&chello!)
     * @param decoration the decoration to decorate this formattable content with
     * @return this builder instance
     */
    public MessageBuilder content(String content, TextDecoration decoration) {
        return this.content(content, decoration, true);
    }

    /**
     * Appends content to this message builder with the given color. The text passed in can also include
     * legacy-ampersand formatting (&chello!)
     *
     * @param content Content to append that can include legacy ampersand formatting (&chello!)
     * @param decoration the decoration to decorate this formattable content with
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return this builder instance
     */
    public MessageBuilder content(String content, TextDecoration decoration, boolean legacy) {
        this.component = Component.empty()
                .append(this.component)
                .append(legacy ?
                        LegacyComponentSerializer.legacyAmpersand().deserialize(content).decorate(decoration) :
                        Component.text(content).decorate(decoration));
        return this;
    }

    /**
     * Applies a replace function to this message. Replacing is done at the time of calling this, and is applied to
     * the current existing component(s). This does not apply future-actively
     *
     * @param placeholder The placeholder to replace.
     * @param replacement The replacement
     * @return this builder instance
     */
    public MessageBuilder replace(String placeholder, Component replacement) {
        this.component = this.component.replaceText(builder -> builder.matchLiteral(placeholder).replacement(b ->
                replacement.style(replacement.style().merge(b.asComponent().style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET))));
        return this;
    }

    /**
     * Applies a replace function to this message. Replacing is done at the time of calling this, and is applied to
     * the current existing component(s). This does not apply future-actively
     *
     * @param placeholder The placeholder to replace.
     * @param replacement The replacement
     * @return this builder instance
     */
    public MessageBuilder replace(String placeholder, String replacement) {
        return this.replace(placeholder, replacement, true);
    }

    /**
     * Applies a replace function to this message. Replacing is done at the time of calling this, and is applied to
     * the current existing component(s). This does not apply future-actively
     *
     * @param placeholder The placeholder to replace.
     * @param replacement The replacement
     * @param legacy Whether to apply legacy formatting to the input string (default true)
     * @return this builder instance
     */
    public MessageBuilder replace(String placeholder, String replacement, boolean legacy) {
        return this.replace(placeholder, legacy ?
                LegacyComponentSerializer.legacyAmpersand().deserialize(replacement) :
                Component.text(replacement));
    }

    /**
     * Appends a new line to this builder.
     *
     * @return this builder instance
     */
    public MessageBuilder newLine() {
        this.component = this.component.append(Component.newline());
        return this;
    }

    /**
     * Returns the component this message builder wraps. Use this to send the player/audience the message.
     *
     * @return The component this message wraps to send to a player/audience
     */
    public Component toComponent() {
        return component;
    }
}
