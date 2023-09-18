package samann.bwplugin.tests;

import org.bukkit.GameMode;

public class Main {
    public static void main(String[] args) {final double gravity = 32d / (20 * 20);
        final double minBoostHeight = 10d;
        final double maxBoostHeight = 10d;
        final double maxBoostDistance = 20;

        double minVerticalSpeed = Math.sqrt(2 * gravity * minBoostHeight);
        double maxVerticalSpeed = Math.sqrt(2 * gravity * maxBoostHeight);
        double verticalSpeed = minVerticalSpeed;
        System.out.println(verticalSpeed);
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
