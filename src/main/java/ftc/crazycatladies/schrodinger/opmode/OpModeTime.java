package ftc.crazycatladies.schrodinger.opmode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * Allows classes to use the OpMode time without access to other OpMode stuff
 */
public class OpModeTime {
    private final OpMode opMode;

    public OpModeTime(OpMode opMode) {
        this.opMode = opMode;
    }

    public double time() {
        return opMode.time;
    }
}
