package ftc.crazycatladies.schrodinger.state;

/**
 * Function interface used to allow {@link State} classes to be defined by lambda expressions
 * @param <T> context type - must match the context type for the {@link StateMachine} to which this state belongs
 */
public interface StateFunction<T> {
    void run(State state, T context);
}
