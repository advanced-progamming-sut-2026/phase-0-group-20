
import models.game.GameSession;
import views.AppView;



public class Main {
    public static void main(String[] args) {
        GameInitializer.loadAllResources();
        AppView.run();
    }
}
