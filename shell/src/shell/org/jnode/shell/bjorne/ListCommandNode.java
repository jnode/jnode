package org.jnode.shell.bjorne;

import java.io.Closeable;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;

import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandThread;
import org.jnode.shell.NullInputStream;
import org.jnode.shell.NullOutputStream;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellFailureException;
import org.jnode.shell.ThreadExitListener;
import org.jnode.shell.bjorne.BjorneContext.StreamHolder;


public class ListCommandNode extends CommandNode {
    private static class PipelineStage {
        private CommandLine command;
        private BjorneContext context;
        private CommandThread thread;
        private StreamHolder[] holders;
    }
    
    private final CommandNode[] commands;

    public ListCommandNode(int nodeType, List<? extends CommandNode> commands) {
        super(nodeType);
        this.commands = commands.toArray(new CommandNode[commands.size()]);
    }

    public CommandNode[] getCommands() {
        return commands;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ListCommand{").append(super.toString());
        sb.append(",commands=");
        appendArray(sb, commands);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) throws ShellException {
        int rc = 0;
        if (getNodeType() == BjorneInterpreter.CMD_SUBSHELL) {
            // This simulates creating a 'subshell'.
            context = new BjorneContext(context);
        }
        int listFlags = getFlags();
        if ((listFlags & BjorneInterpreter.FLAG_PIPE) != 0) {
            PipelineStage[] stages = assemblePipeline(context);
            boolean done = false;
            try {
                rc = runPipeline(stages);
                done = true;
            }
            finally {
                if (!done) {
                    // If we are propagating an exception, all streams that
                    // were opened by 'assemblePipeline' must be closed.
                    for (PipelineStage stage : stages) {
                        for (StreamHolder holder : stage.holders) {
                            holder.close();
                        }
                    }
                }
            }
        } else {
            for (CommandNode command : commands) {
                int commandFlags = command.getFlags();
                if ((commandFlags & BjorneInterpreter.FLAG_AND_IF) != 0) {
                    if (context.getLastReturnCode() != 0) {
                        break;
                    }
                }
                if ((commandFlags & BjorneInterpreter.FLAG_OR_IF) != 0) {
                    if (context.getLastReturnCode() == 0) {
                        break;
                    }
                }
                rc = command.execute(context);
            }
        }
        if ((listFlags & BjorneInterpreter.FLAG_BANG) != 0) {
            rc = (rc == 0) ? -1 : 0;
        }
        return rc;
    }
    
    private class ThreadCallback implements ThreadExitListener {
        private PipelineStage[] stages;
        private int count;
        
        ThreadCallback(PipelineStage[] stages) {
            this.stages = stages;
            this.count = stages.length;
        }
        
        public void notifyThreadExited(CommandThread thread) {
            synchronized (stages) {
                for (PipelineStage stage : stages) {
                    if (stage.thread == thread) {
                        for (StreamHolder holder : stage.holders) {
                            holder.close();
                        }
                        break;
                    }
                }
                count--;
                if (count <= 0) {
                    stages.notify();
                }
            }
        }
    }
    
    private int runPipeline(final PipelineStage[] stages) 
    throws ShellException {
        for (PipelineStage stage : stages) {
            Closeable[] streams = new Closeable[stage.holders.length];
            for (int i = 0; i < streams.length; i++) {
                streams[i] = stage.holders[i].stream;
            }
            stage.thread = stage.context.fork(stage.command, streams);
        }
        synchronized (stages) {
            ThreadCallback callback = new ThreadCallback(stages);
            for (PipelineStage stage : stages) {
                stage.thread.start(callback);
            }
            while (callback.count > 0) {
                try {
                    stages.wait();
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return stages[stages.length - 1].thread.getReturnCode();
        }
    }

    private PipelineStage[] assemblePipeline(BjorneContext context) 
    throws ShellException {
        int len = commands.length;
        final StreamHolder pipeInMarker = new StreamHolder(BjorneContext.PIPE_IN, false);
        final StreamHolder pipeOutMarker = new StreamHolder(BjorneContext.PIPE_OUT, false);
        PipelineStage[] stages = new PipelineStage[len];
        for (int i = 0; i < len; i++) {
            SimpleCommandNode commandNode = (SimpleCommandNode) commands[i];
            PipelineStage stage = stages[i] = new PipelineStage();
            stage.context = new BjorneContext(context);
            stage.context.performAssignments(commandNode.getAssignments());
            stage.command = stage.context.expandAndSplit(commandNode.getWords());
            stage.holders = context.getCopyOfHolders();
            if (i < len - 1) {
                stage.holders[1] = pipeOutMarker;
            }
            if (i > 0) {
                stage.holders[0] = pipeInMarker;
            }
            stage.context.evaluateRedirections(commandNode.getRedirects(), stage.holders);
        }
        for (int i = 0; i < len - 1; i++) {
            StreamHolder newIn = null, newOut = null;
            PipelineStage thisStage = stages[i];
            PipelineStage nextStage = stages[i + 1];
            if (thisStage.holders[1] == pipeOutMarker) {
                if (nextStage.holders[0] == pipeInMarker) {
                    PipedOutputStream pipeOut = new PipedOutputStream();
                    PipedInputStream pipeIn = new PipedInputStream();
                    try {
                        pipeIn.connect(pipeOut);
                    }
                    catch (IOException ex) {
                        throw new ShellFailureException("plumbing failure", ex);
                    }
                    newIn = new StreamHolder(pipeIn, true);
                    newOut = new StreamHolder(pipeOut, true);
                }
                else {
                    newOut = new StreamHolder(new NullOutputStream(), true);
                }
            }
            else {
                if (nextStage.holders[0] == pipeInMarker) {
                    newIn = new StreamHolder(new NullInputStream(), true);
                }
            }
            if (newOut != null) {
                for (int j = 0; j < thisStage.holders.length; j++) {
                    if (thisStage.holders[j] == pipeOutMarker) {
                        thisStage.holders[j] = newOut;
                    }
                }
            }
            if (newIn != null) {
                for (int j = 0; j < nextStage.holders.length; j++) {
                    if (nextStage.holders[j] == pipeInMarker) {
                        nextStage.holders[j] = newIn;
                    }
                }
            }
        }
        return stages;
    }
}
