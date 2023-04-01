package wvlet.airspec;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class TestScopeManager {
    private AtomicInteger id = new AtomicInteger(0);
    private Stack<TestCase> stack = new Stack<>();

    public TestScopeManager() {
        stack.push(new TestCase(0, ""));
    }

    public TestCase getCurrent() {
        return stack.peek();
    }

    public TestCase beginScope(String name) {
        int id = this.id.incrementAndGet();
        TestCase testCase = new TestCase(id, name);
        stack.push(testCase);
        return testCase;
    }

    public TestCase finishScope() {
        return stack.pop();
    }

    public void setError() {
        stack.iterator().forEachRemaining(testCase -> testCase.error = true);
    }

    public class TestCase {
        public String name;
        public int id;
        public boolean error = false;

        public TestCase(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
