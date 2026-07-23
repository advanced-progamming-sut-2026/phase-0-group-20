package models.entities.zombies.behavior.context;

public class SnorkelContext {
    private boolean isSubmerged = false;

    public boolean isSubmerged() {
        return isSubmerged;
    }

    public void setSubmerged(boolean submerged) {
        this.isSubmerged = submerged;
    }
}
