package ftc.crazycatladies.schrodinger.state;

import org.json.JSONObject;

import ftc.crazycatladies.schrodinger.log.DataLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * program using state machines, sequential actions that can be performed simultaneously
 * @param <T>
 */


/**
 * private instance variables being declared
 */
public class StateMachine<T> {
    private String name;
    private List<State> states = new ArrayList<State>();
    private State currentState;
    private boolean isDone;
    private T context;
    private DataLogger logger;
    private String lastContext;
    private int execNum = 0;
    private boolean looping = false;

    /**
     * calling String "name" to run in state machine
     * @param name
     */
    public StateMachine(String name) {
        this.name = name;
    }

    /**
     * instantiating states and indicating that only one state can be added at one time
     * @param state
     * @return
     */

     public State<T> add(State<T> state) {
        if (states.indexOf(state) > -1)
            throw new RuntimeException("States cannot be added more than once");

        states.add(state);
        return state;
    }

    public StateMachine<T> addAll(StateMachine sm) {
        states.addAll(sm.states);
        return this;
    }

    public State<T> repeat(StateFunction<T> function) {
        return add(State.create(function));
    }
    public State<T> once(StateFunction<T> function) {
        return add(State.createOnce(function));
    }

    public StateMachine<T> init() {
        return init(null);
    }

    /**
     * this method sets up the state  machine to run.  Must be called before using the
     * state machines or else it will not work
     * @param context
     * @return
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

    public void stop() {
        isDone = true;
        currentState = null;
    }

    /**
     * running state machines, and using if statements to get information on what to run
     * @return
     */

    public StateAction run() {
        StateAction action = null;
        JSONObject log = new JSONObject();
        DataLogger.putOpt(log, "type", "SM");
        DataLogger.putOpt(log, "name", name);
        DataLogger.putOpt(log, "execNum", execNum);

        /**
         * state machines
         */

        if (currentState != null)
            DataLogger.putOpt(log, "timeInState", currentState.timeInState.milliseconds());

        if (isDone) {
            DataLogger.putOpt(log, "isDone", "true");
        } else {
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
                currentState = ((StateJumpAction) currentState.getNextAction()).nextState;
                currentState.init();
            }
        }

        logger.log(log);
        return action;
    }

    private void logContext(JSONObject log) {
        String contextAsString = "" + context;
        if (!contextAsString.equals(lastContext)) {
            DataLogger.putOpt(log, "context", contextAsString);
            lastContext = contextAsString;
        }
    }

    public boolean isDone() {
        return isDone;
    }

    public StateMachine<T> pause(final long ms) {
        add(State.create((state, context) -> {
            if (state.timeInState.milliseconds() > ms)
                state.next();
        }));
        return this;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }
}
