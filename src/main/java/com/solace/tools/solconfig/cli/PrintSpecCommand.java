package com.solace.tools.solconfig.cli;

import com.solace.tools.solconfig.Commander;
import picocli.CommandLine;

@CommandLine.Command(name = "spec", description = "Print the analyzed SEMPv2 specification")
public class PrintSpecCommand extends SubCommand {

    @Override
    protected Integer execute() {
        Commander commander = parentCommand.commander;
        commander.printSpec();
        return 0;
    }
}
