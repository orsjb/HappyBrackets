package core.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)

@SuiteClasses({
	ControllerConfigTest.class,
	EnvironmentConfTest.class,
	PIConfigTest.class,
	ControllerAdvertiserTest.class,
	ControllerDiscoveryTest.class,
	LoadableTest.class
})

public class AllTests {

}
