import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import beans.*;
import exceptions.*;

class GraphStateBeanTest {

    @InjectMocks
    private GraphStateBean graphStateBean;

    @Mock
    private DataBaseBean dataBaseBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        graphStateBean = new GraphStateBean();
        graphStateBean.setDataBaseBean(dataBaseBean);
    }

    @Test
    void testAddValidPoint() {
        // Настройка
        graphStateBean.setXToSend(0L);
        graphStateBean.setYToSend(0.0);
        graphStateBean.setRToSend(3L);

        // Вызов
        graphStateBean.addPointToDatabase();

        // Проверка
        assertNotNull(graphStateBean.getCachedHit());
        verify(dataBaseBean, times(1)).addPointToDatabase(any(HitResult.class));
    }

    @Test
    void testFormInvalid() {
        graphStateBean.setXToSend(null);
        graphStateBean.addPointToDatabase();
        assertNull(graphStateBean.getCachedHit());
    }
}
