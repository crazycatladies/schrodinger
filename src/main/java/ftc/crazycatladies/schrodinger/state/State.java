package ftc.crazycatladies.schrodinger.state;

import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Defines a potential status for a state machine. Subclasses define actions which should be taken in this status by
 * overriding the {@link #run(Object)} method.
 * @param <T> context type - must match the context type for the {@link StateMachine} to which this state belongs
 */
public abstract class State<T> {
    /**
     * the amount of time that this state machine has been in this state
     */
    protected ElapsedTime timeInState;
    private StateAction nextAction;

    /**
     * Defines the actions that should run while a {@link StateMachine} is in this state. State will run infinitely
     * unless {@link #next()} or {@link #jump(State)} is called
     * @param context current context information for the state machine. Can be used to provide a state with information
     *                or to allow the state to record information for a following state
     */
    protected abstract void run(T context);

    void init() {
        timeInState = new ElapsedTime();
    }

    void resetNextAction() {
        nextAction = new StateContinueAction();
    }

    /**
     * Causes the next state in a {@link StateMachine} to be run during its next execution
     */
    public void next() {
        nextAction = new StateTransitionAction();
    }

    /**
     * Causes the {@link StateMachine} to leave this state and move on to the specified one next
     * @param nextState the state which should be run during the next execution of the {@link StateMachine}
     */
    public void jump(State nextState) {
        nextAction = new StateJumpAction(nextState);
    }

    StateAction getNextAction() {
        return nextAction;
    }

    /**
     * Returns the amount of time that this state machine has been in this state
     */
    public ElapsedTime getTimeInState() {
        return timeInState;
    }

    static <C> State create(final StateFunction<C> function) {
        return new State<C>() {
            @Override
            protected void run(C context) {
                function.run(this, context);
            }
        };
    }

    static <C> State createOnce(final StateFunction<C> function) {
        return new State<C>() {
            @Override
            protected void run(C context) {
                function.run(this, context);
                next();
            }
        };
    }
}
