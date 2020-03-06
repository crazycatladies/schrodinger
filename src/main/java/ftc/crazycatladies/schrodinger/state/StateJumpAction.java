package ftc.crazycatladies.schrodinger.state;

public class StateJumpAction extends StateAction {
    State nextState;

    StateJumpAction(State state) {
        nextState = state;
    }
}
