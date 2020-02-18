/*
 *   Copyright (c) 2017.  Jefferson Lab (JLab). All rights reserved. Permission
 *   to use, copy, modify, and distribute  this software and its documentation for
 *   governmental use, educational, research, and not-for-profit purposes, without
 *   fee and without a signed licensing agreement.
 *
 *   IN NO EVENT SHALL JLAB BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL
 *   INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
 *   OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF JLAB HAS
 *   BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   JLAB SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE. THE CLARA SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY,
 *   PROVIDED HEREUNDER IS PROVIDED "AS IS". JLAB HAS NO OBLIGATION TO PROVIDE
 *   MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 *   This software was developed under the United States Government license.
 *   For more information contact author at gurjyan@jlab.org
 *   Department of Experimental Nuclear Physics, Jefferson Lab.
 */

package org.jlab.coda.afecs.cool.parser;



import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CCompiler {
    private String code;
    private Pattern openB;
    private Pattern closeB;
    private Pattern openP;
    private Pattern closeP;
    private Pattern andorSim;

    private LinkedHashMap<Integer, Integer> Scopes = new LinkedHashMap<>();
    private LinkedHashMap<Integer, Integer> Conditions = new LinkedHashMap<>();
    // local instance of the logger object

    /**
     * Constructor removes all control characters from the cool rule code
     * @param s code of the rule
     */
    public CCompiler(String s){
//        String s4 = "\\&";
//        String s5 = "|";
        openB = Pattern.compile("\\{");
        closeB = Pattern.compile("\\}");
        openP = Pattern.compile("\\(");
        closeP = Pattern.compile("\\)");
        andorSim = Pattern.compile("[&|]+");
        code = s.replaceAll("\\p{Cntrl}", "");
        code = code.trim();
    }

    /**
     * Finds beginning and ending indexes of the conditions and related scopes.
     * Matches opening and closing brackets and records
     * the indexes into the array-lists. Then it finds
     * the matching brackets and records the beginning and
     * the end of the scope into the HashMap. Next it finds
     * the condition for that scopes.
     * N.B. no nested scopes are supported.
     * @param s input string
     * @return status of the execution
     */
    public boolean findConditonScope(String s){
        ArrayList<Integer> openBrackets = new ArrayList<Integer>();
        ArrayList<Integer> closeBrackets = new ArrayList<Integer>();
        // Find open brackets
        Matcher matcher_o = openB.matcher(s);
        while (matcher_o.find()) {
            openBrackets.add(matcher_o.end());
        }
        // Find close brackets
        Matcher matcher_c = closeB.matcher(s);
        while (matcher_c.find()) {
            closeBrackets.add(matcher_c.end());
        }
        if(openBrackets.size()!=closeBrackets.size()){
            System.out.println("Syntax error: unmatched brackets.");
            return false;
        }
        // Find scopes
        for (int c : closeBrackets) {
            int size = 1111111;
            int open = 0;
            int openIndex = 0;
            for (int j = 0; j < openBrackets.size(); j++) {
                int o = openBrackets.get(j);
                if (c - o > 0) {
                    if ((c - o) < size) {
                        size = c - o;
                        open = o;
                        openIndex = j;
                    }
                }
            }
            Scopes.put(open, c);
            openBrackets.remove(openIndex);
        }

        // Find conditional statements
        int i = 0;
        int end = 0;
        for(int st:Scopes.keySet()){
            if(i==0){
                Conditions.put(0,st);
                end = Scopes.get(st);
                i++;
            } else{
                Conditions.put(end,st);
                end = Scopes.get(st);
            }
        }
        if(Scopes.size()!= Conditions.size()){
            System.out.println("Syntax error: scope without a condition \n" +
                    "or condition without a scopes");
            return false;
        }
        return true;
    }

    /**
     * Finds conditional statement limits.
     * @param s condition string to be analyzed
     * @return map of all conditional statents limits or null if syntax error
     */
    public LinkedHashMap<Integer, Integer> findConditionalStatements(String s){

        LinkedHashMap<Integer, Integer> CondStatements = new LinkedHashMap<Integer, Integer>();

        ArrayList<Integer> openParenthesis = new ArrayList<Integer>();
        ArrayList<Integer> closeParenthesis = new ArrayList<Integer>();
        // Find open parentheses
        Matcher matcher_o = openP.matcher(s);
        while (matcher_o.find()) {
            openParenthesis.add(matcher_o.end());
        }
        // Find close parentheses
        Matcher matcher_c = closeP.matcher(s);
        while (matcher_c.find()) {
            closeParenthesis.add(matcher_c.end());
        }
        if(openParenthesis.size()!=closeParenthesis.size()){
            System.out.println("Syntax error! unmatched parenthesis.");
            return null;
        }

        // Find statement limits using pointers for opening and closing parenthesis.
        for(int c:closeParenthesis){
            int size = 1111111;
            int open = 0;
            int openIndex = 0;
            for(int i=0;i<openParenthesis.size();i++){
                int o = openParenthesis.get(i);
                if(c-o>0){
                    if((c-o)<size){
                        size = c-o;
                        open = o;
                        openIndex = i;
                    }
                }
            }
            CondStatements.put(open-1,c+1);
            openParenthesis.remove(openIndex);
        }
        return CondStatements;
    }


    /**
     * Finds AND or OR boolean operators (& |)
     * @param s input string of a condition
     * @return map containing key = index of the operator and value = operator
     */
    public LinkedHashMap<Integer,String> findConditionalOperators(String s){
        LinkedHashMap<Integer,String> aos = new LinkedHashMap<Integer,String>();
        // Find && operators
        Matcher matcher_o = andorSim.matcher(s);
        while (matcher_o.find()) {
            aos.put(matcher_o.end(),matcher_o.group());
        }
        return aos;
    }

    /**
     * get conditional key word of a condition
     *
     * @param s input string of a condition
     * @return conditional keyword ( if, else, while elseif), null if syntax error
     */
    public String getConditionalKeyWord(String s){
        if(s.startsWith("if")){
            return "if";
        } else if(s.startsWith("elseif")){
            return "elseif";
        } else if(s.startsWith("else")){
            return "else";
        } else if(s.startsWith("while")){
            return "while";
        } else {
            System.out.println("Syntax error: ACondition without a conditional operator");
            return null;
        }
    }

    /**
     * Simple check if the coded description ends with the curly bracket
     * @param s string codded using COOL state machine description language
     * @return  true or false
     */
    public boolean checkLastBrase(String s){
        return s.substring(s.lastIndexOf("}")).trim().equals("}");
    }

    /**
     * Parses conditional or action statements
     * @param s statement string
     * @return AStatement object
     */
    public AStatement parseStatement(String s){
        AStatement st = new AStatement();
        StringTokenizer stk = new StringTokenizer(s);
        switch(stk.countTokens()){
            case 2:
                st.setActionOperator(stk.nextToken());
                st.setRight(stk.nextToken());
                break;
            case 3:
                st.setLeft(stk.nextToken());
                st.setActionOperator(stk.nextToken());
                st.setRight(stk.nextToken());
                break;
            default:
                System.out.println("Syntax error: Malformed statement\n statement = "+s);
                return null;
        }
        return st;
    }

    /**
     * Parses string containing mutiple statements, separated by the ;
     * @param s input string
     * @return  ArrayList of AStatement objects
     */
    public ArrayList<AStatement> parseStatements(String s){
        ArrayList<AStatement> al = new ArrayList<AStatement>();
        StringTokenizer stk = new StringTokenizer(s,";");
        while(stk.hasMoreTokens()){
            AStatement stmt = parseStatement(stk.nextToken());
            if(stmt!=null){
                al.add(stmt);
            } else {
                return null;
            }
        }
        return al;
    }

    /**
     * Parses the string codded using COOL state machine description language
     * @return arraylist of ACondition objects
     */
    public ArrayList<ACondition> compile(){
        // check the last brase
        if(!checkLastBrase(code)){
            System.out.println("Sintax error: missing the last brase");
            return null;
        }

        // find pointers for conditions and related scopes
        if(!findConditonScope(code)) {
            return null;
        }


        //Using scope pointers retrieve contents of the scopes
        ArrayList<String> scopes = new ArrayList<String>();
        for(int p:Scopes.keySet()){
            String scope = code.substring(p,Scopes.get(p)-1);
            scopes.add(scope.trim());
        }


        //Using condition pointers retrieve contents of the conditions
        ArrayList<ACondition> conditions = new ArrayList<ACondition>();
        for(int p:Conditions.keySet()){
            ACondition tCond = new ACondition();
            // get the first condition
            String cndstmt = code.substring(p,Conditions.get(p)-1);

            // get conditional key word
            String condkeyw = getConditionalKeyWord(cndstmt.trim());
            if(condkeyw!=null){
                tCond.setKeyWord(condkeyw.trim());
            } else {
                return null;
            }

            // First get the pointers of the conditional statements
            LinkedHashMap<Integer, Integer> pCondStmt = findConditionalStatements(cndstmt);
            // First find the pointers of the operators
            LinkedHashMap<Integer, String> aomap = findConditionalOperators(cndstmt);

            // n.b. number of conditional statements must be +1 more than conditional operators.
            // Conditional statement can have multiple boolean operators. In that case additional
            // pair of parenthesis will be used, making one more "statement".
            // Remember that conditional statement is a string between parenthesis.
            int z = pCondStmt.size()-aomap.size();
            int j=0;
            // find if we need to remove the last conditional statement which is 
            // not actual statement but container statement
            switch(z){
                case 1:
                    j=pCondStmt.size();
                    break;
                case 2:
                    j=pCondStmt.size()-1;
                    break;
                default:
                    System.out.println("Syntax error: Malformed boolean statement");
                    break;
            }

            //Find conditional statements.
            ArrayList<AStatement> condStatements = new ArrayList<AStatement>();
            for(int sp:pCondStmt.keySet()){
                if(j>0){
                    String stmt = cndstmt.substring(sp+1, pCondStmt.get(sp)-2);
                    // parse the conditional statement
                    AStatement tStmt = parseStatement(stmt.trim());
                    if(tStmt!=null){
                        condStatements.add(tStmt);
                    } else {
                        return null;
                    }
                    j--;
                }
            }
            tCond.setConditionalStatements(condStatements);

            // Get conditional operators.
            ArrayList<String> operators = new ArrayList<String>();
            if( aomap!=null && !aomap.isEmpty() ){
                for(int ind:aomap.keySet()){
                    operators.add(aomap.get(ind));
                }
            }
            tCond.setConditionalOperators(operators);

            conditions.add(tCond);
        }

        // Merge conditions and related scopes together
        if(conditions.size()!=scopes.size()){
            System.out.println("Syntax error...");
            return null;
        } else {
            for(int i=0; i<conditions.size();i++){
                ArrayList<AStatement> tStmts = parseStatements(scopes.get(i));
                if(tStmts!=null){
                    conditions.get(i).setActionStatements(tStmts);
                } else {
                    return null;
                }
            }
        }
        return conditions;
    }



    public static void main(String[] args){

        String s = " if " +
                "((EB1 in_state EBState1)&& " +
                "(HVMainFrame not_in_state HVState1)||(HVMainFrame in_state HVState4)){" +
                "EB1 move_to EBState2;" +
                "do externalprocess1;}elseif ( HVMainframe in_state HVState1){" +
                "move_to HVState2;" +
                "} " +
                "if(EMU1 in_state EMUState1) { EMU1 moveToState EMUState2 }";
        CCompiler myC = new CCompiler(s);
        for(ACondition c:myC.compile()){
            System.out.println(" ... DEBUG ... "+c.getConditionalStatements().size()+" "+c.getConditionalOperators().size());
            System.out.println(c);
        }
    }
}
