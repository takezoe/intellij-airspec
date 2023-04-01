package wvlet.airspec;

import sbt.testing.Event;
import sbt.testing.Status;
import wvlet.airspec.runner.AirSpecEventHandler;

import static wvlet.airspec.TestRunnerUtil.*;

public class AirSpecTestRunnerEventHandler extends AirSpecEventHandler {

    private TestScopeManager testScopeManager;

    public AirSpecTestRunnerEventHandler(TestScopeManager testScopeManager) {
        this.testScopeManager = testScopeManager;
    }

    @Override
    public void handle(Event event) {
        System.out.println(event);

        if (event.status() == Status.Pending || event.status() == Status.Ignored || event.status() == Status.Skipped || event.status() == Status.Canceled) {
            TestScopeManager.TestCase current = testScopeManager.getCurrent();
            if (!current.name.equals(event.fullyQualifiedName())) {
                int parentId = current.id;
                current = testScopeManager.beginScope(event.fullyQualifiedName());

                reportMessage(
                        String.format(
                                "##teamcity[testStarted name='%s' nodeId='%d' parentNodeId='%d']",
                                escapeString(event.fullyQualifiedName()),
                                current.id,
                                parentId
                        )
                );
            }
            testScopeManager.finishScope();

            reportMessage(
                    String.format(
                            "##teamcity[testIgnored name='%s' message='%s' nodeId='%d']",
                            escapeString(event.fullyQualifiedName()),
                            escapeString(event.throwable().get().toString()),
                            current.id
                    )
            );
            reportMessage(
                    String.format(
                            "##teamcity[testFinished name='%s' nodeId='%d']",
                            escapeString(event.fullyQualifiedName()),
                            current.id
                    )
            );

        } else if (event.status() == Status.Failure || event.status() == Status.Error) {
            TestScopeManager.TestCase current = testScopeManager.getCurrent();
            if (!current.name.equals(event.fullyQualifiedName())) {
                int parentId = current.id;
                current = testScopeManager.beginScope(event.fullyQualifiedName());

                reportMessage(
                        String.format(
                                "##teamcity[testStarted name='%s' nodeId='%d' parentNodeId='%d']",
                                escapeString(event.fullyQualifiedName()),
                                current.id,
                                parentId
                        )
                );
            }
            testScopeManager.finishScope();

            reportMessage(
                    String.format(
                            "##teamcity[testFailed name='%s' message='%s' details='%s' nodeId='%d'%s]",
                            escapeString(event.fullyQualifiedName()),
                            escapeString(event.throwable().get().toString()),
                            escapeString(getStacktrace(event.throwable().get())),
                            current.id,
                            event.status() == Status.Error ? " error='true'" : ""
                    )
            );
            reportMessage(
                    String.format(
                            "##teamcity[testFinished name='%s' nodeId='%d']",
                            escapeString(event.fullyQualifiedName()),
                            current.id
                    )
            );
            testScopeManager.setError();

        } else  if (event.status() == Status.Success) {
            TestScopeManager.TestCase current = testScopeManager.getCurrent();
            if (!current.name.equals(event.fullyQualifiedName())) {
                int parentId = current.id;
                current = testScopeManager.beginScope(event.fullyQualifiedName());

                reportMessage(
                        String.format(
                                "##teamcity[testStarted name='%s' nodeId='%d' parentNodeId='%d']",
                                escapeString(event.fullyQualifiedName()),
                                current.id,
                                parentId
                        )
                );
            }
            testScopeManager.finishScope();

            if (current.error) {
                reportMessage(
                        String.format(
                                "##teamcity[testFailed name='%s' nodeId='%d' error='true']",
                                escapeString(event.fullyQualifiedName()),
                                current.id
                        )
                );
            }

            reportMessage(
                    String.format(
                            "##teamcity[testFinished name='%s' nodeId='%d']",
                            escapeString(event.fullyQualifiedName()),
                            current.id
                    )
            );
        }
    }
}
