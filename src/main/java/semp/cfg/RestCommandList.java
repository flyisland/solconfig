package semp.cfg;

import com.fasterxml.jackson.core.JsonProcessingException;
import semp.cfg.model.HTTPMethod;
import semp.cfg.model.SEMPError;
import semp.cfg.model.SempResponse;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class RestCommandList {
    class Command {
        private HTTPMethod method;
        private String resourcePath;
        private String payload;

        Command(HTTPMethod method, String resourcePath, String payload) {
            this.method = method;
            this.resourcePath = resourcePath;
            this.payload = payload;
        }

        @Override
        public String toString() {
            return String.format("%s %s%s%n", method.name(), resourcePath,
                    Objects.isNull(payload) || payload.isEmpty() ? "" : "\n" + payload);
        }
    }

    private List<Command> commands = new LinkedList<>();

    public void append(HTTPMethod method, String resourcePath, String payload) {
        commands.add(new Command(method, resourcePath, payload));
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        commands.forEach(sb::append);
        return sb.toString();
    }

    public void execute(SempClient sempClient) {
        execute(sempClient, this.commands);
    }

    private void execute(SempClient sempClient, List<Command> commandList) {
        List<Command> retryCommands = new LinkedList<>();
        for (Command cmd : commandList) {
            Utils.err("%s %s ", cmd.method.name(), cmd.resourcePath);
            var meta = sempClient.cudWithResourcePath(cmd.method.name(), cmd.resourcePath, cmd.payload);
            if (meta.getResponseCode() == 200) {
                Utils.err("OK%n");
            } else if (cmd.method == HTTPMethod.DELETE &&
                    meta.getError().getCode() == SEMPError.NOT_ALLOWED.getValue()) {
                Utils.err("%s, retry later%n", SEMPError.NOT_ALLOWED);
                retryCommands.add(cmd);
            } else if (cmd.method == HTTPMethod.POST &&
                    meta.getError().getCode() == SEMPError.ALREADY_EXISTS.getValue()) {
                Utils.err("%s%n", SEMPError.ALREADY_EXISTS);
            } else {
                Utils.err("%n%s%n", meta.toString());
                System.exit(1);
            }
        }
        if (! retryCommands.isEmpty()) execute(sempClient, retryCommands);
    }
}