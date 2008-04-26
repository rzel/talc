/*
 * This file is part of Talc.
 * Copyright (C) 2007-2008 Elliott Hughes <enh@jessies.org>.
 * 
 * Talc is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jessies.talc;

import java.io.*;
import java.util.*;

public class Talc {
    private static final boolean[] debuggingFlags = new boolean[127];
    private static final String[] debuggingFlagNames = new String[127];
    static {
        debuggingFlagNames['C'] = "doesn't use a synthetic 'constant pool' for int and real constants";
        debuggingFlagNames['i'] = "shows each inferred type as it's fixed up";
        debuggingFlagNames['l'] = "shows each token returned by the lexer";
        debuggingFlagNames['n'] = "doesn't execute the generated code";
        debuggingFlagNames['o'] = "doesn't optimize the AST before generating code";
        debuggingFlagNames['p'] = "shows information about parsing as it progresses, and the AST for each completed parse";
        debuggingFlagNames['t'] = "shows timing information for each phase of compilation/execution";
        debuggingFlagNames['T'] = "shows information helpful when debugging the type checker";
        debuggingFlagNames['S'] = "shows the generated JVM bytecodes";
        debuggingFlagNames['s'] = "saves the generated code to /tmp";
        debuggingFlagNames['v'] = "verifies the generated code with ASM's verifier (implies 's'; libasm3-java must be installed)";
    }
    
    public Talc() {
    }
    
    private void reportTime(String task, long ns) {
        if (Talc.debugging('t')) {
            double s = ns/1000000000.0;
            System.err.println("[talc] " + new java.text.DecimalFormat("#.####").format(s) + "s " + task);
            // GNU Classpath's String.format seems to be broken at the moment, preventing me from using this:
            //System.err.println(String.format("[talc] %.4fs %s", s, task));
        }
    }
    
    private void parseAndEvaluate(String argv0, String[] args, Lexer lexer) {
        // 1. Parse.
        long parse0 = System.nanoTime();
        List<AstNode> ast = new Parser(lexer).parse();
        reportTime("parsing", System.nanoTime() - parse0);
        
        // 1a. Insert values for built-in constants.
        Scope.initGlobalScope(argv0);
        
        // 2. Compile-time checking.
        // 2a. Set up the symbol table.
        SymbolTable symbolTable = new SymbolTable(ast);
        reportTime("symbol table construction", System.nanoTime() - symbolTable.creationTime());
        // 2b. Type checking.
        AstTypeChecker typeChecker = new AstTypeChecker(ast);
        reportTime("type checking", System.nanoTime() - typeChecker.creationTime());
        // 2c. Non-type checks (which can assume that type-checking passed).
        AstErrorChecker errorChecker = new AstErrorChecker(ast);
        reportTime("other checking", System.nanoTime() - errorChecker.creationTime());
        // 2d. Simplification.
        if (Talc.debugging('o') == false) {
            AstSimplifier simplifier = new AstSimplifier();
            ast = simplifier.simplify(ast);
            reportTime("simplification", System.nanoTime() - simplifier.creationTime());
        }
        
        // 3. Byte-code generation.
        TalcClassLoader loader = new TalcClassLoader();
        JvmCodeGenerator codeGenerator = new JvmCodeGenerator(loader, ast);
        reportTime("code generation", System.nanoTime() - codeGenerator.creationTime());
        
        // 4. Execution.
        if (Talc.debugging('n')) {
            System.err.println("[talc] (not executing generated code because of -D n.)");
            return;
        }
        long execution0 = System.nanoTime();
        try {
            Class<?> generatedClass = loader.getClass("GeneratedClass");
            generatedClass.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Throwable th) {
            if (th.getCause() != null) {
                th.getCause().printStackTrace();
            } else {
                th.printStackTrace();
            }
        }
        reportTime("execution", System.nanoTime() - execution0);
    }
    
    private void interactiveReadEvaluatePrintLoop() throws IOException {
        LineReader lineReader = new LineReader();
        String line;
        // FIXME: is breaking into lines really useful, when there's no context carried over?
        while ((line = lineReader.readLine("  ")) != null) {
            try {
                if (line.endsWith(";") == false && line.endsWith("}") == false) {
                    // For interactive use, supply a trailing semicolon so the user isn't forced to type it.
                    // FIXME: is this really useful?
                    line += ";";
                }
                parseAndEvaluate(null, new String[0], new Lexer(line));
            } catch (Throwable th) {
                reportError(th);
            }
        }
        System.out.print("\b\b");
    }
    
    private void usage(int exitStatus) {
        System.err.println("usage: talc [--copyright] [-D flags] [--dump-class name] [--dump-classes] [-f file] [-e program]");
        System.err.println("where:");
        for (int i = 0; i < debuggingFlagNames.length; ++i) {
            if (debuggingFlagNames[i] != null) {
                System.err.println("  -D " + ((char) i) + "\t" + debuggingFlagNames[i]);
            }
        }
        System.exit(exitStatus);
    }
    
    private void parseArguments(String[] args) throws IOException {
        String scriptFilename = null;
        ArrayList<String> scriptArgs = new ArrayList<String>();
        boolean inScriptArgs = false;
        boolean didSomethingUseful = false;
        for (int i = 0; i < args.length; ++i) {
            if (inScriptArgs) {
                scriptArgs.add(args[i]);
            } else if (args[i].equals("--copyright")) {
                reportCopyright();
                didSomethingUseful = true;
            } else if (args[i].equals("-h") || args[i].equals("--help")) {
                usage(0);
            } else if (args[i].startsWith("-D")) {
                String flags = args[i].substring(2);
                if (flags.length() == 0) {
                    if (i + 1 >= args.length) {
                        usage(1);
                    }
                    flags = args[++i];
                }
                parseDebuggingFlags(flags);
            } else if (args[i].equals("--dump-class")) {
                String typeName = args[++i];
                TalcType type = TalcType.byName(typeName);
                if (type == null) {
                    die("unknown type \"" + typeName + "\"");
                } else {
                    System.out.print(type.describeClass());
                }
                didSomethingUseful = true;
            } else if (args[i].equals("--dump-classes")) {
                for (TalcType type : TalcType.documentedTypes()) {
                    System.out.println(type.describeClass());
                }
                didSomethingUseful = true;
            } else if (args[i].equals("-e")) {
                String expression = args[++i];
                parseAndEvaluate(null, new String[0], new Lexer(expression));
                didSomethingUseful = true;
            } else if (args[i].equals("--")) {
                inScriptArgs = true;
            } else if (args[i].startsWith("-")) {
                die("unrecognized option \"" + args[i] + "\"");
            } else {
                scriptFilename = args[i];
                inScriptArgs = true;
            }
        }
        if (scriptFilename == null) {
            if (didSomethingUseful) {
                // Fair enough, then.
                System.exit(0);
            } else {
                die("no script filename supplied");
            }
        }
        parseAndEvaluate(scriptFilename, scriptArgs.toArray(new String[scriptArgs.size()]), new Lexer(new File(scriptFilename)));
    }
    
    private static void die(String message) {
        System.err.println("talc: error: " + message);
        System.exit(1);
    }
    
    private static void reportError(Throwable th) {
        if (th instanceof TalcError) {
            System.err.println(th.getMessage());
            System.err.println("Java stack:");
            for (StackTraceElement e : th.getStackTrace()) {
                System.err.println("    " + e);
            }
        } else {
            System.err.println("Unexpected internal error:");
            th.printStackTrace();
        }
    }
    
    private static void reportCopyright() {
        System.out.println("talc - http://code.google.com/p/talc/");
        System.out.println();
        System.out.println("Copyright (C) 2007-2008 Elliott Hughes <enh@jessies.org>.");
        System.out.println();
        System.out.println("Talc uses org.mozilla.classfile, written by Roger Lawrence.");
        System.out.println();
        System.out.println("Talc is free software; you can redistribute it and/or modify");
        System.out.println("it under the terms of the GNU General Public License as published by");
        System.out.println("the Free Software Foundation; either version 3 of the License, or");
        System.out.println("(at your option) any later version.");
        System.out.println();
        System.out.println("Talc is distributed in the hope that it will be useful,");
        System.out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
        System.out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
        System.out.println("GNU General Public License for more details.");
        System.out.println();
        System.out.println("You should have received a copy of the GNU General Public License");
        System.out.println("along with this program.  If not, see <http://www.gnu.org/licenses/>.");
        System.out.println();
        System.out.println("Documentation should be available via \"man talc\" or on the web.");
    }
    
    public static boolean debugging(char ch) {
        if (ch > debuggingFlags.length || debuggingFlagNames[ch] == null) {
            throw new RuntimeException("unknown debugging flag '" + Lexer.ensurePrintable(ch) + "'");
        }
        return debuggingFlags[ch];
    }
    
    private void parseDebuggingFlags(String flags) {
        for (int i = 0; i < flags.length(); ++i) {
            char ch = flags.charAt(i);
            if (debuggingFlagNames[ch] == null) {
                usage(1);
            } else {
                debuggingFlags[ch] = true;
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        Talc talc = new Talc();
        if (args.length == 0) {
            talc.interactiveReadEvaluatePrintLoop();
        } else {
            try {
                talc.parseArguments(args);
            } catch (Throwable th) {
                reportError(th);
            }
        }
    }
}
