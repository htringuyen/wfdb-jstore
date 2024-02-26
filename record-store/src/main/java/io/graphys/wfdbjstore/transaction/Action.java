package io.graphys.wfdbjstore.transaction;

@FunctionalInterface
public interface Action<R> {
    public R perform();
}
