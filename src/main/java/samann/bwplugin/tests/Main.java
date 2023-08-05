package samann.bwplugin.tests;

import org.bukkit.GameMode;

public class Main {
    public static void main(String[] args) {
        System.out.println(GameMode.ADVENTURE);
    }

    public static class SuperClass{
        public void method(){
            System.out.println("SuperClass");
        }
        public void runMethod(){
            method();
        }
    }
    public static class SubClass extends SuperClass{
        @Override
        public void method(){
            System.out.println("SubClass");
        }
    }
}
