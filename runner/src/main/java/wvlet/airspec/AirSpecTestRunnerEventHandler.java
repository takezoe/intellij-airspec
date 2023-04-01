package wvlet.airspec;

import sbt.testing.Event;
import sbt.testing.Status;
import wvlet.airspec.runner.AirSpecEventHandler;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import static wvlet.airspec.TestRunnerUtil.*;

public class AirSpecTestRunnerEventHandler extends AirSpecEventHandler {

    private AtomicInteger id = new AtomicInteger(1);
//    private final Stack<Integer> idStack = new Stack<>();

//    public AirSpecTestRunnerEventHandler() {
//        descend();
//        descend();
//    }

//    private int getCurrentId() {
//        return idStack.peek();
//    }
//
//    private int descend() {
//        if (idStack.isEmpty()) {
//            //attach to root
//            idStack.push(0);
//        }
//        int oldId = idStack.peek();
//        idStack.push(id.incrementAndGet());
//        return oldId;
//    }
//
//    private void ascend() {
//        idStack.pop();
//    }

    @Override
    public void handle(Event event) {
        if (event.status() == Status.Error) {

        } else if (event.status() == Status.Skipped) {
        } else if (event.status() == Status.Ignored) {
        } else if (event.status() == Status.Pending) {
        } else if (event.status() == Status.Canceled) {
        } else if (event.status() == Status.Failure) {
            System.out.println("fail: " + event.fullyQualifiedName());
            int currentId = id.incrementAndGet();
            reportMessage(
                    String.format(
                            "##teamcity[testStarted name='%s' nodeId='%d' parentNodeId='%d']",
                            escapeString(event.fullyQualifiedName()),
                            currentId,
                            1
                    )
            );
            reportMessage(
                    String.format(
                            "##teamcity[testFailed name='%s' message='%s' details='%s' nodeId='%d']",
                            escapeString(event.fullyQualifiedName()),
                            escapeString(event.throwable().get().toString()),
                            escapeString(getStacktrace(event.throwable().get())),
                            currentId
                    )
            );
            reportMessage(
                    String.format(
                            "##teamcity[testFinished name='%s' nodeId='%d']",
                            escapeString(event.fullyQualifiedName()),
                            currentId
                    )
            );
        } else  if (event.status() == Status.Success) {
            System.out.println("success: " + event.fullyQualifiedName());
            int currentId = id.incrementAndGet();
            reportMessage(
                    String.format(
                            "##teamcity[testStarted name='%s' nodeId='%d' parentNodeId='%d']",
                            escapeString(event.fullyQualifiedName()),
                            currentId,
                            1
                    )
            );
            reportMessage(
                    String.format(
                            "##teamcity[testFinished name='%s' nodeId='%d']",
                            escapeString(event.fullyQualifiedName()),
                            currentId
                    )
            );
        }
    }
}
