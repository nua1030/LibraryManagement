import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LibraryMainTest {
    private InputStream originalIn;
    private PrintStream originalOut;

    @AfterEach
    void tearDown() {
        if (originalIn != null) {
            System.setIn(originalIn);
        }
        if (originalOut != null) {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("종료 확인에서 잘못된 입력 후 재입력 처리")
    void handleExit_repromptsOnInvalidInput() throws Exception {
        originalIn = System.in;
        originalOut = System.out;

        ByteArrayInputStream testIn = new ByteArrayInputStream("maybe\nn\n".getBytes());
        ByteArrayOutputStream testOut = new ByteArrayOutputStream();

        System.setIn(testIn);
        System.setOut(new PrintStream(testOut));

        Field managerField = LibraryMain.class.getDeclaredField("manager");
        managerField.setAccessible(true);
        managerField.set(null, new LibraryManager(new LibraryRepository()) {
            @Override
            public void saveChanges() {
            }
        });

        Method handleExit = LibraryMain.class.getDeclaredMethod("handleExit");
        handleExit.setAccessible(true);
        handleExit.invoke(null);

        String output = testOut.toString();
        assertTrue(output.contains("[오류] 'Y' 또는 'N' 중 하나를 입력해주세요."));
        assertTrue(output.contains("종료를 취소했습니다."));
    }

    @Test
    @DisplayName("로그인 화면에서 종료 입력 시 빠져나오기")
    void performLogin_returnsFalseWhenQuitTokenEntered() throws Exception {
        originalIn = System.in;
        originalOut = System.out;

        ByteArrayInputStream testIn = new ByteArrayInputStream("q\n".getBytes());
        ByteArrayOutputStream testOut = new ByteArrayOutputStream();

        System.setIn(testIn);
        System.setOut(new PrintStream(testOut));

        Field managerField = LibraryMain.class.getDeclaredField("manager");
        managerField.setAccessible(true);
        managerField.set(null, new LibraryManager(new LibraryRepository()));

        Method performLogin = LibraryMain.class.getDeclaredMethod("performLogin");
        performLogin.setAccessible(true);
        boolean result = (boolean) performLogin.invoke(null);

        String output = testOut.toString();
        assertFalse(result);
        assertTrue(output.contains("로그인을 종료합니다."));
    }
}