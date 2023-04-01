package wvlet.airspec;

import wvlet.airspec.runner.AirSpecEvent;
import wvlet.airspec.runner.AirSpecLogger;

import static wvlet.airspec.TestRunnerUtil.escapeString;
import static wvlet.airspec.TestRunnerUtil.reportMessage;

public class AirSpecTestLogger extends AirSpecLogger {

    private TestScopeManager testScopeManager;

    public AirSpecTestLogger(TestScopeManager testScopeManager) {
        this.testScopeManager = testScopeManager;
    }

    @Override
    public void logSpecName(String specName, int indentLevel) {
        super.logSpecName(specName, indentLevel);

        int parentId = testScopeManager.getCurrent().id;
        int currentId = testScopeManager.beginScope(specName).id;

        reportMessage(
                String.format(
                        "##teamcity[testStarted name='%s' nodeId='%d' parentNodeId='%d']",
                        escapeString(specName),
                        currentId,
                        parentId
                )
        );
    }

    @Override
    public void logTestName(String testName, int indentLevel) {
        super.logTestName(testName, indentLevel);

        int parentId = testScopeManager.getCurrent().id;
        int currentId = testScopeManager.beginScope(testName).id;

        reportMessage(
                String.format(
                        "##teamcity[testStarted name='%s' nodeId='%d' parentNodeId='%d']",
                        escapeString(testName),
                        currentId,
                        parentId
                )
        );
    }

    @Override
    public void logEvent(AirSpecEvent e, int indentLevel, boolean showTestName) {
        super.logEvent(e, indentLevel, showTestName);
        System.out.println("** logEvent: " + e);
    }
}
