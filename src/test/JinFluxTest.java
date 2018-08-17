package test;

import org.influxdb.dto.Point;
import org.jlab.coda.jinflux.JinFlux;
import org.jlab.coda.jinflux.JinFluxException;
import org.jlab.coda.jinflux.JinTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Class description here....
 * <p>
 *
 * @author gurjyan
 *         Date 4/19/16
 * @version 3.x
 */
public class JinFluxTest extends JinFlux {

    private JinFluxTest(String dbNode, String dbName) throws JinFluxException {
        super(dbNode);

        try {
            if (!existsDB(dbName)) {
                createDB(dbName, 1, JinTime.HOURE);
            }

        } catch (Exception e) {
            boolean jinFxConnected = false;
        }
    }

    public void push() {
        Map<String, String> tags = new HashMap<>();
        tags.put("TAG1", "SAKO");
        tags.put("TAG2", "KARO");

        Point.Builder p = openTB("myTest", tags);
        addDP(p, "Kukuk", "ROC");
        try {
            write("clara", p);
        } catch (JinFluxException e) {
            e.printStackTrace();
        }
        System.out.println("DDD done.");

    }

    public static void main(String[] args) {
        try {

            JinFluxTest jinFluxTest = new JinFluxTest("claraweb.jlab.org", "clara");

            for (int i = 0; i < 100; i++) {
                jinFluxTest.push();
               Thread.sleep(1000);
                System.out.println("sending.... "+i);

            }
        } catch (JinFluxException | InterruptedException e) {
            e.printStackTrace();
        }


    }
}
