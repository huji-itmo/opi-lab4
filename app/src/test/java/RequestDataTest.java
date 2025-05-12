import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import beans.RequestData;
import exceptions.*;

class RequestDataTest {

    @Test
    void testValidData() throws Exception {
        RequestData data = new RequestData(0L, 0.0, 3L);
        assertDoesNotThrow(data::throwIfBadData);
    }

    @Test
    void testMissingParameters() {
        RequestData data = new RequestData(null, 0.0, 3L);
        assertThrows(MissingParametersException.class, data::throwIfBadData);
    }

    @Test
    void testInvalidR() {
        RequestData data = new RequestData(0L, 0.0, 6L);
        assertThrows(BadParameterException.class, data::throwIfBadData);
    }

    @Test
    void testInvalidX() {
        RequestData data = new RequestData(4L, 0.0, 3L);
        assertThrows(BadParameterException.class, data::throwIfBadData);
    }

    @Test
    void testInvalidY() {
        RequestData data = new RequestData(0L, 6.0, 3L);
        assertThrows(BadParameterException.class, data::throwIfBadData);
    }
}
