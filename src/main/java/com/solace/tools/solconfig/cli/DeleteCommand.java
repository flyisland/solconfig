package com.solace.tools.solconfig.cli;

import picocli.CommandLine;
import com.solace.tools.solconfig.Commander;
import com.solace.tools.solconfig.model.SempSpec;

@CommandLine.Command(name = "delete", description = "Delete the specified objects")
public class DeleteCommand extends SubCommand {
    @CommandLine.Parameters(index = "0",
            description = "Type of the object to delete [${COMPLETION-CANDIDATES}]")
    private SempSpec.TOP_RES_ENUM resourceType;
    @CommandLine.Parameters(index = "1", description = "Object name to remove")
    private String objectName;

    @Override
    protected Integer execute() {
        Commander commander = parentCommand.commander;
        commander.delete(resourceType.getFullName(), new String[]{objectName});
        return 0;
    }
}
