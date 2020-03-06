package ftc.crazycatladies.schrodinger.state;

public interface StateFunction<T> {
    void run(State state, T context);
}
