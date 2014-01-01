/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
import static org.jnode.shell.bjorne.BjorneToken.TOK_CASE_WORD_BITS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_CLOBBER_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_COMMAND_NAME_BITS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_COMMAND_WORD_BITS;
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
import static org.jnode.shell.bjorne.BjorneToken.TOK_FILE_NAME_BITS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FI_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR_NAME_BITS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR_WORD_BITS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FUNCTION_NAME_BITS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREATAND_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREAT_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_HERE_END_BITS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IF_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IN_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IO_NUMBER_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LBRACE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LBRACE_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSAND_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSGREAT_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESS_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LPAREN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LPAREN_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_OR_IF_BIT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_PATTERN_BITS;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    
    private final List<RedirectionNode> hereRedirections = new ArrayList<RedirectionNode>();
    private boolean allowLineBreaks;

    public BjorneParser(BjorneTokenizer tokens) {
        this.tokens = tokens;
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
        CommandNode command = parseOptList();
        if (command != null) {
            allowLineBreaks();
            captureHereDocuments();
        } else {
            noLineBreaks();
            if (optNext(TOK_END_OF_LINE_BIT) != null) {
                command = new SimpleCommandNode(CMD_COMMAND, new BjorneToken[0]);
            }
        }
        return command;
    }
    
    /**
     * Parse a 'complete_command' capturing completions.
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
     * Parse 'list ::= list separator_op and_or | and_or' or empty
     * 
     * @return the CommandNode representing the list.
     * @throws ShellSyntaxException
     */
    private CommandNode parseOptList() throws ShellSyntaxException {
        List<CommandNode> commands = new LinkedList<CommandNode>();
        CommandNode command = parseOptAndOr();
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
        // allowLineBreaks();
        if (optPeek(TOK_LBRACE_BIT | TOK_LPAREN_BIT | TOK_COMMAND_NAME_BITS | TOK_FUNCTION_NAME_BITS |  
                TOK_IF_BIT | TOK_WHILE_BIT | TOK_UNTIL_BIT | TOK_CASE_BIT | TOK_FOR_BIT | 
                TOK_IO_NUMBER_BIT | TOK_LESS_BIT | TOK_GREAT_BIT | TOK_DLESS_BIT | 
                TOK_DGREAT_BIT | TOK_LESSAND_BIT | TOK_GREATAND_BIT | TOK_LESSGREAT_BIT |
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

        // Deal with cmd_prefix'es before the command name; i.e. assignments and
        // redirections
        BjorneToken token;
        for (int i = 0; /**/; i++) {
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
        if (assignments.isEmpty() && redirects.isEmpty()) {
            // An empty command without assignments or redirections is illegal ...
            // but the real reason to call expectNext is to ensure that we don't
            // get stuck on a line-break.
            token = expectNext(TOK_COMMAND_NAME_BITS);
        } else {
            // An empty command with assignments and/or redirections is legal
            token = optNext(TOK_COMMAND_NAME_BITS);
        }
        try {
            if (token != null) {
                // This is the command name.
                words.add(token);

                // Deal with any command arguments and embedded / trailing
                // redirections.
                while ((token = optPeek(TOK_COMMAND_WORD_BITS | TOK_IO_NUMBER_BIT | TOK_LESS_BIT |
                        TOK_GREAT_BIT | TOK_DLESS_BIT | TOK_DGREAT_BIT | TOK_LESSAND_BIT | 
                        TOK_GREATAND_BIT | TOK_LESSGREAT_BIT | TOK_CLOBBER_BIT)) != null) {
                    if (token.getTokenType() == TOK_WORD) {
                        words.add(token);
                        next();
                    } else {
                        redirects.add(parseRedirect());
                    }
                }
            } 
        } catch (ShellSyntaxException ex) {
            if (completer != null) {
                completer.setCommand(words.size() == 0 ? null : 
                    new SimpleCommandNode(CMD_COMMAND, 
                            words.toArray(new BjorneToken[words.size()])));
            }
            throw ex;
        }
        SimpleCommandNode res = new SimpleCommandNode(CMD_COMMAND, 
                words.toArray(new BjorneToken[words.size()]));
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
        BjorneToken fname = optNext(TOK_FUNCTION_NAME_BITS, RULE_8_CONTEXT);
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
            arg = expectNext(TOK_HERE_END_BITS);
            RedirectionNode res = new RedirectionNode(tt, io, arg);
            // (HERE document capture will start when we reach the next 
            // real (i.e. not '\' escaped) line break ... see processLineBreaks())
            hereRedirections.add(res);
            return res;
        } else {
            arg = expectNext(TOK_FILE_NAME_BITS);
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
        CommandNode compoundList = parseCompoundList(TOK_RPAREN_BIT);
        expectNext(TOK_RPAREN_BIT);
        compoundList.setNodeType(CMD_SUBSHELL);
        return compoundList;
    }

    private CommandNode parseCompoundList(long terminatorSet) throws ShellSyntaxException {
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
            // (This helps the completer figure out alternative completions for a word
            // in the command name position.)
            token = optPeek(terminatorSet);
            command = parseOptAndOr();
        }
        return listToNode(commands);
    }

    private CommandNode parseBraceGroup() throws ShellSyntaxException {
        next();
        CommandNode compoundList = parseCompoundList(TOK_RBRACE_BIT);
        expectNext(TOK_RBRACE_BIT, RULE_1_CONTEXT);
        compoundList.setNodeType(CMD_BRACE_GROUP);
        return compoundList;
    }

    private CaseCommandNode parseCaseCommand() throws ShellSyntaxException {
        next();
        BjorneToken word = expectNext(TOK_CASE_WORD_BITS);
        List<CaseItemNode> caseItems = new LinkedList<CaseItemNode>();
        allowLineBreaks();
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
            body = parseCompoundList(0L);
            skipLineBreaks();
        }
        return new CaseItemNode(pattern, body);
    }

    private BjorneToken[] parsePattern() throws ShellSyntaxException {
        List<BjorneToken> pattern = new LinkedList<BjorneToken>();
        while (true) {
            BjorneToken token = expectNext(TOK_PATTERN_BITS);
            pattern.add(token);
            if (optNext(TOK_BAR_BIT) == null) {
                break;
            }
        }
        return pattern.toArray(new BjorneToken[pattern.size()]);
    }

    private ForCommandNode parseForCommand() throws ShellSyntaxException {
        next();
        BjorneToken var = expectNext(TOK_FOR_NAME_BITS, RULE_5_CONTEXT);
        skipLineBreaks();
        List<BjorneToken> words = new LinkedList<BjorneToken>();
        if (optNext(TOK_IN_BIT, RULE_6_CONTEXT) != null) {
            BjorneToken word = expectNext(TOK_FOR_WORD_BITS);
            do {
                words.add(word);
                word = optNext(TOK_FOR_WORD_BITS);
            } while (word != null);
            expectNext(TOK_SEMI_BIT | TOK_END_OF_LINE_BIT);
        }
        return new ForCommandNode(var,
                words.toArray(new BjorneToken[words.size()]), parseDoGroup());
    }

    private CommandNode parseDoGroup() throws ShellSyntaxException {
        allowLineBreaks();
        expectNext(TOK_DO_BIT, RULE_1_CONTEXT);
        CommandNode body = parseCompoundList(TOK_DONE_BIT);
        allowLineBreaks();
        expectNext(TOK_DONE_BIT, RULE_1_CONTEXT);
        return body;
    }

    private LoopCommandNode parseUntilCommand() throws ShellSyntaxException {
        next();
        CommandNode cond = parseCompoundList(TOK_DO_BIT);
        CommandNode body = parseDoGroup();
        return new LoopCommandNode(CMD_UNTIL, cond, body);
    }

    private LoopCommandNode parseWhileCommand() throws ShellSyntaxException {
        next();
        CommandNode cond = parseCompoundList(TOK_DO_BIT);
        CommandNode body = parseDoGroup();
        return new LoopCommandNode(CMD_WHILE, cond, body);
    }

    private IfCommandNode parseIfCommand() throws ShellSyntaxException {
        next();
        CommandNode cond = parseCompoundList(TOK_THEN_BIT);
        allowLineBreaks();
        expectNext(TOK_THEN_BIT, RULE_1_CONTEXT);
        CommandNode thenPart = parseCompoundList(TOK_ELIF_BIT | TOK_ELSE_BIT | TOK_FI_BIT);
        CommandNode elsePart = parseOptElsePart();
        allowLineBreaks();
        expectNext(TOK_FI_BIT, RULE_1_CONTEXT);
        return new IfCommandNode(CMD_IF, cond, thenPart, elsePart);
    }

    private CommandNode parseOptElsePart() throws ShellSyntaxException {
        skipLineBreaks();
        BjorneToken token = optNext(TOK_ELIF_BIT | TOK_ELSE_BIT, RULE_1_CONTEXT);
        if (token == null) {
            return null;
        } else if (token.getTokenType() == TOK_ELIF) {
            CommandNode cond = parseCompoundList(TOK_THEN_BIT);
            allowLineBreaks();
            expectNext(TOK_THEN_BIT, RULE_1_CONTEXT);
            return new IfCommandNode(CMD_ELIF, cond, 
                    parseCompoundList(TOK_ELIF_BIT | TOK_ELSE_BIT | TOK_FI_BIT), parseOptElsePart());
        } else {
            return parseCompoundList(TOK_FI_BIT);
        } 
    }
    
    private BjorneToken optNext(long expectedSet, int context) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(expectedSet, false);
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
        if (allowLineBreaks) {
            doLineBreaks(expectedSet, false);
        }
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
            doLineBreaks(expectedSet, false);
        }
        BjorneToken token = tokens.peek(context);
        return expect(token, expectedSet, false) ? token : null;
    }

    private BjorneToken optPeek(long expectedSet) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(expectedSet, false);
        }
        BjorneToken token = tokens.peek();
        return expect(token, expectedSet, false) ? token : null;
    }

    private BjorneToken expectNext(long expectedSet, int context) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(expectedSet, true);
        }
        BjorneToken token = tokens.next(context);
        expect(token, expectedSet, true);
        return token;
    }

    private BjorneToken expectNext(long expectedSet) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(expectedSet, true);
        }
        BjorneToken token = tokens.next();
        expect(token, expectedSet, true);
        return token;
    }

    private BjorneToken expectPeek(long expectedSet, int context) throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(expectedSet, true);
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
    
    private BjorneToken next() throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(0L, false);
        }
        return tokens.next();
    }
    
    private BjorneToken peekEager() throws ShellSyntaxException {
        if (allowLineBreaks) {
            doLineBreaks(0L, true);
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
                    throw new ShellSyntaxException(
                            "EOF reached while looking for " + BjorneToken.formatExpectedSet(expectedSet));
                } else {
                    throw new ShellSyntaxException(
                            "expected " + BjorneToken.formatExpectedSet(expectedSet) + " but got " + token);
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
            BjorneToken et = completer.getEndToken();
            BjorneToken pt = completer.getPenultimateToken();
            int tt = token.getTokenType();
            if (tt == TOK_END_OF_STREAM) {
                if (et == null) {
                    completer.setEndToken(token);
                    completer.setEndExpectedSet(expectedSet);
                } else {
                    completer.addToEndExpectedSet(expectedSet);
                }
            } else {
                if (pt == null || pt.start != token.start) {
                    completer.setPenultimateToken(token);
                    if (pt != null && pt.getTokenType() == TOK_END_OF_LINE && pt.start < token.start) {
                        completer.addToPenultimateExpectedSet(expectedSet);
                    } else {
                        completer.setPenultimateExpectedSet(expectedSet);
                    }
                } else {
                    completer.addToPenultimateExpectedSet(expectedSet);
                }
            }
        }
    }
    
    private void skipLineBreaks() throws ShellSyntaxException {
        this.allowLineBreaks = true;
        doLineBreaks(0L, false);
    }

    private void allowLineBreaks() throws ShellSyntaxException {
        this.allowLineBreaks = true;
    }

    private void noLineBreaks() throws ShellSyntaxException {
        this.allowLineBreaks = false;
    }

    /**
     * Skip optional linebreaks.
     * @param expectedSet the tokens expected after the line breaks
     * @param needMore if {@code true} we need a token after the line breaks,
     *     otherwise, it is OK to have no more tokens.
     */
    private void doLineBreaks(long expectedSet, boolean needMore) throws ShellSyntaxException {
        // NB: use tokens.peek() / next() rather than the wrappers here!!
        this.allowLineBreaks = false;
        BjorneToken token = tokens.peek();
        int tt = token.getTokenType();
        if (tt == TOK_END_OF_STREAM) {
            captureCompletions(token, expectedSet);
            if (needMore) {
                throw new ShellSyntaxException(
                        "EOF reached while looking for optional linebreak(s)");
            } 
        } else if (tt == TOK_END_OF_LINE) {
            tokens.next();
            captureHereDocuments();
            while (true) {
                token = tokens.peek();
                tt = token.getTokenType();
                if (tt == TOK_END_OF_LINE) {
                    tokens.next();
                } else if (tt == TOK_END_OF_STREAM) {
                    captureCompletions(token, expectedSet);
                    if (needMore) {
                        throw new ShellSyntaxException(
                                "EOF reached while dealing with optional linebreak(s)");
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Capture all current HERE documents, dealing with TAB stripping (if required)
     * and recording on the relevant redirection object.  We don't do expansions at
     * this point, but we set the flag to say if expansion is required.
     * 
     * @throws ShellSyntaxException
     */
    private void captureHereDocuments() throws ShellSyntaxException {
        for (RedirectionNode redirection : hereRedirections) {
            StringBuilder sb = new StringBuilder();
            String rawMarker = redirection.getArg().getText();
            String marker = BjorneContext.dequote(rawMarker).toString();
            boolean trimTabs = redirection.getRedirectionType() == TOK_DLESSDASH;
            while (true) {
                String line = tokens.readRawLine();
                if (line == null) {
                    throw new ShellSyntaxException("EOF reached while looking for '" +
                            marker + "' to end a HERE document");
                }
                if (trimTabs) {
                    int len = line.length();
                    int i;
                    for (i = 0; i < len && line.charAt(i) == '\t'; i++) { /**/  }
                    if (i > 0) {
                        line = line.substring(i);
                    }
                }
                if (line.equals(marker)) {
                    break;
                }
                sb.append(line).append('\n');
            }
            redirection.setHereDocument(sb.toString());
            redirection.setHereDocumentExpandable(marker.equals(rawMarker));
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

    public String getContinuationPrompt() {
        // FIXME ... use PS2, PS4 or whatever as appropriate.
        return "> ";
    }
}
