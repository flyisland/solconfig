package semp.cfg.cli;

import picocli.CommandLine;
import semp.cfg.Commander;
import semp.cfg.model.SempSpec;

import java.util.*;

@CommandLine.Command(name = "backup", description = "Export the whole configuration of objects into a single JSON")
public class BackupCommand extends SubCommand {
    @CommandLine.Parameters(index = "0", completionCandidates = ResourceTypeCandidates.class,
            description = "Type of the exported object [${COMPLETION-CANDIDATES}]")
    private String resourceType;
    @CommandLine.Parameters(index = "1..*", arity = "1..*", description = "One or more object names, , '*' means all")
    private String[] objectNames;
    @CommandLine.Option(names = {"-O", "---opaque-password"},
            description = "The opaquePassword for receiving and updating opaque properties like the password of Client Usernames")
    private String opaquePassword;

    @Override
    public String toString() {
        return "BackupCommand{" +
                "resourceType=" + resourceType +
                ", objectNames=" + Arrays.toString(objectNames) +
                '}';
    }

    @Override
    protected Integer execute() {
        Commander commander = parentCommand.commander;
        commander.getSempClient().setOpaquePassword(opaquePassword);
        commander.backup(SempSpec.TOP_RESOURCES.get(resourceType), objectNames);
        return 0;
    }
}
