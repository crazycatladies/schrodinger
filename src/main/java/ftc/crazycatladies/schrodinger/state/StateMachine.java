package ftc.crazycatladies.schrodinger.state;

import com.qualcomm.robotcore.util.RobotLog;

import org.json.JSONObject;

import ftc.crazycatladies.schrodinger.log.DataLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * State machine which executes the actions specified in a number of {@link State} objects. Status follows
 * transitions which are indicated by the states during execution. A context object can be provided to the
 * state machine and passed between states as a means of providing information to states, or allowing them
 * to pass it between them.<br>
 * <br>
 * Typical use involves building the state machine and then running repeatedly:<br><br>
 * <pre>
 * sm = new StateMachine("A button countdown");
 * sm.repeat((state, seconds) -&gt; {
 *     if (gamepad1.a) {
 *         state.next();
 *     }
 * });
 * sm.repeat(((state, seconds) -&gt; {
 *     double secsInState = state.getTimeInState().seconds();
 *     telemetry.addData("sm-countdown", (int)(seconds - secsInState));
 *     if (secsInState &gt; seconds) {
 *         state.next();
 *     }
 * }));
 * sm.setLooping(true);
 * sm.init(SECONDS);
 *
 * while (opModeIsActive()) {
 *     sm.run();
 * }
 * </pre>
 * @param <T> type of the state machine's context object
 */
public class StateMachine<T> {
    private String name;
    private List<State> states = new ArrayList<State>();
    private Map<String, State> stateNameMap = new HashMap<>();
    private State currentState;
    private boolean isDone;
    private T context;
    private DataLogger logger;
    private String lastContext;
    private int execNum = 0;
    private boolean looping = false;

    /**
     * @param name name of the state machine, used in logging
     */
    public StateMachine(String name) {
        this.name = name;
    }

    /**
     * Adds a state as the next in the sequence
     * instantiating states and indicating that only one state can be added at one time
     * @param state the state to be added this state machine's sequence
     * @throws RuntimeException if an attempt is made to add a state more than once
     * @return the state that was added
     */
     public State<T> add(State<T> state) {
        if (states.indexOf(state) > -1)
            throw new RuntimeException("States cannot be added more than once");

        states.add(state);

        populateStateNameMap();

        return state;
    }

    /**
     * Add all the states from another state machine
     * @param sm another state machine from which to add all the states
     * @return this state machine
     */
    public StateMachine<T> addAll(StateMachine sm) {
        states.addAll(sm.states);

        populateStateNameMap();

        return this;
    }

    private void populateStateNameMap() {
        stateNameMap.clear();
        for (State s : states) {
            if (s.name != null) {
                stateNameMap.put(s.name, s);
            }
        }
    }

    /**
     * Create a state, which repeats by default, and add it to this state machine
     * from a StateFunction lambda expression
     * @param function lambda expression to wrap with a State object
     * @return the created state which has been added to the state machine
     */
    public State<T> repeat(StateFunction<T> function) {
        return add(State.create(function));
    }

    public State<T> repeat(String name, StateFunction<T> function) { return add(State.create(name, function)); }

    /**
     * Create a state, which exits after one execution, and add it to this state machine
     * from a StateFunction lambda expression
     * @param function lambda expression to wrap with a State object
     * @return the created state which has been added to the state machine
     */
    public State<T> once(StateFunction<T> function) {
        return add(State.createOnce(function));
    }

    public State<T> once(String name, StateFunction<T> function) {
        return add(State.createOnce(name, function));
    }

    public State<T> complete(StateFunction<T> waitFunc, StateFunction<T> doFunc) {
        State<T> doState = add(State.createOnce(doFunc));
        add(State.create(waitFunc));
        return doState;
    }

    public StateMachine<T> init() {
        return init(null);
    }

    /**
     * Sets up the state machine to run. Must be called before running the state machine.
     * Intended to be executed during robot initialization, or to reset for another execution
     * @param context data to be provided to states or updated by states
     * @return this state machine
     */
    public StateMachine<T> init(T context) {
        this.context = context;
        this.logger = DataLogger.getLogger();
        currentState = states.get(0);
        currentState.init();
        isDone = false;
        execNum++;
        return this;
    }

    /**
     * Stops the state machine from executing further when {@link #run()} is invoked
     */
    public void stop() {
        isDone = true;
        currentState = null;
    }

    /**
     * Causes the current state to be executed and the current state to be updated appropriately based on the
     * result (i.e. continue, next, jump). Creates a log entry detailing the current state, how much time
     * has been spent in that state, whether the state machine is done, which execution of the state machine
     * this is (e.g. 0 = 1st execution), etc.
     * @return the action specified (potentially a transition) after running the current state
     */
    public StateAction run() {
        StateAction action = null;
        JSONObject log = new JSONObject();
        DataLogger.putOpt(log, "type", "SM");
        DataLogger.putOpt(log, "name", name);
        DataLogger.putOpt(log, "execNum", execNum);

        if (currentState != null)
            DataLogger.putOpt(log, "timeInState", currentState.timeInState.milliseconds());

        if (isDone) {
            DataLogger.putOpt(log, "isDone", "true");
        } else {
            if (currentState == null)
                throw new RuntimeException("Attempt to run State Machine without a current state. Call init() first");

            currentState.resetNextAction();

            final int cur = states.indexOf(currentState);
            DataLogger.putOpt(log, "state", "" + cur);
            logContext(log);

            currentState.run(context);
            DataLogger.putOpt(log, "nextAction", currentState.getNextAction().getClass().getSimpleName());
            action = currentState.getNextAction();

            if (currentState.getNextAction() instanceof StateTransitionAction) {
                if (cur == states.size() - 1) {
                    if (looping) {
                        currentState = states.get(0);
                        currentState.init();
                    } else {
                        isDone = true;
                        currentState = null;
                    }
                } else {
                    currentState = states.get(cur + 1);
                    currentState.init();
                }
            } else if (currentState.getNextAction() instanceof StateJumpAction) {
                StateJumpAction jump = (StateJumpAction) currentState.getNextAction();
                currentState = jump.nextState;
                if (currentState == null) {
                    currentState = stateNameMap.get(jump.nextStateName);
                }
                currentState.init();
            }
        }

        try {
            logger.log(log);
        } catch (Throwable t) {
            RobotLog.i("" + name + ", " + logger + ", " + t.getMessage());
            throw t;
        }
        return action;
    }

    private void logContext(JSONObject log) {
        String contextAsString = "" + context;
        if (!contextAsString.equals(lastContext)) {
            DataLogger.putOpt(log, "context", contextAsString);
            lastContext = contextAsString;
        }
    }

    /**
     * @return true if the state machine has transitioned from the last state or if stop has been invoked
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Creates a new state which is added to this state machine and causes it to wait a period of time
     * before moving on
     * @param ms number of milliseonds to wait before transitioning to the next state
     * @return
     */
    public State<T> pause(final long ms) {
        return pause(null, ms);
    }

    public State<T> pause(String name, final long ms) {
        return add(State.create(name, (state, context) -> {
            if (state.timeInState.milliseconds() > ms)
                state.next();
        }));
    }

    /**
     * @return true if this state machine will automatically restart at the first state after completing the last one
     */
    public boolean isLooping() {
        return looping;
    }

    /**
     * Controls whether this state machine will automatically restart at the first state after completing the last one
     */
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public State getCurrentState() {
        return currentState;
    }
}
