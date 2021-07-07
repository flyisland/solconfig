package com.solace.tools.solconfig.cli;

import com.solace.tools.solconfig.Commander;
import picocli.CommandLine;

@CommandLine.Command(name = "test", description = "Integration Test", hidden = true)
public class TestCommand extends SubCommand {

    @Override
    protected Integer execute() {
        Commander commander = parentCommand.commander;
        commander.integrationTest();
        return 0;
    }
}
