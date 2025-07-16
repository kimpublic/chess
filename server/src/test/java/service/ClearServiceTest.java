//clear test
package service;

import dataaccess.DataAccessException;
import dataaccess.DataAccessOnMemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private ClearService service;
    private DataAccessOnMemory dataAccessObject;

    @BeforeEach
    void testSetUp() {
        dataAccessObject = new DataAccessOnMemory();
        service = new ClearService(dataAccessObject);
    }

    @Test
    void clearShouldNotThrow() {
        assertDoesNotThrow(() -> service.clear());
    }
}