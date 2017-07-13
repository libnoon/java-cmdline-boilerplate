package fr.fbauzac.boil;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import java.util.Arrays;
import joptsimple.OptionSpec;

public final class Main {
    private static void usage() {
        System.out.println("run [OPTION...] TEXT...");
        System.out.println("Run on the given arguments.");
        System.out.println(" Options:");
        System.out.println("   -n, --name=NAME       Use this NAME");
        System.out.println("   -i, --integer=NUMBER  Use this NUMBER");
        System.out.println("   -h, --help            Show this help");
    }

    public static void main(String[] args) {
        OptionParser parser = new OptionParser("hn:i:");
        parser.acceptsAll(Arrays.asList("help", "h"));
        OptionSpec<String> name = parser.acceptsAll(Arrays.asList("name", "n")).withRequiredArg().defaultsTo("Isaac Newton");
        OptionSpec<Integer> integer = parser.acceptsAll(Arrays.asList("integer", "i")).withRequiredArg().ofType(Integer.class);
        

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
        
        //System.out.println("The arguments were: " + ", ".join(parser.nonOptionArguments()));
    }
}
