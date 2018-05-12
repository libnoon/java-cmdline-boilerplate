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

public final class Main {
    private void usage() {
        try {
            try (InputStream is = getClass().getResourceAsStream("/help.txt")) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
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

    public static void main(String[] args) {
        new Main().run(args);
    }

    private void run(String[] args) {
        OptionParser parser = new OptionParser("hn:i:");
        parser.acceptsAll(Arrays.asList("help", "h"));
        OptionSpec<String> name = parser.acceptsAll(Arrays.asList("name", "n")).withRequiredArg().defaultsTo("Isaac Newton");
        OptionSpec<Integer> integer = parser.acceptsAll(Arrays.asList("integer", "i")).withRequiredArg().ofType(Integer.class);
        NonOptionArgumentSpec<String> nonOptions = parser.nonOptions();

        // Note that it also automatically works with enum classes
        // (anything that has valueOf(), in fact).
        parser.accepts("integer").withRequiredArg().ofType(Integer.class).defaultsTo(10);
        OptionSet optionSet = parser.parse(args);

        if (optionSet.has("help")) {
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

        System.out.println("The name is " + optionSet.valueOf(name));

        if (optionSet.has(integer)) {
            System.out.println("The number is " + optionSet.valueOf(integer));
        }

        System.out.println("The arguments were: " + String.join(", ", optionSet.valuesOf(nonOptions)));

        {
            var props = System.getProperties();
            var keys = new TreeSet<String>(props.stringPropertyNames());
            for (var key: keys) {
                System.out.println(String.format("%s=%s", key, props.getProperty(key)));
            }
        }
    }
}
