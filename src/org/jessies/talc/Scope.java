/*
 * This file is part of Talc.
 * Copyright (C) 2007 Elliott Hughes <enh@jessies.org>.
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

import java.util.*;

public class Scope {
    private static Scope globalScope;
    
    private Scope parent;
    private HashMap<String, AstNode.FunctionDefinition> functions;
    private HashMap<String, AstNode.VariableDefinition> variables;
    
    public Scope(Scope parent) {
        this.parent = parent;
    }
    
    public void addFunction(AstNode.FunctionDefinition f) {
        if (functions == null) {
            functions = new HashMap<String, AstNode.FunctionDefinition>();
        }
        functions.put(f.functionName(), f);
    }
    
    public void addVariable(AstNode.VariableDefinition v) {
        if (variables == null) {
            variables = new HashMap<String, AstNode.VariableDefinition>();
        }
        AstNode.VariableDefinition oldDefinition = variables.get(v.identifier());
        if (oldDefinition != null) {
            throw new TalcError(v, "redefinition of \"" + v.identifier() + "\"...\n" + oldDefinition.location() + "...previously defined here");
        }
        variables.put(v.identifier(), v);
    }
    
    public UserDefinedClassValue initializeNewInstance(UserDefinedClassValue instance, AstEvaluator evaluator) {
        if (variables != null) {
            for (AstNode.VariableDefinition var : variables.values()) {
                instance.putField(var.identifier(), var.initializer().accept(evaluator));
            }
        }
        return instance;
    }
    
    public AstNode.FunctionDefinition findFunction(String name) {
        if (functions != null) {
            AstNode.FunctionDefinition f = functions.get(name);
            if (f != null) {
                return f;
            }
        }
        if (parent != null) {
            return parent.findFunction(name);
        }
        return null;
    }
    
    public AstNode.VariableDefinition findVariable(String name) {
        if (variables != null) {
            AstNode.VariableDefinition v = variables.get(name);
            if (v != null) {
                return v;
            }
        }
        if (parent != null) {
            return parent.findVariable(name);
        }
        return null;
    }
    
    public String describeScope() {
        StringBuilder result = new StringBuilder();
        if (variables != null) {
            TreeMap<String, AstNode.VariableDefinition> sortedVariables = new TreeMap<String, AstNode.VariableDefinition>(variables);
            for (AstNode.VariableDefinition variableDefinition : sortedVariables.values()) {
                result.append("  ");
                result.append(variableDefinition);
                result.append(";\n");
            }
        }
        if (functions != null) {
            if (variables != null) {
                result.append("\n");
            }
            TreeMap<String, AstNode.FunctionDefinition> sortedFunctions = new TreeMap<String, AstNode.FunctionDefinition>(functions);
            for (AstNode.FunctionDefinition functionDefinition : sortedFunctions.values()) {
                result.append("  ");
                result.append(functionDefinition.signature());
                result.append("\n");
            }
        }
        return result.toString();
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Scope == false) {
            return false;
        }
        Scope other = (Scope) o;
        return (fieldEquals(parent, other.parent) && fieldEquals(functions, other.functions) && fieldEquals(variables, other.variables));
    }
    
    private <T> boolean fieldEquals(T field, T otherField) {
        return (field == null ? otherField == null : field.equals(otherField));
    }
    
    public static Scope globalScope() {
        return globalScope;
    }
    
    public static void initGlobalScope(Value argv0, ListValue args) {
        globalScope = new Scope(null);
        // Global functions.
        globalScope.addFunction(new Functions.backquote());
        globalScope.addFunction(new Functions.Exit());
        globalScope.addFunction(new Functions.Getenv());
        globalScope.addFunction(new Functions.Gets());
        globalScope.addFunction(new Functions.Print());
        globalScope.addFunction(new Functions.Puts());
        globalScope.addFunction(new Functions.shell());
        globalScope.addFunction(new Functions.system());
        // Global constants.
        globalScope.addVariable(new BuiltInConstant("ARGV0", TalcType.STRING, argv0));
        globalScope.addVariable(new BuiltInConstant("ARGS", TalcType.LIST_OF_STRING, args));
        globalScope.addVariable(new BuiltInConstant("FILE_SEPARATOR", TalcType.STRING, new StringValue(java.io.File.separator)));
        globalScope.addVariable(new BuiltInConstant("PATH_SEPARATOR", TalcType.STRING, new StringValue(java.io.File.pathSeparator)));
    }
    
    public static void fillWithGlobalVariables(Variables variables) {
        for (AstNode.VariableDefinition global : globalScope.variables.values()) {
            variables.defineVariable(global.identifier(), ((AstNode.Constant) global.initializer()).constant());
        }
    }
}