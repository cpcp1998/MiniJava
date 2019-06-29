class Test {
    public static void main (String[] a) {
        System.out.println(new A().start());
    }
}

class B extends A{}

class A{
    A a;
    B b;
    public int start(){
        a = b;
        return 0;
    }
}

class C{
    public int tooManyArguments(int a, int b, int c, int d, int e, int f, int g,
                                int h, int i, int j, int k, int l, int m, int n,
                                int o, int p, int q, int r, int s, int t, int u,
                                int v, int w, int x, int y, int z) {return 0;}
}