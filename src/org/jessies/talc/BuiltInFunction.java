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

public abstract class BuiltInFunction extends AstNode.FunctionDefinition {
    // Pretend that built-in functions are user-defined functions with empty bodies, so they take advantage of the normal compile-time checks.
    public BuiltInFunction(String functionName, List<String> formalParameterNames, List<TalcType> formalParameterTypes, TalcType returnType) {
        super(null, functionName, formalParameterNames, formalParameterTypes, returnType, AstNode.Block.EMPTY_BLOCK);
    }
    
    // Step in at invocation time and do what we have to do.
    @Override
    public Value invoke(AstEvaluator evaluator, Value instance, AstNode[] arguments) {
        return invokeBuiltIn(evaluator, instance, arguments);
    }
    
    public abstract Value invokeBuiltIn(AstEvaluator evaluator, Value instance, AstNode[] arguments);
}