package com.axibase.crawler.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

@Slf4j
@UtilityClass
public class CommandLineUtils {

    private static final char SEPARATOR = ',';

    public static final Option OPT_API_KEY = Option.builder().longOpt("api-key").hasArg(true).required(true).numberOfArgs(1).type(String.class).build();
    public static final Option OPT_IDS = Option.builder().longOpt("ids").hasArg(true).required(true).numberOfArgs(Option.UNLIMITED_VALUES)
            .valueSeparator(SEPARATOR).type(List.class).build();
    public static final Option OPT_DIR = Option.builder().longOpt("dir").hasArg(true).required(false).numberOfArgs(1).type(String.class).build();
    public static final Option OPT_WITH_SERIES_ID = Option.builder().longOpt("with-series-id").hasArg(false).required(false).build();
    public static final Option OPT_WITH_SERIES = Option.builder().longOpt("with-series").hasArg(false).required(false).build();

    private static final Options OPTIONS = new Options() {

        private static final long serialVersionUID = 1L;

        {
            addOption(OPT_API_KEY);
            addOption(OPT_IDS);
            addOption(OPT_DIR);
            addOption(OPT_WITH_SERIES_ID);
            addOption(OPT_WITH_SERIES);
        }
    };

    private static final CommandLine EMPTY_CMD = new CommandLine() {

        private static final long serialVersionUID = 1L;

    };

    private static CommandLine instance;

    public static CommandLine parseArguments(String[] cmdArgs) {
        if (instance == null) {
            instance = getCommandLine(cmdArgs);
        }
        return instance;
    }

    private static CommandLine getCommandLine(String[] cmdArgs) {
        if (cmdArgs.length == 0) {
            log.info("No user command line arguments");
            return EMPTY_CMD;
        }

        CommandLineParser parser = new DefaultParser();

        CommandLine commandLine;
        try {
            commandLine = parser.parse(OPTIONS, cmdArgs);
        } catch (ParseException exc) {
            log.error("Could not parse command line arguments", exc);
            return EMPTY_CMD;
        }

        String names = getOptionNames(commandLine);
        if (!names.isEmpty()) {
            log.info("Command line arguments: {}", names);
        }

        return commandLine;
    }

    private static String getOptionNames(CommandLine commandLine) {
        Option[] options = commandLine.getOptions();

        if (ArrayUtils.isEmpty(options)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Option option : options) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(option.getLongOpt());
        }
        return sb.toString();
    }

}
