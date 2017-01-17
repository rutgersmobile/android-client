package edu.rutgers.css.Rutgers.utils;

/**
 * Created by rz187 on 1/12/17.
 */

public class FuncWrapper {
    public interface Function0 {
        public void run();
    }

    public interface Function1<T,R> {
        public R run(T t);
    }

    public interface Function2<T1, T2, R> {
        public R run(T1 t1, T2 t2);
    }
}
