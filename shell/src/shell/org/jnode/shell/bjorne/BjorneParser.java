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
import static org.jnode.shell.bjorne.BjorneToken.TOK_AND_IF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ASSIGNMENT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_BANG;
import static org.jnode.shell.bjorne.BjorneToken.TOK_BAR;
import static org.jnode.shell.bjorne.BjorneToken.TOK_CASE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_CLOBBER;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DGREAT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESSDASH;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DO;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DONE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DSEMI;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ELIF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ELSE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_LINE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_STREAM;
import static org.jnode.shell.bjorne.BjorneToken.TOK_ESAC;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FI;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREAT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREATAND;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IO_NUMBER;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LBRACE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSAND;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSGREAT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LPAREN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_NAME;
import static org.jnode.shell.bjorne.BjorneToken.TOK_OR_IF;
import static org.jnode.shell.bjorne.BjorneToken.TOK_RBRACE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_RPAREN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_SEMI;
import static org.jnode.shell.bjorne.BjorneToken.TOK_THEN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_UNTIL;
import static org.jnode.shell.bjorne.BjorneToken.TOK_WHILE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_WORD;

import java.util.LinkedList;
import java.util.List;

import org.jnode.shell.ShellFailureException;
import org.jnode.shell.ShellSyntaxException;

public class BjorneParser {
    private final BjorneTokenizer tokens;

    public BjorneParser(BjorneTokenizer tokens) {
        this.tokens = tokens;
    }

    /**
     * Parse 'complete_command ::= list separator | list'
     * 
     * @return the CommandNode representing the complete command.
     * @throws ShellSyntaxException
     */
    public CommandNode parse() throws ShellSyntaxException {
        List<CommandNode> commands = new LinkedList<CommandNode>();
        while (tokens.peek().getTokenType() != TOK_END_OF_STREAM) {
            CommandNode command = parseList();
            commands.add(command);
            tokens.next();
            skipLineBreaks();
        }
        return listToNode(commands);
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
    LOOP: 
        while (command != null) {
            commands.add(command);
            switch (tokens.peek().getTokenType()) {
                case TOK_SEMI:
                    break;
                case TOK_AMP:
                    command.setFlag(FLAG_ASYNC);
                    break;
                case TOK_END_OF_LINE:
                case TOK_END_OF_STREAM:
                    break LOOP;
                default:
                    throw new ShellSyntaxException("unexpected token: " + tokens.peek());
            }
            tokens.next();
            command = parseOptAndOr();
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
    LOOP: 
        while (true) {
            CommandNode command = parsePipeline();
            command.setFlag(flag);
            BjorneToken token = tokens.peek();
            int type = token.getTokenType();
            switch (type) {
                case TOK_AND_IF:
                    flag = FLAG_AND_IF;
                    break;
                case TOK_OR_IF:
                    flag = FLAG_OR_IF;
                    break;
                default:
                    commands.add(command);
                    break LOOP;
            }
            commands.add(command);
            tokens.next();
            skipLineBreaks();
        }
        return listToNode(commands);
    }

    private CommandNode parseOptAndOr() throws ShellSyntaxException {
        switch (tokens.peek(RULE_1_CONTEXT).getTokenType()) {
            case TOK_LBRACE:
            case TOK_LPAREN:
            case TOK_WORD:
            case TOK_IF:
            case TOK_WHILE:
            case TOK_UNTIL:
            case TOK_CASE:
            case TOK_FOR:
            case TOK_IO_NUMBER:
            case TOK_LESS:
            case TOK_GREAT:
            case TOK_DLESS:
            case TOK_DGREAT:
            case TOK_LESSAND:
            case TOK_GREATAND:
            case TOK_LESSGREAT:
            case TOK_CLOBBER:
                return parseAndOr();
            default:
                return null;
        }
    }

    /**
     * Parse 'pipeline ::= '!' pipe_sequence | pipe_sequence'
     * 
     * @return the CommandNode representing the pipeline.
     */
    private CommandNode parsePipeline() throws ShellSyntaxException {
        boolean bang = (tokens.peek().getTokenType() == TOK_BANG);
        if (bang) {
            tokens.next();
        }
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
        while (tokens.peek().getTokenType() == TOK_BAR) {
            tokens.next();
            skipLineBreaks();
            commands.add(parseCommand());
        }
        CommandNode res = listToNode(commands);
        if (res instanceof ListCommandNode) {
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
        switch (tokens.peek(RULE_7a_CONTEXT).getTokenType()) {
            case TOK_IF:
            case TOK_WHILE:
            case TOK_UNTIL:
            case TOK_FOR:
            case TOK_CASE:
            case TOK_LBRACE:
            case TOK_LPAREN:
                return parseCompoundCommand();
            default:
                CommandNode tmp = parseFunctionDefinition();
                if (tmp != null) {
                    return tmp;
                }
                return parseSimpleCommand();
        }
    }

    private CommandNode parseSimpleCommand() throws ShellSyntaxException {
        List<BjorneToken> assignments = new LinkedList<BjorneToken>();
        List<RedirectionNode> redirects = new LinkedList<RedirectionNode>();
        List<BjorneToken> words = new LinkedList<BjorneToken>();

        // Deal with cmd_prefix'es before the command name; i.e. assignments and
        // redirections
        BjorneToken token = null;
        boolean found = false;
        for (int i = 0; !found; i++) {
            token = tokens.peek(i == 0 ? RULE_7a_CONTEXT : RULE_7b_CONTEXT);
            switch (token.getTokenType()) {
                case TOK_ASSIGNMENT:
                    assignments.add(token);
                    tokens.next();
                    break;
                case TOK_IO_NUMBER:
                case TOK_LESS:
                case TOK_GREAT:
                case TOK_DLESS:
                case TOK_DGREAT:
                case TOK_LESSAND:
                case TOK_GREATAND:
                case TOK_LESSGREAT:
                case TOK_CLOBBER:
                    redirects.add(parseRedirect());
                    break;
                default:
                    found = true;
                    break;
            }
        }
        if (token.getTokenType() == TOK_WORD) {
            // This is the command name. Record then consume it.
            words.add(token);
            tokens.next();

            // Deal with any command arguments and embedded / trailing
            // redirections.
        LOOP: 
            while (true) {
                token = tokens.peek();
                switch (token.getTokenType()) {
                    case TOK_IO_NUMBER:
                    case TOK_LESS:
                    case TOK_GREAT:
                    case TOK_DLESS:
                    case TOK_DGREAT:
                    case TOK_LESSAND:
                    case TOK_GREATAND:
                    case TOK_LESSGREAT:
                    case TOK_CLOBBER:
                        redirects.add(parseRedirect());
                        break;
                    case TOK_WORD:
                        words.add(token);
                        tokens.next();
                        break;
                    default:
                        break LOOP;
                }
            }
        } else {
            // An empty command is legal, as are assignments and redirections
            // w/o a command.
        }
        SimpleCommandNode res = 
            new SimpleCommandNode(CMD_COMMAND, words.toArray(new BjorneToken[words.size()]));
        if (!redirects.isEmpty()) {
            res.setRedirects(redirects.toArray(new RedirectionNode[redirects.size()]));
        }
        if (!assignments.isEmpty()) {
            res.setAssignments(assignments.toArray(new BjorneToken[assignments.size()]));
        }
        return res;
    }

    private FunctionDefinitionNode parseFunctionDefinition() throws ShellSyntaxException {
        BjorneToken fname = tokens.peek(RULE_8_CONTEXT);
        if (fname.getTokenType() != TOK_NAME) {
            return null;
        }
        tokens.next();
        if (tokens.peek().getTokenType() != TOK_LPAREN) {
            tokens.backup();
            return null;
        }
        tokens.next();
        if (tokens.next().getTokenType() != TOK_RPAREN) {
            throw new ShellSyntaxException(
                    "expected matching ')' in function_definition");
        }
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
        CommandNode command;
        switch (tokens.peek(RULE_1_CONTEXT).getTokenType()) {
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
            default:
                throw new ShellFailureException("bad token");
        }
        command.setRedirects(parseOptRedirects());
        return command;
    }

    private RedirectionNode parseRedirect() throws ShellSyntaxException {
        BjorneToken token = tokens.next();
        BjorneToken io = null;
        BjorneToken arg = null;
        if (token.getTokenType() == TOK_IO_NUMBER) {
            io = token;
            token = tokens.next();
        }
        int tokenType = token.getTokenType();
        switch (tokenType) {
            case TOK_LESS:
            case TOK_GREAT:
            case TOK_DGREAT:
            case TOK_LESSAND:
            case TOK_GREATAND:
            case TOK_LESSGREAT:
            case TOK_CLOBBER:
                arg = tokens.next();
                if (arg.getTokenType() != TOK_WORD) {
                    throw new ShellSyntaxException("expected a filename after " + token);
                }
                break;
            case TOK_DLESS:
            case TOK_DLESSDASH:
                arg = tokens.next();
                if (arg.getTokenType() != TOK_WORD) {
                    throw new ShellSyntaxException("expected a here-end marker " + token);
                }
                // TODO ... need to grab the HERE document ...
                break;
            default:
                throw new ShellSyntaxException("expected a redirection token");
        }
        // (The corresponding token type and redirection type values are the
        // same ...)
        return new RedirectionNode(tokenType, io, arg);
    }

    private RedirectionNode[] parseOptRedirects() throws ShellSyntaxException {
        List<RedirectionNode> redirects = new LinkedList<RedirectionNode>();
    LOOP:
        while (true) {
            switch (tokens.peek().getTokenType()) {
                case TOK_IO_NUMBER:
                case TOK_LESS:
                case TOK_GREAT:
                case TOK_DLESS:
                case TOK_DLESSDASH:
                case TOK_DGREAT:
                case TOK_LESSAND:
                case TOK_GREATAND:
                case TOK_LESSGREAT:
                case TOK_CLOBBER:
                    redirects.add(parseRedirect());
                    break;
                default:
                    break LOOP;
            }
        }
        if (redirects.isEmpty()) {
            return null;
        }
        return redirects.toArray(new RedirectionNode[redirects.size()]);
    }

    private CommandNode parseSubshell() throws ShellSyntaxException {
        tokens.next();
        CommandNode compoundList = parseCompoundList();
        if (tokens.next().getTokenType() != TOK_RPAREN) {
            throw new ShellSyntaxException("expected ')'");
        }
        compoundList.setNodeType(CMD_SUBSHELL);
        return compoundList;
    }

    private CommandNode parseCompoundList() throws ShellSyntaxException {
        List<CommandNode> commands = new LinkedList<CommandNode>();
        skipLineBreaks();
        CommandNode command = parseAndOr();
    LOOP: 
        while (command != null) {
            commands.add(command);
            skipLineBreaks();
            switch (tokens.peek().getTokenType()) {
                case TOK_SEMI:
                    break;
                case TOK_AMP:
                    command.setFlag(FLAG_ASYNC);
                default:
                    break LOOP;
            }
            tokens.next();
            command = parseOptAndOr();
        }
        return listToNode(commands);
    }

    private CommandNode parseBraceGroup() throws ShellSyntaxException {
        tokens.next();
        CommandNode compoundList = parseCompoundList();
        if (tokens.peek().getTokenType() != TOK_RBRACE) {
            throw new ShellSyntaxException("expected '}'");
        }
        compoundList.setNodeType(CMD_BRACE_GROUP);
        return compoundList;
    }

    private CaseCommandNode parseCaseCommand() throws ShellSyntaxException {
        tokens.next();
        BjorneToken word = tokens.next();
        List<CaseItemNode> caseItems = new LinkedList<CaseItemNode>();
        skipLineBreaks();
        if (tokens.next(RULE_6_CONTEXT).getTokenType() != TOK_IN) {
            throw new ShellSyntaxException("expected 'in' in case_clause");
        }
        skipLineBreaks();
        BjorneToken token = tokens.peek(RULE_1_CONTEXT);
        while (token.getTokenType() != TOK_ESAC) {
            caseItems.add(parseCaseItem());
            skipLineBreaks();
            token = tokens.peek(RULE_1_CONTEXT);
            if (token.getTokenType() == TOK_DSEMI) {
                tokens.next();
                skipLineBreaks();
                token = tokens.peek(RULE_1_CONTEXT);
            } else if (token.getTokenType() != TOK_ESAC) {
                throw new ShellSyntaxException(
                        "expected ';;' or 'esac' after case_item");
            }
        }
        tokens.next();
        return new CaseCommandNode(word, caseItems
                .toArray(new CaseItemNode[caseItems.size()]));
    }

    private CaseItemNode parseCaseItem() throws ShellSyntaxException {
        // Note: we've already ascertained that the first token of the case_item
        // will not
        // be an 'esac', so there's no point applying rule 4
        BjorneToken token = tokens.peek();
        if (token.getTokenType() == TOK_LPAREN) {
            token = tokens.next();
        }
        BjorneToken[] pattern = parsePattern();
        if (tokens.next().getTokenType() != TOK_RPAREN) {
            throw new ShellSyntaxException(
                    "expected ')' after pattern in case_item");
        }
        CommandNode body = null;
        skipLineBreaks();
        token = tokens.peek(RULE_1_CONTEXT);
        if (token.getTokenType() != TOK_DSEMI
                && token.getTokenType() != TOK_ESAC) {
            body = parseCompoundList();
            skipLineBreaks();
        }

        return new CaseItemNode(pattern, body);
    }

    private BjorneToken[] parsePattern() throws ShellSyntaxException {
        List<BjorneToken> pattern = new LinkedList<BjorneToken>();
        while (true) {
            BjorneToken token = tokens.next();
            if (token.getTokenType() != TOK_WORD) {
                throw new ShellSyntaxException("expected WORD in pattern");
            }
            if (tokens.peek().getTokenType() != TOK_BAR) {
                break;
            }
            tokens.next();
        }
        return pattern.toArray(new BjorneToken[pattern.size()]);
    }

    private ForCommandNode parseForCommand() throws ShellSyntaxException {
        tokens.next();
        BjorneToken var = tokens.next(RULE_5_CONTEXT);
        if (var.getTokenType() != TOK_NAME) {
            throw new ShellSyntaxException("expected a NAME following 'for'");
        }
        skipLineBreaks();
        List<BjorneToken> words = new LinkedList<BjorneToken>();
        if (tokens.peek(RULE_6_CONTEXT).getTokenType() == TOK_IN) {
            tokens.next();
            BjorneToken word = tokens.peek();
            while (word.getTokenType() == TOK_WORD) {
                words.add(word);
                tokens.next();
                word = tokens.peek();
            }
            if (words.isEmpty()) {
                throw new ShellSyntaxException(
                        "expected a wordlist following 'in'");
            }
            switch (tokens.peek().getTokenType()) {
                case TOK_SEMI:
                    tokens.next();
                    skipLineBreaks();
                    break;
                case TOK_END_OF_LINE:
                    skipLineBreaks();
                    break;
                default:
                    throw new ShellSyntaxException("expected a ';' following wordlist");
            }
        }
        return new ForCommandNode(var,
                words.toArray(new BjorneToken[words.size()]), parseDoGroup());
    }

    private CommandNode parseDoGroup() throws ShellSyntaxException {
        BjorneToken token = tokens.next(RULE_1_CONTEXT);
        if (token.getTokenType() != TOK_DO) {
            throw new ShellSyntaxException("expected the 'do' of a do_group");
        }
        CommandNode body = parseCompoundList();
        if (tokens.next(RULE_1_CONTEXT).getTokenType() != TOK_DONE) {
            throw new ShellSyntaxException("expected a command or 'done'");
        }
        return body;
    }

    private LoopCommandNode parseUntilCommand() throws ShellSyntaxException {
        tokens.next();
        CommandNode cond = parseCompoundList();
        CommandNode body = parseDoGroup();
        return new LoopCommandNode(CMD_UNTIL, cond, body);
    }

    private LoopCommandNode parseWhileCommand() throws ShellSyntaxException {
        tokens.next();
        CommandNode cond = parseCompoundList();
        CommandNode body = parseDoGroup();
        return new LoopCommandNode(CMD_WHILE, cond, body);
    }

    private IfCommandNode parseIfCommand() throws ShellSyntaxException {
        tokens.next();
        CommandNode cond = parseCompoundList();
        if (tokens.next(RULE_1_CONTEXT).getTokenType() != TOK_THEN) {
            throw new ShellSyntaxException("expected a 'then' in if_clause");
        }
        CommandNode thenPart = parseCompoundList();
        CommandNode elsePart = parseOptElsePart();
        if (tokens.next(RULE_1_CONTEXT).getTokenType() != TOK_FI) {
            throw new ShellSyntaxException("expected an 'elif', 'else' or 'fi'");
        }
        return new IfCommandNode(CMD_IF, cond, thenPart, elsePart);
    }

    private CommandNode parseOptElsePart() throws ShellSyntaxException {
        switch (tokens.next(RULE_1_CONTEXT).getTokenType()) {
            case TOK_ELIF:
                CommandNode cond = parseCompoundList();
                if (tokens.next(RULE_1_CONTEXT).getTokenType() != TOK_THEN) {
                    throw new ShellSyntaxException("expected a 'then' in else_part");
                }
                return new IfCommandNode(CMD_ELIF, cond, parseCompoundList(),
                        parseOptElsePart());
            case TOK_ELSE:
                return parseCompoundList();
            default:
                tokens.backup();
                return null;
        }
    }

    private void skipLineBreaks() {
        while (tokens.peek().getTokenType() == TOK_END_OF_LINE) {
            tokens.next();
        }
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
