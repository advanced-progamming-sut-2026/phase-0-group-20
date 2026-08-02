package io.java.pvz;

import io.java.pvz.views.AppView;


public class Main {
    public static void main(String[] args) {
        GameInitializer.loadAllResources();
        AppView.run();
    }
}
