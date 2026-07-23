package test.java.controllers.GameController;

import controllers.GameController.SettingController;
import models.App;
import models.Result;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SettingControllerUnitTest {

    private SettingController controller;

    @Before
    public void setUp() {
        App.getSettings().setDifficulty(3);
        controller = new SettingController();
    }

    @Test
    public void testChangeDifficultyValidIntegerSettingsUpdated() {
        Result result = controller.changeDifficulty("5");
        assertTrue(result.isSuccessful());
        assertEquals("Difficulty changed successfully to 5", result.message());
        assertEquals(5, App.getSettings().getDifficulty());
    }

    @Test
    public void testChangeDifficultyNonInteger() {
        Result result = controller.changeDifficulty("abc");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficultyEmptyString() {
        Result result = controller.changeDifficulty("");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficultyFloatInput() {
        Result result = controller.changeDifficulty("2.5");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficultyInvalidInputSettingsNotChanged() {
        controller.changeDifficulty("abc");
        assertEquals(3, App.getSettings().getDifficulty());
    }

    @Test
    public void testChangeDifficultyAboveRange() {
        Result result = controller.changeDifficulty("6");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficultyBelowRange() {
        Result result = controller.changeDifficulty("0");
        assertFalse(result.isSuccessful());
        assertEquals("Invalid difficulty (Must be an integer [1-5])", result.message());
    }

    @Test
    public void testChangeDifficultyOutOfRangeSettingsNotChanged() {
        controller.changeDifficulty("6");
        assertEquals(3, App.getSettings().getDifficulty());
    }

    @Test
    public void testChangeDifficultyBoundaryMin() {
        Result result = controller.changeDifficulty("1");
        assertTrue(result.isSuccessful());
        assertEquals("Difficulty changed successfully to 1", result.message());
    }

    @Test
    public void testChangeDifficultyBoundaryMaxReturnsSuccess() {
        Result result = controller.changeDifficulty("5");
        assertTrue(result.isSuccessful());
        assertEquals("Difficulty changed successfully to 5", result.message());
    }
}