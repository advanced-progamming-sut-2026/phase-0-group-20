package test.java.controllers.GameController;

import controllers.GameController.SettingController;
import models.App;
import models.Result;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SettingControllerUnitTest {

    private SettingController controller;

    @Before
    public void setUp() {
        App.getSettings().setDifficulty(3);
        controller = new SettingController();
    }


    @Test
    public void testChangeDifficulty_validInteger_settingsUpdated() {
        Result result = controller.changeDifficulty("5");
        assertTrue(result.isSuccessful());
        assertEquals("Difficulty changed successfully to 5", result.message());
        assertEquals(5, App.getSettings().getDifficulty());
    }

    @Test
    public void testChangeDifficulty_nonInteger() {
        Result result = controller.changeDifficulty("abc");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficulty_emptyString() {
        Result result = controller.changeDifficulty("");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficulty_floatInput() {
        Result result = controller.changeDifficulty("2.5");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficulty_invalidInput_settingsNotChanged() {
        controller.changeDifficulty("abc");
        assertEquals(3, App.getSettings().getDifficulty());
    }

    @Test
    public void testChangeDifficulty_aboveRange() {
        Result result = controller.changeDifficulty("6");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficulty_belowRange() {
        Result result = controller.changeDifficulty("0");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficulty_outOfRange_settingsNotChanged() {
        controller.changeDifficulty("6");
        assertEquals(3, App.getSettings().getDifficulty());
    }

    @Test
    public void testChangeDifficulty_boundaryMin() {
        Result result = controller.changeDifficulty("1");
        assertTrue(result.isSuccessful());
        assertEquals("Difficulty changed successfully to 1", result.message());
    }

    @Test
    public void testChangeDifficulty_boundaryMax_returnsSuccess() {
        Result result = controller.changeDifficulty("5");
        assertTrue(result.isSuccessful());
        assertEquals("Difficulty changed successfully to 5", result.message());
    }
}