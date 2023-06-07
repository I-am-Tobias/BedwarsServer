package samann.bwplugin.tests;

public class Main {
    public static void main(String[] args) {
        SuperClass obj = new SubClass();
        obj.method();
        obj.runMethod();
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
