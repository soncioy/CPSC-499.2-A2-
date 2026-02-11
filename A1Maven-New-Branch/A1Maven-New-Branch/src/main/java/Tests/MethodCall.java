class MethodCall {
    void test() {
        System.out.println("Hello"); // Standard chained call
        foo(1, 2);                   // Simple call
        this.bar();                  // Explicit receiver
    }
}