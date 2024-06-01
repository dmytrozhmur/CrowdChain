package ua.nure.crowdchainnode.util.tuples;

public class Pair<T1, T2> {
    private T1 value0;
    private T2 value1;

    public Pair(T1 value0, T2 value1) {
        this.value0 = value0;
        this.value1 = value1;
    }

    public T1 getValue0() {
        return value0;
    }

    public T2 getValue1() {
        return value1;
    }

    public static <T1, T2> Pair<T1, T2> with(T1 value0, T2 value1) {
        return new Pair<>(value0, value1);
    }
}
