package com.solace.tools.solconfig.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "subcommand", description = "abstract subcommand")
public abstract class SubCommand implements Callable<Integer> {
    @CommandLine.ParentCommand
    protected SempCfgCommand parentCommand;

    @Override
    public Integer call(){
        parentCommand.init();
        return execute();
    }

    protected abstract Integer execute();
}
