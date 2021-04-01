package com.solace.tools.solconfig.cli;

import com.solace.tools.solconfig.Commander;
import com.solace.tools.solconfig.Utils;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(name = "create", description = "Create objects from the configuration file")
public class CreateCommand extends SubCommand {
    @CommandLine.Parameters(index = "0", description = "Configuration file")
    private Path confPath;

    @CommandLine.Spec  CommandLine.Model.CommandSpec spec;

    @Override
    protected Integer execute() {
        if (!Files.isReadable(confPath)) {
            Utils.errPrintlnAndExit("Path %s doesn't exist or is un-readable!", confPath.toAbsolutePath());
        }

        Commander commander = parentCommand.commander;
        commander.create(confPath);
        return 0;
    }
}
