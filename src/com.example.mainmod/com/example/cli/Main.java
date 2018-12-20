package com.example.cli;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import java.util.Arrays;
import joptsimple.OptionSpec;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import joptsimple.NonOptionArgumentSpec;
import java.util.List;
import java.util.TreeSet;
import java.io.Console;

/*
 * Cheat sheet for UNIX operations: see the javadoc:
 *
 * java.nio.file
 * java.nio.file.Files
 * java.nio.file.Paths
 * ...
 */

/**
 * Command line interface.
 */
public final class Main {
    /**
     * Print the command-line help.
     */
    private void usage() {
        try {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/help.txt")))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }
        catch (IOException ioException) {
            System.err.println("cannot retrieve help");
        }
    }

    /**
     * Commandline entry point.
     *
     * This function merely creates a new Main object and calls its
     * run() method.
     *
     * @param args the commandline arguments.
     */
    public static void main(String[] args) {
        new Main().run(args);
    }

    /**
     * The toplevel processing of the commandline.
     *
     * @param args the commandline arguments.
     */
    public void run(String[] args) {
        OptionParser parser = new OptionParser();
        OptionSpec<Void> help = parser.acceptsAll(List.of("help", "h"));
        OptionSpec<String> name = parser.acceptsAll(List.of("name", "n")).withRequiredArg();
        OptionSpec<Integer> integer = parser.acceptsAll(List.of("integer", "i")).withRequiredArg().ofType(Integer.class).defaultsTo(10);
        NonOptionArgumentSpec<String> nonOptions = parser.nonOptions();

        // Note that it also automatically works with enum classes
        // (anything that has valueOf(), in fact).
        OptionSet optionSet = parser.parse(args);

        if (optionSet.has(help)) {
            usage();
            return;
        }

        List<String> params = optionSet.valuesOf(nonOptions);
        if (params.size() < 1) {
            System.err.println("not enough parameters");
            System.exit(1);
        }
        else if (params.size() > 2) {
            System.err.println("too many arguments");
            System.exit(1);
        }

        if (optionSet.has(name)) {
            System.out.println("There is a name and its value is " + optionSet.valueOf(name));
        }

        System.out.println("The number is " + optionSet.valueOf(integer));

        System.out.println("The arguments were: " + String.join(", ", optionSet.valuesOf(nonOptions)));

        {
            var props = System.getProperties();
            var keys = new TreeSet<String>(props.stringPropertyNames());
            keys.stream().filter(s -> s.startsWith("java.")).forEach(key -> {
                    System.out.println(String.format("%s=%s", key, props.getProperty(key)));
                });
        }

        Console console = System.console();
        String username = console.readLine("What is your %s: ", "username");
        String password = new String(console.readPassword("Enter your %s:", "password"));
        System.out.println(String.format("username: %s; pass len = %d", username, password.length()));
    }
}
