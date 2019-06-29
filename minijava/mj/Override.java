class Factorial{
    public static void main(String[] a){
        System.out.println(0);
    }
}

class A {
    public int f(int b, int c) { return 0; }
    public int g(int a) {return 0;}
    public A h() {return new A();}
}

class B extends A {
    public int f(int a, int c) { return 1; }
}

class C extends B {
    public int f(B a, int c) {return new B();}
    public int g() {return 0;}
    public int h() {return 0;}
}
