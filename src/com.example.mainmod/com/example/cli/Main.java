package com.example.cli;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import java.util.Arrays;
import joptsimple.OptionSpec;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import joptsimple.NonOptionArgumentSpec;
import java.util.List;
import java.util.TreeSet;
import java.io.Console;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.util.Optional;
import java.time.Instant;
import java.time.Duration;
import java.lang.ProcessBuilder;
import java.lang.Process;
import java.util.Map;

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
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    /**
     * The toplevel processing of the commandline.
     *
     * @param args the commandline arguments.
     */
    public void run(String[] args) throws Exception {
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

        for (String arg: optionSet.valuesOf(nonOptions)) {
            Path path = Paths.get(arg);
            System.out.format("%s is on filesystem %s, the filename is %s, root is %s, it is %s\n",
                              path, path.getFileSystem(), path.getFileName(), path.getRoot(), path.isAbsolute()? "absolute": "relative");
            Path absolute = path.toAbsolutePath();
            System.out.format("Absolute: %s, File: %s, realpath: %s, realpath no follow lints: %s\n", absolute, path.toFile(), path.toRealPath(), path.toRealPath(LinkOption.NOFOLLOW_LINKS));
            System.out.format("Relatively, /usr/bin is %s\n", absolute.relativize(Paths.get("/usr/bin")));
        }

        {
            var props = System.getProperties();
            var keys = new TreeSet<String>(props.stringPropertyNames());
            keys.stream().filter(s -> s.startsWith("java.")).forEach(key -> {
                    System.out.println(String.format("%s=%s", key, props.getProperty(key)));
                });
        }

        Console console = System.console();
        console.format("This line is printed on the console\n"); // See also console.reader() and .writer()

        String firstArg = optionSet.valuesOf(nonOptions).get(0);

        // Exercize a few useful string methods
        if (firstArg.isBlank()) {
            System.out.println("firstArg is blank");
        }
        if (firstArg.startsWith("abc")) {
            System.out.println("firstArg starts with abc");
        }
        if (firstArg.isEmpty()) {
            System.out.println("firstArg is empty");
        }

        // Exercize ProcessHandles
        ProcessHandle.allProcesses().forEach(process -> {
                ProcessHandle.Info info = process.info();
                System.out.format("%5d  %-8s  %-24s  %-10s  %-10s  %s\n", process.pid(), info.user().orElse("-"), info.startInstant().map(Instant::toString).orElse("-"), info.totalCpuDuration().map(Duration::toString).orElse("-"), info.command().orElse("-"), info.commandLine().orElse("-"));
            });

        //ProcessHandle process = ProcessHandle.of(pid);
        ProcessHandle processHandle = ProcessHandle.current();
        System.out.format("pid: %d; parent: %s\n", processHandle.pid(), processHandle.parent().map(ProcessHandle::toString).orElse("none"));
        //processHandle.destroy() // TERM
        //processHandle.destroyForcibly() // KILL

        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", "gzip > /dev/null")
            .directory(new File("/"))     // Working directory
            //.inheritIO()        // subprocess has same stdin/out/err as the Java process
            .redirectErrorStream(true) // Merge stderr to stdout
            //.redirectError(ProcessBuilder.Redirect.INHERIT) // has same as the Java process
            .redirectOutput(ProcessBuilder.Redirect.DISCARD) // use /dev/null, apparently?
            //.redirectOutput(ProcessBuilder.Redirect.to(File)) // to(File), appendTo(File), from(File)
            .redirectInput(ProcessBuilder.Redirect.PIPE)     // Pipe from or to the Java process
            ;
        Map<String, String> gzipEnvironment = processBuilder.environment();
        gzipEnvironment.put("TMPDIR", "/tmp");
        gzipEnvironment.remove("TOTO");
        Process process = processBuilder.start(); // Also see: ProcessBuilder.startPipeline(List<ProcessBuilder> ...)
        System.out.format("gzip subprocess pid: %d\n", process.pid());
        try (OutputStream streamToGzip = process.getOutputStream()) {
            streamToGzip.write(42);
        }
        process.waitFor();

        String username = console.readLine("What is your %s: ", "username");
        String password = new String(console.readPassword("Enter your %s: ", "password"));
        System.out.println(String.format("username: %s; pass len = %d", username, password.length()));
    }
}
