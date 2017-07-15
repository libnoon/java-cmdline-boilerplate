package fr.fbauzac.boil;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import java.util.Arrays;
import joptsimple.OptionSpec;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import joptsimple.NonOptionArgumentSpec;

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

        System.out.println("The name is " + optionSet.valueOf(name));

        if (optionSet.has(integer)) {
            System.out.println("The number is " + optionSet.valueOf(integer));
        }
        
        System.out.println("The arguments were: " + String.join(", ", optionSet.valuesOf(nonOptions)));
    }
}
