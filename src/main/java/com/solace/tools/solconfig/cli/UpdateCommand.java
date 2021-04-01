package com.solace.tools.solconfig.cli;

import com.solace.tools.solconfig.Commander;
import picocli.CommandLine;
import com.solace.tools.solconfig.Utils;

import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(name = "update", description = "Update the existing objects to make them the same as the configuration file\n" +
        "\n" +
        "Be careful, it will DELETE existing objects like Queues or Client Usernames, etc if they are absent in the configuration file.\n" +
        "\n" +
        "This \"update\" command is a good complement to \"create\" command, especially for the \"default\" VPN or the VPN of the Solace Cloud Service instance, since you can only update them.\n")
public class UpdateCommand extends SubCommand {
    @CommandLine.Parameters(index = "0", description = "Configuration file")
    private Path confPath;

    @CommandLine.Spec  CommandLine.Model.CommandSpec spec;

    @Override
    protected Integer execute() {
        if (!Files.isReadable(confPath)) {
            Utils.errPrintlnAndExit("Path %s doesn't exist or is un-readable!", confPath.toAbsolutePath());
        }

        Commander commander = parentCommand.commander;
        commander.update(confPath);
        return 0;
    }
}
