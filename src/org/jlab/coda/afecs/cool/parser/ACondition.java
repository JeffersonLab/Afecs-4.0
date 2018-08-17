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

public class ACondition {
    private String keyWord;
    private ArrayList<String> conditionalOperators = new ArrayList<String>();
    private ArrayList<AStatement> conditionalStatements = new ArrayList<AStatement>();
    private ArrayList<AStatement> actionStatements = new ArrayList<AStatement>();


    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public ArrayList<String> getConditionalOperators() {
        return conditionalOperators;
    }

    public void setConditionalOperators(ArrayList<String> conditionalOperators) {
        this.conditionalOperators = conditionalOperators;
    }

    public ArrayList<AStatement> getConditionalStatements() {
        return conditionalStatements;
    }

    public void setConditionalStatements(ArrayList<AStatement> conditionalStatements) {
        this.conditionalStatements = conditionalStatements;
    }

    public ArrayList<AStatement> getActionStatements() {
        return actionStatements;
    }

    public void setActionStatements(ArrayList<AStatement> actionStatements) {
        this.actionStatements = actionStatements;
    }

    public String toString(){
        StringBuffer sb= new StringBuffer();
        sb.append(keyWord).append("\n");
        AStatement stmt;
        String oper;
        System.out.println("\n......... New ACondition ..... ");
        for(int i=0; i< conditionalStatements.size();i++){
            stmt = conditionalStatements.get(i);
            sb.append(stmt.getLeft()).append(" ").
                    append(stmt.getActionOperator()).append(" ").append(stmt.getRight());
            sb.append("\n");
            if(i<conditionalOperators.size()){
                sb.append(conditionalOperators.get(i)).append("\n");
            }
        }
        for (AStatement actionStatement : actionStatements) {
            stmt = actionStatement;
            sb.append(" -> ").append(stmt.getLeft()).append(" ").
                    append(stmt.getActionOperator()).append(" ").append(stmt.getRight());
            sb.append("\n");
        }
        return sb.toString();
    }
}
