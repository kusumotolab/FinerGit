package jp.ac.titech.c.se.stein.core;

public interface Configurable {
    void addOptions(final Config conf);

    void configure(final Config conf);
}
