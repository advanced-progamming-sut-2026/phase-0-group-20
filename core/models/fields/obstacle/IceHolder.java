package models.fields.obstacle;

public interface IceHolder {
    boolean hasIceBlock();

    IceBlock getIceBlock();

    void setIceBlock(IceBlock iceBlock);

    default boolean isBlockedByIce() {
        return hasIceBlock();
    }

    void removeIceBlock();

    default void takeIceDamage(int damage) {
        IceBlock iceBlock = getIceBlock();
        if (iceBlock == null) return;

        iceBlock.takeDamage(damage);

        if (iceBlock.getIceHp() <= 0) removeIceBlock();
    }
}