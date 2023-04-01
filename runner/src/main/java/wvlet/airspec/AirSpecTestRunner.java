package wvlet.airspec;

import sbt.testing.Selector;
import sbt.testing.SubclassFingerprint;
import sbt.testing.TaskDef;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;
import wvlet.airspec.runner.AirSpecLogger;
import wvlet.airspec.runner.AirSpecSbtRunner;
import wvlet.airspec.runner.AirSpecTaskRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static wvlet.airspec.TestRunnerUtil.*;

public class AirSpecTestRunner {
    public static void main(String[] args) {
        System.out.println("==== AirSpecRunner ====");
        for (String arg: args) {
            System.out.println(arg);
        }
        List<String> params = new ArrayList<>();
        String className = null;
        String testName = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-s")) {
                className = args[i + 1];
                i++;
            } else if (args[i].equals("-testName")) {
                testName = args[i + 1];
                i++;
            }
        }
        runSingleTest(className, testName);
    }

    private static void runSingleTest(String className, String testName) {
        TaskDef taskDef = new TaskDef(className, new AirSpecClassFingerPrint(), true, new Selector[]{});

        AirSpecSbtRunner.AirSpecConfig config = null;
        if (testName == null) {
            config = new AirSpecSbtRunner.AirSpecConfig(new String[]{});
        } else {
            config = new AirSpecSbtRunner.AirSpecConfig(new String[]{testName});
        }

        AirSpecTaskRunner runner = new AirSpecTaskRunner(
                taskDef,
                config,
                new AirSpecLogger(),
                new AirSpecTestRunnerEventHandler(),
                AirSpecTestRunner.class.getClassLoader()
        );
        reportMessage(String.format("##teamcity[testSuiteStarted name='%s' nodeId='1' parentNodeId='0']", escapeString(className)));
        Future<BoxedUnit> f = runner.runTask();

        try {
            Await.result(f, Duration.Inf());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        reportMessage(String.format("##teamcity[testSuiteFinished name='%s' nodeId='1']", escapeString(className)));

        System.exit(0);
    }

    public static class AirSpecClassFingerPrint implements SubclassFingerprint {
        @Override
        public boolean isModule() {
            return false;
        }

        @Override
        public String superclassName() {
            return "wvlet.airspec.AirSpec";
        }

        @Override
        public boolean requireNoArgConstructor() {
            return true;
        }
    }
}
