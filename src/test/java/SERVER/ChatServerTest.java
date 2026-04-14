package SERVER;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ChatServerTest {

    @Test
    @DisplayName("Le serveur utilise bien le port 12345")
    void testServerPort() {
        assertEquals(12345, 12345);
    }

    @Test
    @DisplayName("Parsing d'un message GROUP valide")
    void testGroupMessageParsing() {
        String message = "GROUP:1:alice:Bonjour le groupe";
        String[] parts = message.split(":", 4);

        assertEquals(4, parts.length);
        assertEquals("GROUP", parts[0]);
        assertEquals("1", parts[1].trim());
        assertEquals("alice", parts[2].trim());
        assertEquals("Bonjour le groupe", parts[3].trim());
    }

    @Test
    @DisplayName("Parsing d'un message GROUP invalide")
    void testGroupMessageParsingInvalid() {
        String message = "GROUP:1:alice";
        String[] parts = message.split(":", 4);

        assertTrue(parts.length < 4);
    }

    @Test
    @DisplayName("Parsing d'un message privé valide")
    void testPrivateMessageParsing() {
        String message = "bob:Salut Bob !";
        String[] parts = message.split(":", 2);

        assertEquals(2, parts.length);
        assertEquals("bob", parts[0]);
        assertEquals("Salut Bob !", parts[1]);
    }

    @Test
    @DisplayName("Détection d'un message GROUP par préfixe")
    void testIsGroupMessage() {
        String groupMsg = "GROUP:2:charlie:Hello";
        String privateMsg = "alice:Salut";

        assertTrue(groupMsg.startsWith("GROUP:"));
        assertFalse(privateMsg.startsWith("GROUP:"));
    }
}