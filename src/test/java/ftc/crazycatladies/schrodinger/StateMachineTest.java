package ftc.crazycatladies.schrodinger;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import ftc.crazycatladies.schrodinger.log.DataLogger;
import ftc.crazycatladies.schrodinger.opmode.OpModeTime;
import ftc.crazycatladies.schrodinger.state.State;
import ftc.crazycatladies.schrodinger.state.StateMachine;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.FileSystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StateMachineTest {
    @BeforeClass
    public static void setup() {
        new File("sdcard").mkdir();

        DataLogger.createDataLogger(new OpModeTime(null) {
            @Override
            public double time() {
                return 0;
            }
        }, "OpMode");
    }

    @Test
    public void testStateObjectsLinearPathNotLooping() {
        final int NUM_OF_STATES = 4;

        StateMachine sm = new StateMachine("testStateObjectsLinearPath");

        for (int i = 0; i < NUM_OF_STATES; i++) {
            State s = sm.add(new State() {
                @Override
                protected void run(Object context) {
                    next();
                }
            });
            assertNotNull(s);
        }

        assertEquals(sm, sm.init());

        for (int i = 0; i < NUM_OF_STATES; i++) {
            assertEquals(false, sm.isDone());
            sm.run();
        }

        assertEquals(true, sm.isDone());
    }

    @Test
    public void testLambdaStates() {
        StateMachine sm = new StateMachine("testLambdaStates");

        sm.once((state, context) -> {
        });
        sm.repeat((state, context) -> {
            state.next();
        });
        sm.init();

        assertEquals(false, sm.isDone());
        sm.run();
        assertEquals(false, sm.isDone());
        sm.run();
        assertEquals(true, sm.isDone());
    }
}
