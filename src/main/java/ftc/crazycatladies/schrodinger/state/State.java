package ftc.crazycatladies.schrodinger.state;

import com.qualcomm.robotcore.util.ElapsedTime;

public abstract class State<T> {
    protected ElapsedTime timeInState;
    private StateAction nextAction;

    protected abstract void run(T context);

    void init() {
        timeInState = new ElapsedTime();
    }

    void resetNextAction() {
        nextAction = new StateContinueAction();
    }

    public void next() {
        nextAction = new StateTransitionAction();
    }

    public void jump(State nextState) {
        nextAction = new StateJumpAction(nextState);
    }

    StateAction getNextAction() {
        return nextAction;
    }

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
