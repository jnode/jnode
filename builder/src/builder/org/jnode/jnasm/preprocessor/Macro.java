/**
 * $Id$  
 */
package org.jnode.jnasm.preprocessor;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class Macro {
    private static int localLabelCount = 0;
    private String name;
    private int paramCount;
    private int maxParamCount = -1;
    private String[] defaultValues;
    private String body;
    private String[] localLabels;

    public String getName() {
        return name;
    }

    public int getParamCount() {
        return paramCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }

    public void setMaxParamCount(int maxParamCount) {
        this.maxParamCount = maxParamCount;
    }

    public void setDefaultValues(String[] defaultValues) {
        this.defaultValues = defaultValues;
    }

    public void setLocalLabels(String[] localLabels) {
        this.localLabels = localLabels;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String expand(String[] params){
        //if(paramCount != params.length) return null;
        String exp = body;
        for(int i = 0; i < localLabels.length; i++){
            exp = exp.replaceAll(localLabels[i], "__jnasm_macro_local_label_" + localLabelCount ++);
        }

        for(int i = 0; i < params.length; i++){
            String par = params[i];
            par = (par == null) ? "" : par.trim();
            exp = exp.replaceAll("%"+(i + 1), par);
        }

        if(maxParamCount > params.length){
            if(defaultValues == null){
                for(int i = params.length; i < maxParamCount; i++){
                    exp = exp.replaceAll("%"+(i + 1), "");
                }
            }else{
                for(int i = params.length; i < maxParamCount; i++){
                    if(defaultValues.length > i - params.length){
                        String def = defaultValues[i - params.length];
                        def = (def == null) ? "" : def.trim();
                        exp = exp.replaceAll("%"+(i + 1), def);
                    }else{
                        exp = exp.replaceAll("%"+(i + 1), "");
                    }
                }
            }
        }

        return exp;
    }

    public String toString() {
         return "MACRO " + name + " " + paramCount + "\n" + body + "\n\n";
    }
}
