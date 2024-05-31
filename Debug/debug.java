package Debug;

import java.net.URI;

public class debug {

    @Test
    void status() {
        Error error = new Error();
        error.status(404);
        assertEquals(404, error.getStatus());
    }

    @Test
    void getType() {
        Error error = new Error();
        error.type(URI.create("http://example.com"));
        assertEquals(URI.create("http://example.com"), error.getType());
    }

    @Test
    void title() {
        Error error = new Error();
        error.title("Some error occurred");
        assertEquals("Some error occurred", error.getTitle());
    }

    @Test
    void detail() {
        Error error = new Error();
        error.detail("TestDetail");
        assertEquals("TestDetail", error.getDetail());
    }

    @Test
    void objectMethods() {
        Error error1 = new Error().status(200).type(URI.create("http://example.com")).title("title").detail("detail");
        Error error2 = new Error().status(200).type(URI.create("http://example.com")).title("title").detail("detail");
        Error error3 = new Error().status(404).type(URI.create("http://test.com")).title("test").detail("test");

        assertEquals(error1, error2);
        assertNotEquals(error1, error3);
        assertEquals(error1.hashCode(), error2.hashCode());
        assertNotEquals(error1.hashCode(), error3.hashCode());
        assertTrue(error1.toString().contains("200"));
    }
}
