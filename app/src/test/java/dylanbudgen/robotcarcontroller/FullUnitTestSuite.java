package dylanbudgen.robotcarcontroller;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class FullUnitTestSuite {

    @Test
    public void foundDevice_functional() {

        FoundDevice test1 = new FoundDevice("3ERFDD", "Car1");
        FoundDevice test2 = new FoundDevice("S900D", "Car2");

        assertEquals(test1.getDeviceAddress(), "3ERFDD");
        assertEquals(test1.getDeviceName(), "Car1");

        assertEquals(test2.getDeviceAddress(),"S900D");
        assertEquals(test2.getDeviceName(), "Car2");

    }
}