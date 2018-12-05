package jp.ac.titech.c.se.stein.core;

public interface Config {
    void addOption(final String opt, final String longOpt, final boolean hasArg, final String description);

    boolean hasOption(final String opt);

    String getOptionValue(final String opt);
}
