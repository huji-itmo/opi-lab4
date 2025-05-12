import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import beans.CoordinateSpace;
import beans.RequestData;

class CoordinateSpaceTest {

    @Test
    void testHitTopBatman() {
        RequestData data = new RequestData(0L, 1.0, 5L); // Проверяем точку внутри верхней части
        assertTrue(CoordinateSpace.testHit(data));
    }

    @Test
    void testHitBottomBatman() {
        RequestData data = new RequestData(0L, -1.0, 3L); // Проверяем точку внутри нижней части
        assertTrue(CoordinateSpace.testHit(data));
    }

    @Test
    void testHitOutside() {
        RequestData data = new RequestData(0L, 10.0, 1L); // Точка за пределами фигуры
        assertFalse(CoordinateSpace.testHit(data));
    }

    @Test
    void testTopBatmanFirstSegment() {
        assertEquals(9 * 2 / 28.0, CoordinateSpace.topBatman(0.0, 2));
    }

}
