package semp.cfg.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import semp.cfg.Commander;
import semp.cfg.SempClient;

import java.util.concurrent.Callable;

@Command(name = "sempcfg", mixinStandardHelpOptions = true, version = {"sempcfg 1.0.0",
        "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
        "OS: ${os.name} ${os.version} ${os.arch}"},
        description = "Backing Up and Restoring Solace PubSub+ Broker Configuration with SEMPv2 protocol. " +
                "Use the 'backup' command to export the configuration of objects on a PS+  Broker into a single JSON, " +
                "then use the 'create' or 'update' command to restore the configuration.",
        subcommands = {
            BackupCommand.class,
            DeleteCommand.class,
            CreateCommand.class,
            UpdateCommand.class,
            CommandLine.HelpCommand.class
        },
        showDefaultValues = true)
public class SempCfgCommand implements Callable<Integer> {

    @Option(names = {"-H", "--host"}, description = "URL to access the management endpoint of the broker")
    private String adminHost = "http://localhost:8080";

    @Option(names = {"-u", "--admin-user"}, description = "The username of the management user")
    private String adminUser = "admin";

    @Option(names = {"-p", "--admin-password"}, description = "The password of the management user")
    private String adminPwd = "admin";

    @Option(names = {"-O", "---opaque-password"},
            description = "The opaquePassword for receiving and updating opaque properties like the password of Client Usernames")
    private String opaquePassword;

    @Option(names = "--curl-only", description = "Print curl commands only, no effect on 'backup' command")
    private boolean curlOnly = false;

    @CommandLine.Spec  CommandLine.Model.CommandSpec spec;
    @Override
    public Integer call() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand!");
    }

    protected Commander commander;
    protected void init(){
        commander = Commander.ofSempClient(new SempClient(adminHost, adminUser, adminPwd));
        commander.setCurlOnly(curlOnly);
        commander.getSempClient().setOpaquePassword(opaquePassword);
    }

    @Override
    public String toString() {
        return "SempCfgCommand{" +
                "adminHost='" + adminHost + '\'' +
                ", adminUser='" + adminUser + '\'' +
                ", adminPwd='" + adminPwd + '\'' +
                ", curlOnly=" + curlOnly +
                '}';
    }
}
