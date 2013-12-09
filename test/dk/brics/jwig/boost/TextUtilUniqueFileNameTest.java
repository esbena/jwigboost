package dk.brics.jwig.boost;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

public class TextUtilUniqueFileNameTest {
    @Test
    public void noDuplicates() {
        assertEquals("foo",
                TextUtil.getUniqueFileName("foo", new HashSet<String>()));
    }

    @Test
    public void noDuplicates2() {
        assertEquals("foo",
                TextUtil.getUniqueFileName("foo", Collections.singleton("bar")));
    }

    @Test
    public void oneDuplicate() {
        assertEquals("foo-1",
                TextUtil.getUniqueFileName("foo", Collections.singleton("foo")));
    }

    @Test
    public void twoDuplicates() {
        final HashSet<String> existing = new HashSet<>();
        existing.add("foo");
        existing.add("foo-1");
        assertEquals("foo-2", TextUtil.getUniqueFileName("foo", existing));
    }

    @Test
    public void keepsExtension() {
        assertEquals(
                "foo-1.exe",
                TextUtil.getUniqueFileName("foo.exe",
                        Collections.singleton("foo.exe")));
    }

    @Test
    public void keepsMultipleExtensions() {
        assertEquals(
                "foo-1.exe.zip",
                TextUtil.getUniqueFileName("foo.exe.zip",
                        Collections.singleton("foo.exe.zip")));
    }
}
