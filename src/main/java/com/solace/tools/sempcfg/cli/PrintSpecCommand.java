package com.solace.tools.sempcfg.cli;

import com.solace.tools.sempcfg.Commander;
import com.solace.tools.sempcfg.model.SempSpec;
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
