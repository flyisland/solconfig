package com.solace.tools.solconfig.cli;

import picocli.CommandLine;
import com.solace.tools.solconfig.Commander;
import com.solace.tools.solconfig.model.SempSpec;

@CommandLine.Command(name = "delete", description = "Delete the specified objects")
public class DeleteCommand extends SubCommand {
    @CommandLine.Parameters(index = "0", completionCandidates = ResourceTypeCandidates.class,
            description = "Type of the object to delete [${COMPLETION-CANDIDATES}]")
    private String resourceType;
    @CommandLine.Parameters(index = "1", description = "Object name to remove")
    private String objectName;

    @Override
    protected Integer execute() {
        Commander commander = parentCommand.commander;
        commander.delete(SempSpec.TOP_RESOURCES.get(resourceType), new String[]{objectName});
        return 0;
    }
}
