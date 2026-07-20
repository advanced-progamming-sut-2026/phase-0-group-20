
import models.App;
import models.game.GameSession;
import models.game.adventure.Adventure;
import views.AppView;



public class Main {
    public static void main(String[] args) {
        GameInitializer.loadAllResources();
        AppView.run();
    }
}
