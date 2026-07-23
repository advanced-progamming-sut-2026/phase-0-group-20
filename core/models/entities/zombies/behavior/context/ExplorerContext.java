package models.entities.zombies.behavior.context;

public class ExplorerContext {
    private boolean isTorchLit = true;

    public boolean isTorchLit() {
        return isTorchLit;
    }

    public void setTorchLit(boolean torchLit) {
        this.isTorchLit = torchLit;
    }
}
