package semp.cfg.cli;

import picocli.CommandLine;
import semp.cfg.Commander;

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
            throw new CommandLine.ParameterException(spec.commandLine(), String.format(
                    "Path %s doesn't exist or is un-readable!",
                    confPath.toAbsolutePath()
            ));

        }

        Commander commander = parentCommand.commander;
        commander.create(confPath);
        return 0;
    }
}
