package test.java.controllers;

import controllers.NavigationController;
import models.App;
import models.Result;
import models.enums.Menu;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NavigationControllerUnitTest {

    @Before
    public void setUp() {
        App.setActiveMenu(Menu.MAIN_MENU);
    }


    @Test
    public void testEnterMenu_validTarget_caseInsensitive_returnsSuccess() {
        Result result = NavigationController.enterMenu("GAME");
        assertTrue(result.isSuccessful());
        assertEquals("entered game menu", result.message());
        assertEquals(Menu.GAME_MENU, App.getActiveMenu());
    }

    @Test
    public void testEnterMenu_nonExistentMenu_returnsFailure() {
        Result result = NavigationController.enterMenu("nonexistent menu");
        assertFalse(result.isSuccessful());
        assertEquals("no such menu exists", result.message());
        assertEquals(Menu.MAIN_MENU, App.getActiveMenu());
    }

    @Test
    public void testEnterMenu_notAllowedFromCurrentMenu_returnsFailure() {
        Result result = NavigationController.enterMenu("collection menu");
        assertFalse(result.isSuccessful());
        assertEquals("you cannot enter this menu from here", result.message());
        assertEquals(Menu.MAIN_MENU, App.getActiveMenu());
    }

    @Test
    public void testExitMenu_fromLoginMenu_goesToSignupMenu() {
        App.setActiveMenu(Menu.LOGIN_MENU);
        Result result = NavigationController.exitMenu();
        assertTrue(result.isSuccessful());
        assertEquals(Menu.SIGNUP_MENU, App.getActiveMenu());
        assertEquals("exited to signup menu", result.message());
    }

    @Test
    public void testEnterMenu_fromSignupMenu_toLoginMenu_returnsSuccess() {
        App.setActiveMenu(Menu.SIGNUP_MENU);
        Result result = NavigationController.enterMenu("login menu");
        assertTrue(result.isSuccessful());
        assertEquals(Menu.LOGIN_MENU, App.getActiveMenu());
    }

    @Test
    public void testEnterMenu_fromGameMenu_toCollectionMenu_returnsSuccess() {
        App.setActiveMenu(Menu.GAME_MENU);
        Result result = NavigationController.enterMenu("collection menu");
        assertTrue(result.isSuccessful());
        assertEquals(Menu.COLLECTION_MENU, App.getActiveMenu());
    }

    @Test
    public void testEnterMenu_fromProfileMenu_toPlantSelectionMenu_returnsSuccess() {
        App.setActiveMenu(Menu.PROFILE_MENU);
        Result result = NavigationController.enterMenu("plant selection menu");
        assertTrue(result.isSuccessful());
        assertEquals(Menu.PLANTSELLECTION_MENU, App.getActiveMenu());
    }

    @Test
    public void testExitMenu_fromMainMenu_returnsFailure() {
        Result result = NavigationController.exitMenu();
        assertFalse(result.isSuccessful());
        assertEquals("use the logout command to exit the main menu", result.message());
        assertEquals(Menu.MAIN_MENU, App.getActiveMenu());
    }

    @Test
    public void testExitMenu_fromSettingsMenu_goesToMainMenu() {
        App.setActiveMenu(Menu.SETTINGS_MENU);
        Result result = NavigationController.exitMenu();
        assertTrue(result.isSuccessful());
        assertEquals(Menu.MAIN_MENU, App.getActiveMenu());
        assertEquals("exited to main menu", result.message());
    }

    @Test
    public void testExitMenu_fromPlantSelectionMenu_goesToGameMenu() {
        App.setActiveMenu(Menu.PLANTSELLECTION_MENU);
        Result result = NavigationController.exitMenu();
        assertTrue(result.isSuccessful());
        assertEquals(Menu.GAME_MENU, App.getActiveMenu());
        assertEquals("exited to game menu", result.message());
    }

    @Test
    public void testShowCurrentMenu_afterNavigation_returnsUpdatedMenu() {
        NavigationController.enterMenu("game menu");
        Result result = NavigationController.showCurrentMenu();
        assertTrue(result.isSuccessful());
        assertEquals("game menu", result.message());
    }

    @Test
    public void testChainedNavigation_chainChangingMenu_caseInsensitive() { // two times changing
        NavigationController.enterMenu("  game ");
        NavigationController.enterMenu(" collectiOn    ");
        assertEquals(Menu.COLLECTION_MENU, App.getActiveMenu());
    }

}