package models.users;

import models.items.Item;

import java.util.ArrayList;

public class Inventory {
    private ArrayList<Item> items;

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }
}
