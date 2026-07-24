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
    public void testEnterMenuValidTargetCaseInsensitiveReturnsSuccess() {
        Result result = NavigationController.enterMenu("GAME");
        assertTrue(result.isSuccessful());
        assertEquals("entered game menu", result.message());
        assertEquals(Menu.GAME_MENU, App.getActiveMenu());
    }

    @Test
    public void testEnterMenuNonExistentMenuReturnsFailure() {
        Result result = NavigationController.enterMenu("nonexistent menu");
        assertFalse(result.isSuccessful());
        assertEquals("no such menu exists", result.message());
        assertEquals(Menu.MAIN_MENU, App.getActiveMenu());
    }

    @Test
    public void testEnterMenuNotAllowedFromCurrentMenuReturnsFailure() {
        Result result = NavigationController.enterMenu("collection menu");
        assertFalse(result.isSuccessful());
        assertEquals("you cannot enter this menu from here", result.message());
        assertEquals(Menu.MAIN_MENU, App.getActiveMenu());
    }

    @Test
    public void testExitMenuFromLoginMenuGoesToSignupMenu() {
        App.setActiveMenu(Menu.LOGIN_MENU);
        Result result = NavigationController.exitMenu();
        assertTrue(result.isSuccessful());
        assertEquals(Menu.SIGNUP_MENU, App.getActiveMenu());
        assertEquals("exited to signup menu", result.message());
    }

    @Test
    public void testEnterMenuFromSignupMenuToLoginMenuReturnsSuccess() {
        App.setActiveMenu(Menu.SIGNUP_MENU);
        Result result = NavigationController.enterMenu("login menu");
        assertTrue(result.isSuccessful());
        assertEquals(Menu.LOGIN_MENU, App.getActiveMenu());
    }

    @Test
    public void testEnterMenuFromGameMenuToCollectionMenuReturnsSuccess() {
        App.setActiveMenu(Menu.GAME_MENU);
        Result result = NavigationController.enterMenu("collection menu");
        assertTrue(result.isSuccessful());
        assertEquals(Menu.COLLECTION_MENU, App.getActiveMenu());
    }

    @Test
    public void testEnterMenuFromProfileMenuToPlantSelectionMenuReturnsSuccess() {
        App.setActiveMenu(Menu.PROFILE_MENU);
        Result result = NavigationController.enterMenu("plant selection menu");
        assertTrue(result.isSuccessful());
        assertEquals(Menu.PLANTSELLECTION_MENU, App.getActiveMenu());
    }

    @Test
    public void testExitMenuFromMainMenuReturnsFailure() {
        Result result = NavigationController.exitMenu();
        assertFalse(result.isSuccessful());
        assertEquals("use the logout command to exit the main menu", result.message());
        assertEquals(Menu.MAIN_MENU, App.getActiveMenu());
    }

    @Test
    public void testExitMenuFromSettingsMenuGoesToMainMenu() {
        App.setActiveMenu(Menu.SETTINGS_MENU);
        Result result = NavigationController.exitMenu();
        assertTrue(result.isSuccessful());
        assertEquals(Menu.MAIN_MENU, App.getActiveMenu());
        assertEquals("exited to main menu", result.message());
    }

    @Test
    public void testExitMenuFromPlantSelectionMenuGoesToGameMenu() {
        App.setActiveMenu(Menu.PLANTSELLECTION_MENU);
        Result result = NavigationController.exitMenu();
        assertTrue(result.isSuccessful());
        assertEquals(Menu.GAME_MENU, App.getActiveMenu());
        assertEquals("exited to game menu", result.message());
    }

    @Test
    public void testShowCurrentMenuAfterNavigationReturnsUpdatedMenu() {
        NavigationController.enterMenu("game menu");
        Result result = NavigationController.showCurrentMenu();
        assertTrue(result.isSuccessful());
        assertEquals("game menu", result.message());
    }

    @Test
    public void testChainedNavigationChainChangingMenuCaseInsensitive() {
        NavigationController.enterMenu("  game ");
        NavigationController.enterMenu(" collectiOn    ");
        assertEquals(Menu.COLLECTION_MENU, App.getActiveMenu());
    }
}