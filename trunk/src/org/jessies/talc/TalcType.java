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

import java.util.*;

public class TalcType {
    private static final HashMap<String, TalcType> documentedTypes = new HashMap<String, TalcType>();
    
    // The primordial type.
    public static final TalcType OBJECT = new TalcType(null, "object");
    
    // Built-in simple types.
    public static final TalcType BOOL = new TalcType(OBJECT, "bool");
    public static final TalcType FILE = new TalcType(OBJECT, "file");
    public static final TalcType INT = new TalcType(OBJECT, "int");
    public static final TalcType MATCH = new TalcType(OBJECT, "match");
    public static final TalcType REAL = new TalcType(OBJECT, "real");
    public static final TalcType STRING = new TalcType(OBJECT, "string");
    public static final TalcType VOID = new TalcType(null, "void");
    
    // Type parameters.
    public static final TalcType K = makeTypeVariable("K");
    public static final TalcType V = makeTypeVariable("V");
    public static final TalcType T = makeTypeVariable("T");
    
    // Built-in parameterized types.
    public static final TalcType LIST_OF_T = new TalcType(OBJECT, "list", T, null);
    public static final TalcType MAP_OF_K_V = new TalcType(OBJECT, "map", K, V);
    
    // Commonly-used list types in the built-in library.
    public static final TalcType LIST_OF_OBJECT = instantiateType(LIST_OF_T, OBJECT, null);
    public static final TalcType LIST_OF_STRING = instantiateType(LIST_OF_T, STRING, null);
    public static final TalcType LIST_OF_K;
    public static final TalcType LIST_OF_V;
    
    // The type of "null".
    public static final TalcType NULL = new TalcType(null, "null-type");
    
    // The special type of list literals. The type checker will find a suitable more specific type.
    public static final TalcType LIST_OF_NOTHING = new TalcType(null, "empty-list");
    
    static {
        addMemberFunction(OBJECT, new Functions.Object_to_s());
        addClass(OBJECT);
        
        addClass(BOOL);
        
        addMemberFunction(FILE, new Functions.File_append());
        addMemberFunction(FILE, new Functions.File_exists());
        addMemberFunction(FILE, new Functions.File_file());
        addMemberFunction(FILE, new Functions.File_is_directory());
        addMemberFunction(FILE, new Functions.File_is_executable());
        addMemberFunction(FILE, new Functions.File_mkdir());
        addMemberFunction(FILE, new Functions.File_mkdir_p());
        addMemberFunction(FILE, new Functions.File_read());
        addMemberFunction(FILE, new Functions.File_read_lines());
        addMemberFunction(FILE, new Functions.File_realpath());
        addMemberFunction(FILE, new Functions.File_write());
        addClass(FILE);
        
        addMemberFunction(INT, new Functions.Int_abs());
        addMemberFunction(INT, new Functions.Int_signum());
        addMemberFunction(INT, new Functions.Int_to_base());
        addMemberFunction(INT, new Functions.Int_to_char());
        addMemberFunction(INT, new Functions.Numeric_to_i());
        addMemberFunction(INT, new Functions.Numeric_to_r());
        addClass(INT);
        
        addMemberFunction(LIST_OF_T, new Functions.List___get_item__());
        addMemberFunction(LIST_OF_T, new Functions.List___set_item__());
        addMemberFunction(LIST_OF_T, new Functions.List_add_all());
        addMemberFunction(LIST_OF_T, new Functions.List_clear());
        addMemberFunction(LIST_OF_T, new Functions.List_contains());
        addMemberFunction(LIST_OF_T, new Functions.List_is_empty());
        addMemberFunction(LIST_OF_T, new Functions.List_join());
        addMemberFunction(LIST_OF_T, new Functions.List_length());
        addMemberFunction(LIST_OF_T, new Functions.List_list());
        addMemberFunction(LIST_OF_T, new Functions.List_peek_back());
        addMemberFunction(LIST_OF_T, new Functions.List_peek_front());
        addMemberFunction(LIST_OF_T, new Functions.List_pop_back());
        addMemberFunction(LIST_OF_T, new Functions.List_pop_front());
        addMemberFunction(LIST_OF_T, new Functions.List_push_back());
        addMemberFunction(LIST_OF_T, new Functions.List_push_front());
        addMemberFunction(LIST_OF_T, new Functions.List_remove_all());
        addMemberFunction(LIST_OF_T, new Functions.List_remove_at());
        addMemberFunction(LIST_OF_T, new Functions.List_remove_first());
        addMemberFunction(LIST_OF_T, new Functions.List_reverse());
        addMemberFunction(LIST_OF_T, new Functions.List_sort());
        addMemberFunction(LIST_OF_T, new Functions.List_to_s());
        addMemberFunction(LIST_OF_T, new Functions.List_uniq());
        LIST_OF_K = LIST_OF_T.duplicateWithDifferentKeyType(K);
        LIST_OF_V = LIST_OF_T.duplicateWithDifferentKeyType(V);
        addClass(LIST_OF_T);
        
        addMemberFunction(MAP_OF_K_V, new Functions.Map___get_item__());
        addMemberFunction(MAP_OF_K_V, new Functions.Map___set_item__());
        addMemberFunction(MAP_OF_K_V, new Functions.Map_clear());
        addMemberFunction(MAP_OF_K_V, new Functions.Map_has_key());
        addMemberFunction(MAP_OF_K_V, new Functions.Map_has_value());
        addMemberFunction(MAP_OF_K_V, new Functions.Map_keys());
        addMemberFunction(MAP_OF_K_V, new Functions.Map_length());
        addMemberFunction(MAP_OF_K_V, new Functions.Map_map());
        addMemberFunction(MAP_OF_K_V, new Functions.Map_remove());
        addMemberFunction(MAP_OF_K_V, new Functions.Map_values());
        addClass(MAP_OF_K_V);
        
        addMemberFunction(MATCH, new Functions.Match_group());
        addClass(MATCH);
        
        addMemberFunction(REAL, new Functions.Real_abs());
        addMemberFunction(REAL, new Functions.Real_log());
        addMemberFunction(REAL, new Functions.Real_log10());
        addMemberFunction(REAL, new Functions.Real_logE());
        addMemberFunction(REAL, new Functions.Real_signum());
        addMemberFunction(REAL, new Functions.Real_sqrt());
        addMemberFunction(REAL, new Functions.Numeric_to_i());
        addMemberFunction(REAL, new Functions.Numeric_to_r());
        addClass(REAL);
        
        addMemberFunction(STRING, new Functions.String_contains());
        addMemberFunction(STRING, new Functions.String_ends_with());
        addMemberFunction(STRING, new Functions.String_escape_html());
        addMemberFunction(STRING, new Functions.String_gsub());
        addMemberFunction(STRING, new Functions.String_lc());
        addMemberFunction(STRING, new Functions.String_lc_first());
        addMemberFunction(STRING, new Functions.String_length());
        addMemberFunction(STRING, new Functions.String_match());
        addMemberFunction(STRING, new Functions.String_replace());
        addMemberFunction(STRING, new Functions.String_split());
        addMemberFunction(STRING, new Functions.String_starts_with());
        addMemberFunction(STRING, new Functions.String_sub());
        addMemberFunction(STRING, new Functions.String_to_i());
        addMemberFunction(STRING, new Functions.String_to_r());
        addMemberFunction(STRING, new Functions.String_trim());
        addMemberFunction(STRING, new Functions.String_uc());
        addMemberFunction(STRING, new Functions.String_uc_first());
        addClass(STRING);
    }
    
    // Cached hash code.
    private volatile int hashCode = 0;
    
    private TalcType superclass;
    private String name;
    private TalcType uninstantiatedParametricType;
    private TalcType keyType;
    private TalcType valueType;
    private Scope members;
    private boolean isTypeVariable;
    
    private TalcType() {
    }
    
    // For simple (non-parametric) types.
    private TalcType(TalcType superclass, String name) {
        this.superclass = superclass;
        this.name = name;
        this.members = new Scope(superclass != null ? superclass.members : null);
    }
    
    // For uninstantiated parametric types.
    private TalcType(TalcType superclass, String name, TalcType keyType, TalcType valueType) {
        this.superclass = superclass;
        this.name = name;
        this.members = new Scope(superclass != null ? superclass.members : null);
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    // For instantiated parametric types.
    private TalcType(TalcType superclass, TalcType uninstantiatedParametricType, TalcType keyType, TalcType valueType) {
        this.superclass = superclass;
        this.members = uninstantiatedParametricType.members;
        this.uninstantiatedParametricType = uninstantiatedParametricType;
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    // For type variables (currently just K, V, and T).
    private static TalcType makeTypeVariable(String name) {
        TalcType result = new TalcType();
        result.name = name;
        result.isTypeVariable = true;
        return result;
    }
    
    // For turning list<T> into list<K> or list<V>.
    private TalcType duplicateWithDifferentKeyType(TalcType newKeyType) {
        TalcType result = new TalcType();
        result.isTypeVariable = isTypeVariable;
        result.keyType = newKeyType;
        result.members = members;
        result.name = name;
        result.superclass = superclass;
        result.uninstantiatedParametricType = uninstantiatedParametricType;
        result.valueType = valueType;
        return result;
    }
    
    public static TalcType makeUserDefinedClass(TalcType superclass, String name) {
        return new TalcType(superclass, name);
    }
    
    public static void addClass(TalcType t) {
        documentedTypes.put(t.name, t);
    }
    
    private static void addMemberFunction(TalcType type, AstNode.FunctionDefinition f) {
        f.fixUpTypes(type);
        type.members().addFunction(f);
    }
    
    public static TalcType byName(String name) {
        return documentedTypes.get(name);
    }
    
    public static Collection<TalcType> documentedTypes() {
        return new TreeMap<String, TalcType>(documentedTypes).values();
    }
    
    public static TalcType instantiateType(TalcType parametricType, TalcType keyType, TalcType valueType) {
        if (parametricType.uninstantiatedParametricType != null && (parametricType.keyType.isTypeVariable == false && parametricType.valueType.isTypeVariable == false)) {
            throw new RuntimeException("instantiateType(" + parametricType + ", " + keyType + ", " + valueType + "): alleged uninstantiated parametric type isn't!");
        }
        TalcType newTypeVariable1 = selectTypeVariable(parametricType.keyType, keyType, valueType);
        TalcType newTypeVariable2 = selectTypeVariable(parametricType.valueType, keyType, valueType);
        return new TalcType(parametricType.superclass(), parametricType, newTypeVariable1, newTypeVariable2);
    }
    
    private static TalcType selectTypeVariable(TalcType typeVariable, TalcType keyType, TalcType valueType) {
        TalcType result = null;
        if (typeVariable == T || typeVariable == K) {
            result = keyType;
        } else if (typeVariable == V) {
            result = valueType;
        } else if (typeVariable != null) {
            throw new RuntimeException("unknown type variable " + typeVariable + "!");
        }
        return result;
    }
    
    // Is this an instantiated parametric type?
    public boolean isInstantiatedParametricType() {
        return (uninstantiatedParametricType != null);
    }
    
    // Is this an uninstantiated parametric type?
    public boolean isUninstantiatedParametricType() {
        return isTypeVariable || (keyType != null && keyType.isUninstantiatedParametricType()) || (valueType != null && valueType.isUninstantiatedParametricType());
    }
    
    // Returns the uninstantiated parametric type for this instantiated parametric type.
    public TalcType uninstantiatedParametricType() {
        return uninstantiatedParametricType;
    }
    
    public TalcType superclass() {
        return superclass;
    }
    
    // Distance from root of class hierarchy.
    // object's depth is 0.
    public int depth() {
        int result = 0;
        for (TalcType type = this; type.superclass != null; type = type.superclass) {
            ++result;
        }
        return result;
    }
    
    // Returns the instantiated type parameter for this type, corresponding to the given type-variable TalcType.
    // That is:
    //   list<string>.typeParameter(T) == string
    //   map<string, color>.typeParameter(V) == color
    public TalcType typeParameter(TalcType typeVariable) {
        if (typeVariable.isTypeVariable == false) {
            throw new RuntimeException("alleged type variable " + typeVariable + " isn't a type variable");
        }
        if (typeVariable == T || typeVariable == K) {
            return keyType;
        } else if (typeVariable == V) {
            return valueType;
        } else {
            return null;
        }
    }
    
    public boolean canBeAssignedTo(TalcType declaredType) {
        // A "null" value can be assigned to any type.
        // All types' values can be assigned to the same type.
        if (this == NULL || this.equals(declaredType)) {
            return true;
        }
        
        // A type can be assigned to any of its superclasses.
        for (TalcType t = superclass; t != null; t = t.superclass) {
            if (t.canBeAssignedTo(declaredType)) {
                return true;
            }
        }
        
        // For a parametric type, both types must be parametric, their uninstantiated types must be the same, and the matching instantiated type parameters must be assignable.
        if (isInstantiatedParametricType()) {
            return declaredType.uninstantiatedParametricType() != null && uninstantiatedParametricType().equals(declaredType.uninstantiatedParametricType()) && (keyType== null || keyType.canBeAssignedTo(declaredType.keyType)) && (valueType == null || valueType.canBeAssignedTo(declaredType.valueType));
        }
        
        // The LIST_OF_NOTHING type, used for empty list literals, can be assigned to any list type.
        if (this == LIST_OF_NOTHING && declaredType.isInstantiatedParametricType() && declaredType.uninstantiatedParametricType().equals(LIST_OF_T)) {
            return true;
        }
        
        return false;
    }
    
    public Scope members() {
        return members;
    }
    
    public Value newInstance(AstEvaluator evaluator) {
        return members().initializeNewInstance(new UserDefinedClassValue(this), evaluator);
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TalcType == false) {
            return false;
        }
        TalcType other = (TalcType) o;
        if (isTypeVariable && other.isTypeVariable) {
            return true;
        }
        if (fieldEquals(superclass, other.superclass) == false) {
            return false;
        }
        if (fieldEquals(name, other.name) == false) {
            return false;
        }
        if (fieldEquals(uninstantiatedParametricType, other.uninstantiatedParametricType) == false) {
            return false;
        }
        if (fieldEquals(keyType, other.keyType) == false) {
            return false;
        }
        if (fieldEquals(valueType, other.valueType) == false) {
            return false;
        }
        if (fieldEquals(members, other.members) == false) {
            return false;
        }
        return true;
    }
    
    private <T> boolean fieldEquals(T field, T otherField) {
        return (field == null ? otherField == null : field.equals(otherField));
    }
    
    public int hashCode() {
        if (hashCode == 0) {
            int result = 17;
            result = 37*result + (superclass == null ? 0 : superclass.hashCode());
            result = 37*result + (name == null ? 0 : name.hashCode());
            result = 37*result + (uninstantiatedParametricType == null ? 0 : uninstantiatedParametricType.hashCode());
            result = 37*result + (keyType == null ? 0 : keyType.hashCode());
            result = 37*result + (valueType == null ? 0 : valueType.hashCode());
            result = 37*result + (members == null ? 0 : members.hashCode());
            hashCode = result;
        }
        return hashCode;
    }
    
    public String describeClass() {
        StringBuilder result = new StringBuilder();
        result.append(this);
        if (superclass != null) {
            result.append(" : ");
            result.append(superclass);
        }
        result.append("\n");
        if (members != null) {
            result.append(members.describeScope());
        }
        return result.toString();
    }
    
    public String describe() {
        return "TalcType[name=" + name + ",keyType=" + keyType + ",valueType=" + valueType + ",superclass=" + superclass + ",unPT=" + uninstantiatedParametricType + ",isTypeVariable=" + isTypeVariable + ",members=" + members + "]";
    }
    
    // Used in JvmCodeGenerator to recognize "list" and translate it to ListValue.
    // FIXME: long-term, we'll need a better way to do that, and this method can probably disappear.
    public String rawName() {
        if (name != null) {
            return name;
        } else if (uninstantiatedParametricType != null) {
            return uninstantiatedParametricType.name;
        } else {
            throw new RuntimeException("type " + this + " doesn't have a raw name");
        }
    }
    
    public String toString() {
        StringBuilder result = new StringBuilder();
        String basicName = name;
        if (name == null && uninstantiatedParametricType != null) {
            basicName = uninstantiatedParametricType.name;
        }
        if (basicName == null) {
            result.append(describe());
        }
        result.append(basicName);
        if (keyType != null) {
            result.append("<");
            result.append(keyType);
            if (valueType != null) {
                result.append(",");
                result.append(valueType);
            }
            result.append(">");
        }
        return result.toString();
    }
}