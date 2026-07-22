package views;

import controllers.GameController.GameMenuController;
import models.enums.commands.GameMenuCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class GameMenu implements AppMenu {
    private final GameMenuController controller = new GameMenuController();

    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();


        Matcher matcher;


        if ((matcher = GameMenuCommands.ENTER_CHAPTER.getMatcher(input)) != null) {
            String chapterStr = matcher.group("chaptername");
            System.out.println(controller.enterChapter(chapterStr));
        } else if (checkGlobalCommand(input)) {
            return;
        }

        else if ((matcher = GameMenuCommands.ENTER_LEADERBOARD.getMatcher(input)) != null) {
            System.out.println(controller.enterLeaderboard());
        } else if ((matcher = GameMenuCommands.ENTER_COIN_WALLET.getMatcher(input)) != null) {
            System.out.println(controller.showCoin());
        } else if ((matcher = GameMenuCommands.ENTER_GEM_WALLET.getMatcher(input)) != null) {
            System.out.println(controller.showGem());
        } else if ((matcher = GameMenuCommands.CHEAT_ADD.getMatcher(input)) != null) {
            int amount = Integer.parseInt(matcher.group("amount")); // bug for string
            String type = matcher.group("type");
            System.out.println(controller.cheat(amount, type));
        } else {
            invalidCommands();
        }
    }

}
