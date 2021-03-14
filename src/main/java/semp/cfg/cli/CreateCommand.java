package semp.cfg.cli;

import picocli.CommandLine;
import semp.cfg.Commander;

import java.io.File;

@CommandLine.Command(name = "create", description = "Create objects from the configuration file")
public class CreateCommand extends SubCommand {
    @CommandLine.Parameters(index = "0", description = "Configuration file")
    private File confFile;

    @CommandLine.Spec  CommandLine.Model.CommandSpec spec;

    @Override
    protected Integer execute() {
        if (!confFile.exists()) {
            throw new CommandLine.ParameterException(spec.commandLine(), String.format(
                    "File %s is not exists!",
                    confFile.getAbsolutePath()
            ));

        }

        Commander commander = parentCommand.commander;
        commander.create(confFile);
        return 0;
    }
}
