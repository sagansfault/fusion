package com.projecki.fusion.util.progress;

import net.kyori.adventure.text.Component;
import org.junit.Test;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since May 09, 2022
 * @author Andavin
 */
public class MessageProgressBarTest {

    private static final long MILLION = 1000_000;

    @Test
    public void testSmallFourths() {
        Component bar25 = MessageProgressBar.of(40, '|')
                .progress(5).goal(20)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(10), GREEN).append(text("|".repeat(30), RED)), bar25);
        Component bar50 = MessageProgressBar.of(40, '|')
                .progress(10).goal(20)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(20), GREEN).append(text("|".repeat(20), RED)), bar50);
        Component bar75 = MessageProgressBar.of(40, '|')
                .progress(15).goal(20)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(30), GREEN).append(text("|".repeat(10), RED)), bar75);
        Component bar100 = MessageProgressBar.of(40, '|')
                .progress(20).goal(20)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(40), GREEN).append(text("|".repeat(0), RED)), bar100);
    }

    @Test
    public void testSmallFifths() {
        Component bar20 = MessageProgressBar.of(40, '|')
                .progress(4).goal(20)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(8), GREEN).append(text("|".repeat(32), RED)), bar20);
        Component bar40 = MessageProgressBar.of(40, '|')
                .progress(8).goal(20)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(16), GREEN).append(text("|".repeat(24), RED)), bar40);
        Component bar60 = MessageProgressBar.of(40, '|')
                .progress(12).goal(20)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(24), GREEN).append(text("|".repeat(16), RED)), bar60);
        Component bar80 = MessageProgressBar.of(40, '|')
                .progress(16).goal(20)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(32), GREEN).append(text("|".repeat(8), RED)), bar80);
        Component bar100 = MessageProgressBar.of(40, '|')
                .progress(20).goal(20)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(40), GREEN).append(text("|".repeat(0), RED)), bar100);
    }

    @Test
    public void testLargeFourths() {
        Component bar25 = MessageProgressBar.of(40, '|')
                .progress(5 * MILLION).goal(20 * MILLION)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(10), GREEN).append(text("|".repeat(30), RED)), bar25);
        Component bar50 = MessageProgressBar.of(40, '|')
                .progress(10 * MILLION).goal(20 * MILLION)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(20), GREEN).append(text("|".repeat(20), RED)), bar50);
        Component bar75 = MessageProgressBar.of(40, '|')
                .progress(15 * MILLION).goal(20 * MILLION)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(30), GREEN).append(text("|".repeat(10), RED)), bar75);
        Component bar100 = MessageProgressBar.of(40, '|')
                .progress(20 * MILLION).goal(20 * MILLION)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(40), GREEN).append(text("|".repeat(0), RED)), bar100);
    }

    @Test
    public void testLargeFifths() {
        Component bar20 = MessageProgressBar.of(40, '|')
                .progress(4 * MILLION).goal(20 * MILLION)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(8), GREEN).append(text("|".repeat(32), RED)), bar20);
        Component bar40 = MessageProgressBar.of(40, '|')
                .progress(8 * MILLION).goal(20 * MILLION)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(16), GREEN).append(text("|".repeat(24), RED)), bar40);
        Component bar60 = MessageProgressBar.of(40, '|')
                .progress(12 * MILLION).goal(20 * MILLION)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(24), GREEN).append(text("|".repeat(16), RED)), bar60);
        Component bar80 = MessageProgressBar.of(40, '|')
                .progress(16 * MILLION).goal(20 * MILLION)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(32), GREEN).append(text("|".repeat(8), RED)), bar80);
        Component bar100 = MessageProgressBar.of(40, '|')
                .progress(20 * MILLION).goal(20 * MILLION)
                .completeColor(GREEN)
                .incompleteColor(RED)
                .create();
        assertEquals(text("|".repeat(40), GREEN).append(text("|".repeat(0), RED)), bar100);
    }
}
