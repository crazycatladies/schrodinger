package ftc.crazycatladies.schrodinger.state;

/**
 * Result for executing a {@link State} for which the {@link StateMachine} should transition to the specified
 * {@link State}
 */
public class StateJumpAction extends StateAction {
    State nextState;
    String nextStateName;

    StateJumpAction(State state) {
        nextState = state;
    }

    public StateJumpAction(String nextState) {
        nextStateName = nextState;
    }
}
