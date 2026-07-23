package models.entities.zombies.behavior.context;

public class AllStarContext {
    private boolean hasTackled = false;

    public boolean hasTackled() {
        return hasTackled;
    }

    public void setTackled() {
        this.hasTackled = true;
    }
}
