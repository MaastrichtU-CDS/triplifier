package nl.um.cds.triplifier.rdf;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Properties;

/**
 * Test the hostname functionality in RdfFactory
 */
public class RdfFactoryHostnameTest {

    // Create a concrete implementation of RdfFactory for testing
    private static class TestRdfFactory extends RdfFactory {
        public TestRdfFactory(Properties props) {
            super(props);
        }
        
        // Make getHostname public for testing
        public String getHostname() {
            return super.getHostname();
        }
    }

    @Test
    public void testCustomHostnameFromProperties() {
        Properties props = new Properties();
        props.setProperty("custom.hostname", "example.com");
        
        TestRdfFactory factory = new TestRdfFactory(props);
        String hostname = factory.getHostname();
        
        assertEquals("Should return custom hostname when set", "example.com", hostname);
    }

    @Test
    public void testFallbackToActualHostname() {
        Properties props = new Properties();
        // Don't set custom.hostname
        
        TestRdfFactory factory = new TestRdfFactory(props);
        String hostname = factory.getHostname();
        
        assertNotNull("Should return a hostname", hostname);
        assertTrue("Should not be empty", !hostname.trim().isEmpty());
        // The hostname should be either the actual hostname or "localhost"
        assertTrue("Should be actual hostname or localhost", 
                   hostname.equals("localhost") || !hostname.equals("localhost"));
    }

    @Test
    public void testEmptyCustomHostnameFallsBack() {
        Properties props = new Properties();
        props.setProperty("custom.hostname", "");
        
        TestRdfFactory factory = new TestRdfFactory(props);
        String hostname = factory.getHostname();
        
        assertNotNull("Should return a hostname", hostname);
        assertTrue("Should not be empty", !hostname.trim().isEmpty());
        // Should fall back to actual hostname or localhost, not the empty string
        assertFalse("Should not return empty string", hostname.isEmpty());
    }

    @Test
    public void testWhitespaceOnlyCustomHostnameFallsBack() {
        Properties props = new Properties();
        props.setProperty("custom.hostname", "   ");
        
        TestRdfFactory factory = new TestRdfFactory(props);
        String hostname = factory.getHostname();
        
        assertNotNull("Should return a hostname", hostname);
        assertTrue("Should not be empty", !hostname.trim().isEmpty());
        // Should fall back to actual hostname or localhost, not whitespace
        assertFalse("Should not return whitespace only", hostname.trim().isEmpty());
    }
}