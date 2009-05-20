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

import static org.jnode.shell.bjorne.BjorneInterpreter.CMD_BRACE_GROUP;
import static org.jnode.shell.bjorne.BjorneInterpreter.CMD_COMMAND;
import static org.jnode.shell.bjorne.BjorneInterpreter.CMD_ELIF;
import static org.jnode.shell.bjorne.BjorneInterpreter.CMD_IF;
import static org.jnode.shell.bjorne.BjorneInterpreter.CMD_LIST;
import static org.jnode.shell.bjorne.BjorneInterpreter.CMD_SUBSHELL;
import static org.jnode.shell.bjorne.BjorneInterpreter.CMD_UNTIL;
import static org.jnode.shell.bjorne.BjorneInterpreter.CMD_WHILE;
import static org.jnode.shell.bjorne.BjorneInterpreter.FLAG_AND_IF;
import static org.jnode.shell.bjorne.BjorneInterpreter.FLAG_ASYNC;
import static org.jnode.shell.bjorne.BjorneInterpreter.FLAG_BANG;
import static org.jnode.shell.bjorne.BjorneInterpreter.FLAG_OR_IF;
import static org.jnode.shell.bjorne.BjorneInterpreter.FLAG_PIPE;
import static org.jnode.shell.bjorne.BjorneToken.RULE_1_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_5_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_6_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_7a_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_7b_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.RULE_8_CONTEXT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_AMP;
import static org.jnode.shell.bjorne.BjorneToken.TOK_AMP_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_AND_IF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_AND_IF_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ASSIGNMENT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ASSIGNMENT_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_BANG_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_BAR_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_CASE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_CASE_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_CLOBBER_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DGREAT_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESSDASH;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESSDASH_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESS_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DONE_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DO_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DSEMI_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ELIF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ELIF_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ELSE_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_LINE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_LINE_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_STREAM;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_STREAM_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ESAC_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FI_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREATAND_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREAT_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IF_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IN_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IO_NUMBER_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LBRACE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LBRACE_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSAND_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSGREAT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSGREAT_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESS_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LPAREN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LPAREN_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_NAME_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_OR_IF_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_RBRACE_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_RPAREN_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_SEMI;
import static org.jnode.shell.bjorne.BjorneToken.TOK_SEMI_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_THEN_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_UNTIL;
import static org.jnode.shell.bjorne.BjorneToken.TOK_UNTIL_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_WHILE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_WHILE_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_WORD;
import static org.jnode.shell.bjorne.BjorneToken.TOK_WORD_BIT;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jnode.shell.IncompleteCommandException;
import org.jnode.shell.ShellSyntaxException;

/**
 * The BjorneBarser is a simple recursive descent parser/tree builder for the "posix shell"
 * language with the "twist" that it can capture completion information.  This done by
 * causing all token 'expectations' to be expressed as 'expectedSets', and capturing the
 * expected sets that apply when the end-of-stream token and the one before it are parsed.
 * 
 * @author crawley@jnode.org
 */
public class BjorneParser {
    private final BjorneTokenizer tokens;
    private BjorneCompleter completer;
    
    private final String continuationPrompt;
    
    private final List<RedirectionNode> hereRedirections = new ArrayList<RedirectionNode>();
    private boolean allowLineBreaks;

    public BjorneParser(BjorneTokenizer tokens, String continuationPrompt) {
        this.tokens = tokens;
        this.continuationPrompt = continuationPrompt;
    }

    /**
     * Parse a 'complete_command'.
     * <p>
     * Parse 'complete_command ::= list separator | list'
     * 
     * @return the CommandNode representing the complete command.
     * @throws ShellSyntaxException
     */
    public CommandNode parse() throws ShellSyntaxException {
        hereRedirections.clear();
        List<CommandNode> commands = new LinkedList<CommandNode>();
        while (peek().getTokenType() != TOK_END_OF_STREAM) {
            CommandNode command = parseList();
            commands.add(command);
            allowLineBreaks();
        }
        captureHereDocuments();
        return listToNode(commands);
    }
    
    /**
     * Parse a 'complete_command' capturing completions.parseAndOr
     * 
     * @param completer holder object for capturing completion information.
     * @return the CommandNode representing the complete command.
     * @throws ShellSyntaxException
     */
    public CommandNode parse(BjorneCompleter completer) throws ShellSyntaxException {
        this.completer = completer;
        try {
            return parse();
        } finally {
            // Make sure this is nulled ...     
            this.completer = null;
        }
    }

    /**
     * Parse 'list ::= list separator_op and_or | and_or'
     * 
     * @return the CommandNode representing the list.
     * @throws ShellSyntaxException
     */
    private CommandNode parseList() throws ShellSyntaxException {
        List<CommandNode> commands = new LinkedList<CommandNode>();
        CommandNode command = parseAndOr();
        while (command != null) {
            commands.add(command);
            BjorneToken token = expectPeek(
                    TOK_SEMI_BIT | TOK_AMP_BIT | TOK_END_OF_LINE_BIT | TOK_END_OF_STREAM_BIT);
            switch (token.getTokenType()) {
                case TOK_SEMI:
                    next();
                    command = parseOptAndOr();
                    break;
                case TOK_AMP:
                    command.setFlag(FLAG_ASYNC);
                    next();
                    command = parseOptAndOr();
                    break;
                case TOK_END_OF_LINE:
                case TOK_END_OF_STREAM:
                    command = null;
                    break;
            }
        }
        return listToNode(commands);
    }

    /**
     * Parse 'and_or ::= pipeline | and_or AND_IF linebreak pipeline | and_or
     * OR_IF linebreak pipeline'
     * 
     * @return the CommandNode representing the and_or.
     * @throws ShellSyntaxException
     */
    private CommandNode parseAndOr() throws ShellSyntaxException {
        List<CommandNode> commands = new LinkedList<CommandNode>();
        int flag = 0;
        CommandNode command = parsePipeline();
        commands.add(command);
        BjorneToken token;
        while ((token = optNext(TOK_AND_IF_BIT | TOK_OR_IF_BIT)) != null) {
            flag = (token.getTokenType() == TOK_AND_IF) ? FLAG_AND_IF : FLAG_OR_IF;
            skipLineBreaks();
            command = parsePipeline();
            command.setFlag(flag);
            commands.add(command);
        }
        return listToNode(commands);
    }

    private CommandNode parseOptAndOr() throws ShellSyntaxException {
        allowLineBreaks();
        if (optPeek(TOK_LBRACE_BIT | TOK_LPAREN_BIT | TOK_WORD_BIT | TOK_IF_BIT | 
                TOK_WHILE_BIT | TOK_UNTIL_BIT | TOK_CASE_BIT | TOK_FOR_BIT | 
                TOK_IO_NUMBER_BIT | TOK_LESS_BIT | TOK_GREAT_BIT | TOK_DLESS_BIT | 
                TOK_DGREAT_BIT | TOK_LESSAND_BIT | TOK_GREATAND_BIT | TOK_LESSGREAT |
                TOK_CLOBBER_BIT, RULE_1_CONTEXT) != null) {
            return parseAndOr();
        } else {
            return null;
        }
    }

    /**
     * Parse 'pipeline ::= '!' pipe_sequence | pipe_sequence'
     * 
     * @return the CommandNode representing the pipeline.
     */
    private CommandNode parsePipeline() throws ShellSyntaxException {
        peekEager();
        boolean bang = optNext(TOK_BANG_BIT) != null;
        CommandNode pipeSeq = parsePipeSequence();
        if (bang) {
            pipeSeq.setFlag(FLAG_BANG);
        }
        return pipeSeq;
    }

    /**
     * Parse 'pipe_sequence ::= command | pipe_sequence '|' linebreak command'
     * 
     * @return the CommandNode representing the pipe_sequence.
     */
    private CommandNode parsePipeSequence() throws ShellSyntaxException {
        List<CommandNode> commands = new LinkedList<CommandNode>();
        commands.add(parseCommand());
        while (optNext(TOK_BAR_BIT) != null) {
            skipLineBreaks();
            commands.add(parseCommand());
        }
        boolean pipe = commands.size() > 1;
        CommandNode res = listToNode(commands);
        if (pipe) {
            res.setFlag(FLAG_PIPE);
        }
        return res;
    }

    /**
     * Parse 'command ::= simple_command_or_function_definition |
     * compound_command_with_opt_redirects '
     * 
     * @return the CommandNode representing the command.
     * @throws ShellSyntaxException
     */
    private CommandNode parseCommand() throws ShellSyntaxException {
        if (optPeek(TOK_IF_BIT | TOK_WHILE_BIT | TOK_UNTIL_BIT | TOK_FOR_BIT | 
                TOK_CASE_BIT | TOK_LBRACE_BIT | TOK_LPAREN_BIT, RULE_7a_CONTEXT) != null) {
            return parseCompoundCommand();
        } else {
            CommandNode tmp = parseFunctionDefinition();
            return (tmp != null) ? tmp : parseSimpleCommand();
        }
    }

    private CommandNode parseSimpleCommand() throws ShellSyntaxException {
        List<BjorneToken> assignments = new LinkedList<BjorneToken>();
        List<RedirectionNode> redirects = new LinkedList<RedirectionNode>();
        List<BjorneToken> words = new LinkedList<BjorneToken>();
        boolean builtin = false;

        // Deal with cmd_prefix'es before the command name; i.e. assignments and
        // redirections
        BjorneToken token;
        for (int i = 0; ; i++) {
            token = optPeek(TOK_ASSIGNMENT_BIT | TOK_IO_NUMBER_BIT | TOK_LESS_BIT |
                    TOK_GREAT_BIT | TOK_DLESS_BIT | TOK_DGREAT_BIT | TOK_LESSAND_BIT | 
                    TOK_GREATAND_BIT | TOK_LESSGREAT_BIT | TOK_CLOBBER_BIT, 
                    i == 0 ? RULE_7a_CONTEXT : RULE_7b_CONTEXT);
            if (token == null) {
                break;
            }
            if (token.getTokenType() == TOK_ASSIGNMENT) {
                assignments.add(token);
                next();
            } else {
                redirects.add(parseRedirect());
            }
        }
        token = optNext(TOK_WORD_BIT);
        try {
            if (token != null) {
                // This is the command name.
                words.add(token);

                // Deal with any command arguments and embedded / trailing
                // redirections.
                while ((token = optPeek(TOK_WORD_BIT | TOK_IO_NUMBER_BIT | TOK_LESS_BIT |
                        TOK_GREAT_BIT | TOK_DLESS_BIT | TOK_DGREAT_BIT | TOK_LESSAND_BIT | 
                        TOK_GREATAND_BIT | TOK_LESSGREAT_BIT | TOK_CLOBBER_BIT)) != null) {
                    if (token.getTokenType() == TOK_WORD) {
                        words.add(token);
                        next();
                    } else {
                        redirects.add(parseRedirect());
                    }
                }
                String commandWord = words.get(0).getText();
                builtin = BjorneInterpreter.isBuiltin(commandWord);
                // FIXME ... built-in commands should use the Syntax mechanisms so
                // that completion, help, etc will work as expected.
            } else {
                // An empty command is legal, as are assignments and redirections
                // w/o a command.
            }
        } catch (IncompleteCommandException ex) {
            if (completer != null) {
                completer.setCommand(new SimpleCommandNode(CMD_COMMAND, 
                        words.toArray(new BjorneToken[words.size()]), builtin));
            }
            throw ex;
        } catch (ShellSyntaxException ex) {
            if (completer != null) {
                completer.setCommand(words.size() == 0 ? null : 
                    new SimpleCommandNode(CMD_COMMAND, 
                            words.toArray(new BjorneToken[words.size()]), builtin));
            }
            throw ex;
        }
        SimpleCommandNode res = new SimpleCommandNode(CMD_COMMAND, 
                words.toArray(new BjorneToken[words.size()]), builtin);
        if (completer != null) {
            completer.setCommand(words.size() == 0 ? null : res);
        }
        if (!redirects.isEmpty()) {
            res.setRedirects(redirects.toArray(new RedirectionNode[redirects.size()]));
        }
        if (!assignments.isEmpty()) {
            res.setAssignments(assignments.toArray(new BjorneToken[assignments.size()]));
        }
        return res;
    }

    private FunctionDefinitionNode parseFunctionDefinition() throws ShellSyntaxException {
        BjorneToken fname = optNext(TOK_NAME_BIT, RULE_8_CONTEXT);
        if (fname == null) {
            return null;
        }
        if (optPeek(TOK_LPAREN_BIT) == null) {
            tokens.backup();
            return null;
        } else {
            next();
        }
        expectNext(TOK_RPAREN_BIT);
        skipLineBreaks();
        return new FunctionDefinitionNode(fname, parseFunctionBody());
    }

    private CommandNode parseFunctionBody() throws ShellSyntaxException {
        // TODO ... need to set the context to 'rule 9' while parsing the
        // function body
        CommandNode body = parseCompoundCommand();
        body.setRedirects(parseOptRedirects());
        return body;
    }

    /**
     * Parse 'compound_command_with_opt_redirects ::= ( if_command |
     * while_command | until_command | for_command | case_command | brace_group |
     * subshell ) [ redirects ]
     * 
     * @return the CommandNode representing the compound command.
     * @throws ShellSyntaxException
     */
    private CommandNode parseCompoundCommand() throws ShellSyntaxException {
        CommandNode command = null;
        BjorneToken token = expectPeek(TOK_IF_BIT | TOK_WHILE_BIT | TOK_UNTIL_BIT | TOK_FOR_BIT | 
                TOK_CASE_BIT | TOK_LBRACE_BIT | TOK_LPAREN_BIT, RULE_1_CONTEXT);
        switch (token.getTokenType()) {
            case TOK_IF:
                command = parseIfCommand();
                break;
            case TOK_WHILE:
                command = parseWhileCommand();
                break;
            case TOK_UNTIL:
                command = parseUntilCommand();
                break;
            case TOK_FOR:
                command = parseForCommand();
                break;
            case TOK_CASE:
                command = parseCaseCommand();
                break;
            case TOK_LBRACE:
                command = parseBraceGroup();
                break;
            case TOK_LPAREN:
                command = parseSubshell();
                break;
        }
        command.setRedirects(parseOptRedirects());
        return command;
    }

    private RedirectionNode parseRedirect() throws ShellSyntaxException {
        BjorneToken io = optNext(TOK_IO_NUMBER_BIT);
        BjorneToken arg = null;
        BjorneToken token = expectNext(TOK_LESS_BIT | TOK_GREAT_BIT | TOK_DGREAT_BIT | 
                TOK_LESSAND_BIT | TOK_GREATAND_BIT | TOK_LESSGREAT_BIT | TOK_CLOBBER_BIT | 
                TOK_DLESS_BIT | TOK_DLESSDASH_BIT);
        int tt = token.getTokenType();
        if (tt == TOK_DLESS || tt == TOK_DLESSDASH) {
            arg = expectNext(TOK_WORD_BIT);
            RedirectionNode res = new RedirectionNode(tt, io, arg);
            // (HERE document capture will start when we reach the next 
            // real (i.e. not '\' escaped) line break ... see processLineBreaks())
            hereRedirections.add(res);
            return res;
        } else {
            arg = expectNext(TOK_WORD_BIT);
            // (Corresponding token type and redirection type values are the same)
            return new RedirectionNode(tt, io, arg);
        }
    }

    private RedirectionNode[] parseOptRedirects() throws ShellSyntaxException {
        List<RedirectionNode> redirects = new LinkedList<RedirectionNode>();
        while (optPeek(TOK_LESS_BIT | TOK_GREAT_BIT | TOK_DGREAT_BIT | 
                TOK_LESSAND_BIT | TOK_GREATAND_BIT | TOK_LESSGREAT_BIT | TOK_CLOBBER_BIT | 
                TOK_DLESS_BIT | TOK_DLESSDASH_BIT | TOK_IO_NUMBER_BIT) != null) {
            redirects.add(parseRedirect());
        }
        if (redirects.isEmpty()) {
            return null;
        }
        return redirects.toArray(new RedirectionNode[redirects.size()]);
    }

    private CommandNode parseSubshell() throws ShellSyntaxException {
        next();
        CommandNode compoundList = parseCompoundList();
        expectNext(TOK_RPAREN_BIT);
        compoundList.setNodeType(CMD_SUBSHELL);
        return compoundList;
    }

    private CommandNode parseCompoundList() throws ShellSyntaxException {
        List<CommandNode> commands = new LinkedList<CommandNode>();
        skipLineBreaks();
        CommandNode command = parseAndOr();
        while (command != null) {
            commands.add(command);
            BjorneToken token = optNext(TOK_SEMI_BIT | TOK_AMP_BIT | TOK_END_OF_LINE_BIT);
            if (token == null) {
                break;
            } else if (token.getTokenType() == TOK_AMP) {
                command.setFlag(FLAG_ASYNC);
            }
            skipLineBreaks();
            command = parseOptAndOr();
        }
        return listToNode(commands);
    }

    private CommandNode parseBraceGroup() throws ShellSyntaxException {
        next();
        CommandNode compoundList = parseCompoundList();
        expectPeek(TOK_RBRACE_BIT);
        compoundList.setNodeType(CMD_BRACE_GROUP);
        return compoundList;
    }

    private CaseCommandNode parseCaseCommand() throws ShellSyntaxException {
        next();
        BjorneToken word = expectNext(TOK_WORD_BIT);
        List<CaseItemNode> caseItems = new LinkedList<CaseItemNode>();
        skipLineBreaks();
        expectNext(TOK_IN_BIT, RULE_6_CONTEXT);
        skipLineBreaks();
        while (optNext(TOK_ESAC_BIT, RULE_1_CONTEXT) == null) {
            caseItems.add(parseCaseItem());
            skipLineBreaks();
            if (optNext(TOK_DSEMI_BIT, RULE_1_CONTEXT) != null) {
                skipLineBreaks();
            }
        }
        return new CaseCommandNode(word, caseItems.toArray(new CaseItemNode[caseItems.size()]));
    }

    private CaseItemNode parseCaseItem() throws ShellSyntaxException {
        // Note: we've already ascertained that the first token of the case_item
        // will not be an 'esac', so there's no point applying rule 4
        optNext(TOK_LPAREN_BIT);
        BjorneToken[] pattern = parsePattern();
        expectNext(TOK_RPAREN_BIT);
        CommandNode body = null;
        skipLineBreaks();
        if (optPeek(TOK_DSEMI_BIT | TOK_ESAC_BIT, RULE_1_CONTEXT) == null) {
            body = parseCompoundList();
            skipLineBreaks();
        }
        return new CaseItemNode(pattern, body);
    }

    private BjorneToken[] parsePattern() throws ShellSyntaxException {
        List<BjorneToken> pattern = new LinkedList<BjorneToken>();
        while (true) {
            BjorneToken token = expectNext(TOK_WORD_BIT);
            pattern.add(token);
            if (optNext(TOK_BAR_BIT) == null) {
                break;
            }
        }
        return pattern.toArray(new BjorneToken[pattern.size()]);
    }

    private ForCommandNode parseForCommand() throws ShellSyntaxException {
        next();
        BjorneToken var = expectNext(TOK_NAME_BIT, RULE_5_CONTEXT);
        skipLineBreaks();
        List<BjorneToken> words = new LinkedList<BjorneToken>();
        if (optNext(TOK_IN_BIT, RULE_6_CONTEXT) != null) {
            BjorneToken word = expectNext(TOK_WORD_BIT);
            do {
                words.add(word);
                word = optNext(TOK_WORD_BIT);
            } while (word != null);
            expectNext(TOK_SEMI_BIT | TOK_END_OF_LINE_BIT);
        }
        return new ForCommandNode(var,
                words.toArray(new BjorneToken[words.size()]), parseDoGroup());
    }

    private CommandNode parseDoGroup() throws ShellSyntaxException {
        skipLineBreaks();
        expectNext(TOK_DO_BIT, RULE_1_CONTEXT);
        CommandNode body = parseCompoundList();
        skipLineBreaks();
        expectNext(TOK_DONE_BIT, RULE_1_CONTEXT);
        return body;
    }

    private LoopCommandNode parseUntilCommand() throws ShellSyntaxException {
        next();
        CommandNode cond = parseCompoundList();
        CommandNode body = parseDoGroup();
        return new LoopCommandNode(CMD_UNTIL, cond, body);
    }

    private LoopCommandNode parseWhileCommand() throws ShellSyntaxException {
        next();
        CommandNode cond = parseCompoundList();
        CommandNode body = parseDoGroup();
        return new LoopCommandNode(CMD_WHILE, cond, body);
    }

    private IfCommandNode parseIfCommand() throws ShellSyntaxException {
        next();
        CommandNode cond = parseCompoundList();
        skipLineBreaks();
        expectNext(TOK_THEN_BIT, RULE_1_CONTEXT);
        CommandNode thenPart = parseCompoundList();
        CommandNode elsePart = parseOptElsePart();
        skipLineBreaks();
        expectNext(TOK_FI_BIT, RULE_1_CONTEXT);
        return new IfCommandNode(CMD_IF, cond, thenPart, elsePart);
    }

    private CommandNode parseOptElsePart() throws ShellSyntaxException {
        skipLineBreaks();
        BjorneToken token = optNext(TOK_ELIF_BIT | TOK_ELSE_BIT, RULE_1_CONTEXT);
        if (token == null) {
            return null;
        } else if (token.getTokenType() == TOK_ELIF) {
            CommandNode cond = parseCompoundList();
            skipLineBreaks();
            expectNext(TOK_THEN_BIT, RULE_1_CONTEXT);
            return new IfCommandNode(CMD_ELIF, cond, parseCompoundList(), parseOptElsePart());
        } else {
            return parseCompoundList();
        } 
    }
    
    private BjorneToken optNext(long expectedSet, int context) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(false);
        }
        BjorneToken token = tokens.next(context);
        if (expect(token, expectedSet, false)) {
            return token;
        } else {
            tokens.backup();
            return null;
        }
    }

    private BjorneToken optNext(long expectedSet) throws ShellSyntaxException {
        BjorneToken token = next();
        if (expect(token, expectedSet, false)) {
            return token;
        } else {
            tokens.backup();
            return null;
        }
    }

    private BjorneToken optPeek(long expectedSet, int context) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(false);
        }
        BjorneToken token = tokens.peek(context);
        return expect(token, expectedSet, false) ? token : null;
    }

    private BjorneToken optPeek(long expectedSet) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(false);
        }
        BjorneToken token = tokens.peek();
        return expect(token, expectedSet, false) ? token : null;
    }

    private BjorneToken expectNext(long expectedSet, int context) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(true);
        }
        BjorneToken token = tokens.next(context);
        expect(token, expectedSet, true);
        return token;
    }

    private BjorneToken expectNext(long expectedSet) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(true);
        }
        BjorneToken token = tokens.next();
        expect(token, expectedSet, true);
        return token;
    }

    private BjorneToken expectPeek(long expectedSet, int context) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(true);
        }
        BjorneToken token =  tokens.peek(context);
        expect(token, expectedSet, true);
        return token;
    }

    private BjorneToken expectPeek(long expectedSet) throws ShellSyntaxException {
        BjorneToken token = peekEager();
        expect(token, expectedSet, true);
        return token;
    }
    
    private BjorneToken next() throws IncompleteCommandException {
        if (allowLineBreaks) {
            doLineBreaks(false);
        }
        return tokens.next();
    }
    
    private BjorneToken peek() throws IncompleteCommandException {
        if (allowLineBreaks) {
            doLineBreaks(false);
        }
        return tokens.peek();
    }
    
    private BjorneToken peekEager() throws IncompleteCommandException {
        if (allowLineBreaks) {
            doLineBreaks(true);
        }
        return tokens.peek();
    }

    private boolean expect(BjorneToken token, long expectedSet, boolean mandatory) 
        throws ShellSyntaxException {
        captureCompletions(token, expectedSet);
        int tt = token.getTokenType();
        if (((1L << tt) & expectedSet) == 0L) {
            if (mandatory) {
                if (tt == TOK_END_OF_STREAM) {
                    throw new IncompleteCommandException(
                            "EOF reached while looking for " + formatExpectedSet(expectedSet), 
                            continuationPrompt);
                } else {
                    throw new ShellSyntaxException(
                            "expected " + formatExpectedSet(expectedSet) + " but got " + token);
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void captureCompletions(BjorneToken token, long expectedSet) {
        if (completer != null) {
            // Capture tokens and expectedSets for later use in determining what
            // completions to allow.
            int tt = token.getTokenType();
            if (tt == TOK_END_OF_STREAM) {
                if (completer.getEndToken() == null) {
                    completer.setEndToken(token);
                    completer.setEndExpectedSet(expectedSet);
                } else {
                    completer.addToEndExpectedSet(expectedSet);
                }
            } else {
                BjorneToken pt = completer.getPenultimateToken();
                if (pt == null || pt.start < token.start) {
                    completer.setPenultimateToken(token);
                    completer.setPenultimateExpectedSet(expectedSet);
                } else {
                    completer.addToPenultimateExpectedSet(expectedSet);
                }
            }
        }
    }

    private String formatExpectedSet(long expectedSet) {
        StringBuilder sb = new StringBuilder(40);
        long mask = 1L;
        for (int i = 0; i < 64 && expectedSet != 0L; i++) {
            if ((expectedSet & mask) != 0) {
                if (sb.length() > 0) {
                    if (expectedSet != 0L) {
                        sb.append(", ");
                    } else {
                        sb.append(" or ");
                    }
                }
                sb.append(BjorneToken.toString(i));
                expectedSet &= ~mask;
            }
            mask <<= 1;
        }
        return sb.toString();
    }

    private void skipLineBreaks() throws IncompleteCommandException {
        this.allowLineBreaks = true;
        doLineBreaks(true);
    }

    private void allowLineBreaks() throws IncompleteCommandException {
        this.allowLineBreaks = true;
    }

    private void doLineBreaks(boolean needMore) throws IncompleteCommandException {
        // NB: use tokens.peek() / next() rather than the wrappers here!!
        this.allowLineBreaks = false;
        BjorneToken token = tokens.peek();
        captureCompletions(token, TOK_END_OF_LINE_BIT);
        int tt = token.getTokenType();
        if (needMore && tt == TOK_END_OF_STREAM) {
            throw new IncompleteCommandException(
                    "EOF reached while looking for optional linebreak(s)", 
                    continuationPrompt);
        } else if (tt == TOK_END_OF_LINE) {
            tokens.next();
            captureHereDocuments();
            while (true) {
                token = tokens.peek();
                tt = token.getTokenType();
                if (tt == TOK_END_OF_LINE) {
                    tokens.next();
                } else if (needMore && tt == TOK_END_OF_STREAM) {
                    throw new IncompleteCommandException(
                            "EOF reached while looking for optional linebreak(s)", 
                            continuationPrompt);
                } else {
                    break;
                }
            }
        }
    }

    private void captureHereDocuments() throws IncompleteCommandException {
        for (RedirectionNode redirection : hereRedirections) {
            StringBuilder sb = new StringBuilder();
            String marker = redirection.getArg().getText();
            boolean trimTabs = redirection.getRedirectionType() == TOK_DLESSDASH;
            while (true) {
                String line = tokens.readHereLine(trimTabs);
                if (line == null) {
                    throw new IncompleteCommandException("EOF reached while looking for '" +
                            marker + "' to end a HERE document", continuationPrompt);
                }
                if (line.equals(marker)) {
                    break;
                }
                sb.append(line).append('\n');
            }
            redirection.setHereDocument(sb.toString());
        }
        hereRedirections.clear();
    }

    private CommandNode listToNode(List<? extends CommandNode> commands) {
        int len = commands.size();
        if (len == 0) {
            return null;
        } else if (len == 1) {
            return commands.get(0);
        } else {
            return new ListCommandNode(CMD_LIST, commands);
        }
    }
}
