package com.projecki.fusion.chat.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents parts of a message in order mapped to a key representing them. These are used for simplicity of marking
 * which parts of a message are which instead of parsing through one large string/component. This also makes for easy
 * placeholder fulfillment.
 */
public final class Message {

    private Component format;
    private final Map<String, Component> replacements = new HashMap<>();

    public Message(Component format) {
        this.format = format;
    }

    public Message(String format) {
        this(LegacyComponentSerializer.legacyAmpersand().deserialize(format));
    }

    /**
     * Constructs a new message from a given initial "message" to be used as a replacement for the %message% replacement.
     *
     * @param message The message to initialize this message with
     * @return A created message
     */
    public static Message fromInitialMessage(String message) {
        return fromInitialMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    /**
     * Constructs a new message from a given initial "message" to be used as a replacement for the %message% replacement.
     *
     * @param message The message to initialize this message with
     * @return A created message
     */
    public static Message fromInitialMessage(Component message) {
        Message messageObj = new Message(Component.text("%message%"));
        messageObj.setReplacement("%message%", message);
        return messageObj;
    }

    /**
     * @return The format for this message
     */
    public Component getFormat() {
        return format;
    }

    /**
     * Sets the format for this message
     */
    public void setFormat(Component format) {
        this.format = format;
    }

    /**
     * @return A copy of the current state of the replacements for this message
     */
    public Map<String, Component> getReplacements() {
        return new HashMap<>(replacements);
    }

    /**
     * Puts a replacement into this Message's map of replacements. This will overwrite any existing replacement with the
     * same placeholder.
     *
     * @param placeholder The placeholder/placeholder this replacement will replace
     * @param replacement The replacement for this placeholder
     */
    public void setReplacement(String placeholder, Component replacement) {
        this.replacements.put(placeholder, replacement);
    }

    /**
     * Puts a replacement into this Message's map of replacements. This will overwrite any existing replacement with the
     * same placeholder.
     *
     * @param placeholder The placeholder/placeholder this replacement will replace
     * @param replacement The replacement for this placeholder
     */
    public void setReplacement(String placeholder, String replacement) {
        this.setReplacement(placeholder, LegacyComponentSerializer.legacyAmpersand().deserialize(replacement));
    }

    /**
     * Puts a replacement into this Message's map of replacements only if there was not already the same placeholder
     * present regardless of if the replacement was set or empty.
     *
     * @param placeholder The placeholder this replacement will replace
     * @param replacement The replacement for this placeholder
     * @return True if the value was set, false if there was already a value present
     */
    public boolean setIfAbsent(String placeholder, Component replacement) {
        return this.replacements.putIfAbsent(placeholder, replacement) == null;
    }

    /**
     * Puts a replacement into this Message's map of replacements only if there was not already the same placeholder
     * present regardless of if the replacement was set or empty.
     *
     * @param placeholder The placeholder this replacement will replace
     * @param replacement The replacement for this placeholder
     * @return True if the value was set, false if there was already a value present
     */
    public boolean setIfAbsent(String placeholder, String replacement) {
        return this.setIfAbsent(placeholder, LegacyComponentSerializer.legacyAmpersand().deserialize(replacement));
    }

    /**
     * Mutates an exising placeholder only if it exists already by using the given mapping function which gives the
     * existing replacement and asks for a returned mutated replacement
     *
     * @param placeholder The placeholder to mutate the replacement of
     * @param mutator The function to map the existing replacement to the new one
     */
    public void mutateExistingReplacement(String placeholder, Function<Component, Component> mutator) {
        this.getReplacement(placeholder).ifPresent(comp -> this.setReplacement(placeholder, mutator.apply(comp)));
    }

    /**
     * Returns an optional containing the replacement mapped to the given placeholder. If there is no mapping for this
     * placeholder then am empty optional is returned.
     *
     * @param placeholder The placeholder to look for the replacement for
     * @return An optional of the replacement if present otherwise null if no mapping was found
     */
    public Optional<Component> getReplacement(String placeholder) {
        return Optional.ofNullable(this.replacements.get(placeholder));
    }

    /**
     * Builds the current state of this message. This does not mutate the message in any way. Future modifications to
     * this message will not affect previously built components.
     *
     * @return A built component representing the state of this message when it was built
     */
    public Component build() {
        Component built = this.format;
        for (Map.Entry<String, Component> entry : replacements.entrySet()) {
            String placeholder = entry.getKey();
            Component replacement = entry.getValue();
            built = built.replaceText(builder -> builder.matchLiteral(placeholder).replacement((r, b) -> replacement));
        }
        return built;
    }
}
