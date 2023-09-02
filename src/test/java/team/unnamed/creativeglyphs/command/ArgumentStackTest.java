package team.unnamed.creativeglyphs.command;

import org.junit.jupiter.api.Test;
import team.unnamed.creativeglyphs.plugin.command.ArgumentStack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArgumentStackTest {

    @Test
    public void test() {
        ArgumentStack args = ArgumentStack.of("arg0", "arg1", "arg2");

        assertTrue(args.hasNext());
        assertEquals(3, args.available());
        assertEquals("arg0", args.next());

        assertTrue(args.hasNext());
        assertEquals(2, args.available());
        assertEquals("arg1", args.next());

        assertTrue(args.hasNext());
        assertEquals(1, args.available());
        assertEquals("arg2", args.next());

        assertFalse(args.hasNext());
        assertEquals(0, args.available());
        assertThrows(IndexOutOfBoundsException.class, args::next);
    }

}
