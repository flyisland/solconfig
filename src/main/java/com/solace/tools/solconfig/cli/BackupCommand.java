package com.solace.tools.solconfig.cli;

import com.solace.tools.solconfig.Commander;
import picocli.CommandLine;
import com.solace.tools.solconfig.model.SempSpec;

import java.util.*;

@CommandLine.Command(name = "backup", description = "Export the whole configuration of objects into a single JSON",
        showDefaultValues = true)
public class BackupCommand extends SubCommand {
    @CommandLine.Parameters(index = "0",
            description = "Type of the exported object [${COMPLETION-CANDIDATES}]")
    private SempSpec.RES_ABBR resourceType;
    @CommandLine.Parameters(index = "1..*", arity = "1..*", description = "One or more object names, , \"*\" means all")
    private String[] objectNames;
    @CommandLine.Option(names = {"-O", "---opaque-password"},
            description = "The opaquePassword for receiving and updating opaque properties like the password of Client Usernames")
    private String opaquePassword;
    @CommandLine.Option(names = {"-D", "--keep-default"}, description = "Whether to Keep attributes with a default value")
    private boolean isKeepDefault = false;


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
        commander.backup(resourceType.getFullName(), objectNames, isKeepDefault);
        return 0;
    }
}
