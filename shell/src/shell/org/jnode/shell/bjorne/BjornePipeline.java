/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 package org.jnode.shell.bjorne;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jnode.shell.Command;
import org.jnode.shell.CommandShell;
import org.jnode.shell.CommandThread;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellFailureException;
import org.jnode.shell.ThreadExitListener;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.io.CommandIOHolder;
import org.jnode.shell.io.CommandIOMarker;
import org.jnode.shell.io.CommandInput;
import org.jnode.shell.io.CommandOutput;
import org.jnode.shell.io.Pipeline;

/**
 * This class deals with construction and running of multi-command
 * pipelines for the Bjorne shell.  
 * 
 * @author crawley@jnode.org
 */
class BjornePipeline {
    private static class PipelineStage {
        private String stageName;
        private CommandNode command;
        private BjorneContext context;
        private CommandThread thread;
        private CommandIOHolder[] holders;
        private BjornePipeline nestedPipeline;
    }
    
    private int stageCount = 0;
    private int nextStage = 0;
    private final PipelineStage[] stages;
    private final BjornePipeline parent;
    private final Map<String, Pipeline> pipes;
    private int activeStageCount;
    

    BjornePipeline(int len) {
        this.stages = new PipelineStage[len];
        this.parent = null;
        this.pipes = new HashMap<String, Pipeline>();
    }

    BjornePipeline(BjornePipeline parent, int len) {
        this.stages = new PipelineStage[len];
        this.parent = parent;
        this.pipes = null;
    }

    void closeStreams() {
        for (PipelineStage stage : stages) {
            if (stage != null) {
                for (CommandIOHolder holder : stage.holders) {
                    holder.close();
                }
                if (stage.nestedPipeline != null) {
                    stage.nestedPipeline.closeStreams();
                }
            }
        }
    }

    void wire() throws ShellException {
        evaluateRedirections();
        createPipes();
        activatePipes();
    }

    private void evaluateRedirections() throws ShellException {
        for (PipelineStage stage : stages) {
            RedirectionNode[] redirects = stage.command.getRedirects();
            stage.context.evaluateRedirections(redirects, stage.holders);
        }
    }

    private void createPipes() throws ShellFailureException {
        try {
            for (PipelineStage stage : stages) {
                for (CommandIOHolder holder : stage.holders) {
                    CommandIO io = holder.getIO();
                    if (io instanceof CommandIOMarker) {
                        CommandIOMarker marker = (CommandIOMarker) io;
                        String name = marker.getName();

                        if (name.startsWith("PIPE-")) {
                            holder.setIO(
                                    (marker.getDirection() == CommandIO.DIRECTION_OUT ?
                                            getOutPipeIO(name) : getInPipeIO(name)),
                                            true);
                        }
                    }
                } 
                if (stage.nestedPipeline != null) {
                    stage.nestedPipeline.createPipes();
                }
            }
        } catch (IOException ex) {
            throw new ShellFailureException("IO error while creating pipes.");
        }
    }

    private CommandIO getOutPipeIO(String name) throws IOException {
        if (parent != null) {
            return parent.getOutPipeIO(name);
        } else {
            Pipeline pipe = pipes.get(name);
            if (pipe == null) {
                pipe = new Pipeline();
                pipes.put(name, pipe);
            }
            return new CommandOutput(pipe.createSource());
        }
    }

    private CommandIO getInPipeIO(String name) throws IOException {
        if (parent != null) {
            return parent.getInPipeIO(name);
        } else {
            Pipeline pipe = pipes.get(name);
            if (pipe == null) {
                pipe = new Pipeline();
                pipes.put(name, pipe);
            }
            return new CommandInput(pipe.createSink());
        }
    }
    
    private void activatePipes() {
        for (Pipeline pipe : pipes.values()) {
            try {
                pipe.activate();
            } catch (IOException ex) {
                pipe.shutdown();
            }
        }
    }

    int run(CommandShell shell) throws ShellException {
        for (PipelineStage stage : stages) {
            stage.thread = stage.command.fork(shell, stage.context);
        }
        activeStageCount = stages.length;
        synchronized (this) {
            for (PipelineStage stage : stages) {
                ThreadCallback callback = new ThreadCallback(stage.context);
                stage.thread.start(callback);
            }
            while (activeStageCount > 0) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return stages[stages.length - 1].thread.getReturnCode();
        }
    }

    void addStage(CommandNode commandNode, BjorneContext context) throws ShellException {
        int i = nextStage++;
        PipelineStage stage = stages[i] = new PipelineStage();
        stage.stageName = stageName();
        stage.command = commandNode;
        stage.holders = context.getHolders();
        if (i > 0) {
            String pipeName = "PIPE-" + stages[i - 1].stageName;
            stages[i - 1].holders[Command.STD_OUT].setIO(
                    new CommandIOMarker(pipeName, CommandIO.DIRECTION_OUT), true);
            stage.holders[Command.STD_IN].setIO(
                    new CommandIOMarker(pipeName, CommandIO.DIRECTION_IN), true);
        }
        stage.context = context;
        stage.nestedPipeline = commandNode.buildPipeline(context);
        context.evaluateRedirections(commandNode.getRedirects(), stage.holders);
    }

    private String stageName() {
        return (parent != null) ? parent.stageName() : ("STAGE-" + ++stageCount);
    }

    private class ThreadCallback implements ThreadExitListener {
        private BjorneContext context;

        public ThreadCallback(BjorneContext context) {
            this.context = context;
        }

        public void notifyThreadExited(CommandThread thread) {
            synchronized (BjornePipeline.this) {
                context.closeIOs();
                activeStageCount--;
                if (activeStageCount <= 0) {
                    BjornePipeline.this.notify();
                }
            }
        }
    }
}
