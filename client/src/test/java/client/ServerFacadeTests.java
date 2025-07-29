package client;

import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {

        server = new Server();
        int port = server.run(0);

        String serverUrl = "http://localhost:" + port;
        facade = new ServerFacade(serverUrl);
        System.out.println("Started test HTTP server on " + port);


    }

    @BeforeEach
    public void clearServer() {
        try {
            facade.clearDatabase();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    public void registerPositive() throws Exception {
        facade.register("user1", "user1password", "user1email@gmail.com");
        Assertions.assertEquals("user1", facade.getCurrentUsername());
    }

    @Test
    public void registerNegative() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            facade.register("user2", "user2password", "user1email@gmail.com");
            facade.register("user2", "user2password", "user1email@gmail.com");
        });
    }

    @Test
    public void loginPositive() throws Exception {
        facade.register("user1", "user1password", "user1email@gmail.com");
        facade.logout();
        facade.login("user1", "user1password");
        Assertions.assertEquals("user1", facade.getCurrentUsername());
    }

    @Test
    public void loginNegative() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            facade.login("userhaha", "hahaha");
        });
    }

    @Test
    public void logoutPositive() throws Exception {
        facade.register("user3", "user3password", "user3email@gmail.com");
        facade.logout();
        assertNull(facade.getCurrentUsername());
    }

    @Test
    public void logoutNegative() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            facade.logout();
        });
    }

    @Test
    public void createGamePositive() throws Exception {
        facade.register("user4", "user4password", "user4email@gmail.com");
        int id = facade.createGame("testGame");
        assertTrue(id > 0);
    }

    @Test
    public void createGameNegative() throws Exception {
        assertThrows(RuntimeException.class, () -> {
            facade.createGame("testGame");
        });
    }

    @Test
    public void listGamePositive() throws Exception {
        facade.register("user5", "user5password", "user5email@gmail.com");
        facade.createGame("testGame1");
        facade.createGame("testGame2");
        List<Map<String, Object>> gameList = facade.listGames();
        assertEquals(2, gameList.size());
    }

    @Test
    public void listGamesNegative() {
        assertThrows(RuntimeException.class, () -> {
            facade.listGames();
        });
    }

    @Test
    public void joinGamePositive() throws Exception {
        facade.register("user6", "user6password", "user6email@gmail.com");
        int id = facade.createGame("testGame3");
        facade.joinGame(id, "WHITE");
        assertTrue(true);
    }

    @Test
    public void joinGameNegative() throws Exception {
        facade.register("user7", "user7password", "user7mail@gmail.com");
        assertThrows(RuntimeException.class, () -> {
            facade.joinGame(9999, "BLACK");
        });
    }
}
