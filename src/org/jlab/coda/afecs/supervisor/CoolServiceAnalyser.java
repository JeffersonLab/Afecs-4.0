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

package org.jlab.coda.afecs.supervisor;

import org.jlab.coda.afecs.agent.AReportingTime;
import org.jlab.coda.afecs.cool.ontology.*;
import org.jlab.coda.afecs.cool.parser.ACondition;
import org.jlab.coda.afecs.cool.parser.AStatement;
import org.jlab.coda.afecs.cool.parser.CCompiler;
import org.jlab.coda.afecs.system.ACodaType;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.ALogger;
import org.jlab.coda.afecs.system.util.AfecsTool;

import java.util.ArrayList;

/**
 * <p>
 * This class provides a collection of utility
 * methods to analyse cool configuration description,
 * including methods analysing cool state machine
 * descriptions.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/11/14 Time: 2:51 PM
 * @version 3.x
 */
public class CoolServiceAnalyser {

    private SupervisorAgent owner;

    // local instance of the logger object
    private ALogger lg = ALogger.getInstance();

    CoolServiceAnalyser(SupervisorAgent owner) {
        this.owner = owner;
    }

    /**
     * <p>
     * Executes the conditional statement
     * described in Cool
     * </p>
     *
     * @param condition {@link ACondition} object
     * @return co integer indicating which of the
     * conditional statement were executed.
     * 1: if, 2: elseif, 3: else, 4: while.
     * -1 indicates COOL language error.
     */
    public int executeCondition(ACondition condition) {
        int co = 0;
        String keyword = condition.getKeyWord();
        switch (keyword) {

            case "if":
                if (isTrueConditionStatement(condition)) {
                    executeActionStatements(condition.getActionStatements());
                    co = 1;
                }
                break;

            case "elseif":
                if (isTrueConditionStatement(condition)) {
                    executeActionStatements(condition.getActionStatements());
                    co = 2;
                }
                break;

            case "else":
                executeActionStatements(condition.getActionStatements());
                co = 3;
                break;

            case "while":
                if (isTrueConditionStatement(condition)) {
                    executeActionStatements(condition.getActionStatements());
                    co = 4;
                }
                break;

            default:
                owner.reportAlarmMsg(owner.me.getSession() +
                                "/" + owner.me.getRunType(),
                        owner.me.getName(),
                        9,
                        AConstants.ERROR,
                        " Syntax error in the cool state machine " +
                                "description. Wrong keyword.");
                owner.dalogMsg(owner.me,
                        9,
                        "ERROR",
                        " Syntax error in the cool state machine " +
                                "description. Wrong keyword.");
                lg.logger.severe(owner.myName +
                        " Syntax error in the cool state machine " +
                        "description. Wrong keyword.");
//                co =  -1;
        }

        return co;
    }

    /**
     * <p>
     * Methods executes action statements of a cool service
     * Supports COOL action statements, such as
     * <ul>
     * <li>move_to</li>
     * <li>do</li>
     * </ul>
     * Supports type based execution, i.e. executes action
     * statement for all described agents of a specific type.
     * The main execution mode however is the priority based
     * execution, where action statement is executed for an
     * agent with a specific priority.
     * </p>
     *
     * @param al arrayList of {@link AStatement} objects
     */
    private void executeActionStatements(ArrayList<AStatement> al) {
        for (AStatement st : al) {

            // Type based statement execution
            if (st.getLeft().contains("type")) {
                ArrayList<String> agents;
                agents = getAgentNamesByCoolType(st.getLeft());

                // Execute those action statements containing
                // known components for the current configuration.
                if (agents != null && !agents.isEmpty()) {
                    sendStatementToAgents(st, agents);
                }

                // Priority based statement execution
            } else if (AfecsTool.isNumber(st.getLeft()) > 0) {
                int p = AfecsTool.isNumber(st.getLeft());
                ArrayList<String> cn = getAgentNamesByPriority(p);
                if (cn != null && cn.size() > 0) {
                    sendStatementToAgents(st, cn);
                }

                // Statement execution directed for a specific agent
            } else {
                if (st.getLeft() != null &&
                        !owner.myComponents.containsKey(st.getLeft())) {
                    owner.reportAlarmMsg(owner.me.getSession() +
                                    "/" + owner.me.getRunType(),
                            owner.me.getName(),
                            9,
                            AConstants.ERROR,
                            " Syntax error in the cool service " +
                                    "description 1. Unknown component.");
                    owner.dalogMsg(owner.me,
                            9,
                            "ERROR",
                            " Syntax error in the cool service " +
                                    "description 1. Unknown component.");
                    lg.logger.severe(owner.myName +
                            " Syntax error in the cool service " +
                            "description 1. Unknown component.");
                } else {
                    if (st.getLeft() != null) {
                        switch (st.getActionOperator()) {
                            case "move_to":
//                                try {
                                    owner.send(st.getLeft(),
                                            AConstants.AgentControlRequestMoveToState,
                                            st.getRight(), 5000);
//                                } catch (AException e) {
//                                    owner.reportAlarmMsg(owner.me.getSession() +
//                                                    "/" + owner.me.getRunType(),
//                                            owner.me.getName(),
//                                            7,
//                                            AConstants.WARN,
//                                            " Lost communication. Please reset and try again.");
//                                }
                                break;
                            case "do":
                                owner.send(st.getLeft(),
                                        AConstants.AgentControlRequestExecuteProcess,
                                        st.getRight());
                                break;
                            default:
                                owner.reportAlarmMsg(owner.me.getSession() +
                                                "/" + owner.me.getRunType(),
                                        owner.me.getName(),
                                        9,
                                        AConstants.ERROR,
                                        " Syntax error in the cool state machine " +
                                                "description. Wrong actionOperator.");
                                owner.dalogMsg(owner.me,
                                        9,
                                        "ERROR",
                                        " Syntax error in the cool state machine " +
                                                "description. Wrong actionOperator.");
                                lg.logger.severe(owner.myName +
                                        " Syntax error in the cool state machine " +
                                        "description. Wrong actionOperator.");
                                break;
                        }
                    } else {

                        // This is the case when the COOL statement left
                        // side is missing and is considered supervisor agent
                        // as the actor( left operator). Not sure if we
                        // are using this ...
                        if (st.getActionOperator().equals("move_to")) {
                            owner.transition(st.getRight());

                        } else if (st.getActionOperator().equals("do")) {
                            owner.pm.executeDoProcess(st.getRight(),
                                    owner.myPlugin,
                                    owner.me);
                        }
                    }
                }
            }
        }
    }

    private void sendStatementToAgents(AStatement st, ArrayList<String> agents) {
        for (String s : agents) {
            switch (st.getActionOperator()) {
                case "move_to":
//                    try {
                        owner.send(s,
                                AConstants.AgentControlRequestMoveToState,
                                st.getRight());
//                    } catch (AException e) {
//                        owner.reportAlarmMsg(owner.me.getSession() +
//                                        "/" + owner.me.getRunType(),
//                                owner.me.getName(),
//                                7,
//                                AConstants.WARN,
//                                " Lost communication. Please reset and try again.");
//                    }
                    break;
                case "do":
                    owner.send(s,
                            AConstants.AgentControlRequestExecuteProcess,
                            st.getRight());
                    break;
                default:
                    owner.reportAlarmMsg(owner.me.getSession() +
                                    "/" + owner.me.getRunType(),
                            owner.me.getName(),
                            9,
                            AConstants.ERROR,
                            " Syntax error in the cool state machine " +
                                    "description. Wrong actionOperator.");
                    owner.dalogMsg(owner.me,
                            9,
                            "ERROR",
                            " Syntax error in the cool state machine " +
                                    "description. Wrong actionOperator.");
                    lg.logger.severe(owner.myName +
                            " Syntax error in the cool state machine " +
                            "description. Wrong actionOperator.");
                    break;
            }
        }
    }

    /**
     * <p>
     * Method checks the statement. First it checks to see if the
     * described component is under this supervisors control. Then
     * it checks to see if the described state is equal to the state
     * recorded in the myComponents hashTable.
     * N.B. the cool statement has the following structure:
     * <br></br>
     * <b>comp_name in_state state_name</b>
     * </p>
     *
     * @param stmt {@link  AStatement} object reference.
     * @return true if COOL statement is valid.
     */
    private boolean isTrueStatement(AStatement stmt) {
        boolean stat = true;
        if (stmt.getActionOperator().equals("in_state")) {
            if (owner.myComponents.containsKey(stmt.getLeft())) {
                stat = owner.myComponents.get(stmt.getLeft()).getState().equals(stmt.getRight());

                // type based sm
            } else if (stmt.getLeft().contains("type")) {
                // Get names of all agents having
                // the required type (type_ROC for e.g.)
                ArrayList<String> n = getAgentNamesByCoolType(stmt.getLeft());
                if (n.isEmpty()) {
                    return false;
                }
                for (String s : n) {
                    if (!owner.myComponents.get(s).getState().equals(stmt.getRight())) stat = false;
                }

                // priority based sm
            } else if (AfecsTool.isNumber(stmt.getLeft()) > 0) {
                int p = AfecsTool.isNumber(stmt.getLeft());
                ArrayList<String> cn = getAgentNamesByPriority(p);
                if (cn != null && cn.size() > 0) {
                    for (String aName : cn) {
                        stat = owner.myComponents.get(aName).getState().equals(stmt.getRight());
                    }
                }
            } else {
                owner.reportAlarmMsg(owner.me.getSession() +
                                "/" + owner.me.getRunType(),
                        owner.me.getName(),
                        9,
                        AConstants.ERROR,
                        " Syntax error in the cool state machine " +
                                "description 1. Unknown component = " +
                                stmt.getLeft());
                owner.dalogMsg(owner.me,
                        9,
                        "ERROR",
                        " Syntax error in the cool state machine " +
                                "description 1. Unknown component = " +
                                stmt.getLeft());
                lg.logger.severe(owner.myName +
                        " Syntax error in the cool state machine " +
                        "description 1. Unknown component = " +
                        stmt.getLeft());
                stat = false;
            }
        } else if (stmt.getActionOperator().equals("not_in_state")) {
            if (owner.myComponents.containsKey(stmt.getLeft())) {
                stat = !owner.myComponents.get(stmt.getLeft()).getState().equals(stmt.getRight());

                // type based sm
            } else if (stmt.getLeft().contains("type")) {

                // Get names of all agents having the
                // required type (type_ROC for e.g.)
                ArrayList<String> n = getAgentNamesByCoolType(stmt.getLeft());
                if (n.isEmpty()) {
                    return false;
                }
                for (String s : n) {
                    if (owner.myComponents.get(s).getState().equals(stmt.getRight())) stat = false;
                }

                // priority based sm
            } else if (AfecsTool.isNumber(stmt.getLeft()) > 0) {
                int p = AfecsTool.isNumber(stmt.getLeft());
                ArrayList<String> cn = getAgentNamesByPriority(p);
                if (cn != null && cn.size() > 0) {
                    for (String aName : cn) {
                        stat = owner.myComponents.get(aName).getState().equals(stmt.getRight());
                    }
                }
            } else {
                owner.reportAlarmMsg(owner.me.getSession() +
                                "/" + owner.me.getRunType(),
                        owner.me.getName(),
                        9,
                        AConstants.ERROR, " Syntax error in the cool state machine " +
                                "description 2. Unknown component.");
                owner.dalogMsg(owner.me,
                        9,
                        "ERROR",
                        " Syntax error in the cool state machine " +
                                "description 2. Unknown component.");
                lg.logger.severe(owner.myName +
                        "Syntax error in the cool state machine " +
                        "description 2. Unknown component.");
                stat = false;
            }
        }
        return stat;
    }

    /**
     * <p>
     * COOL statements can be combined using conditional operators,
     * such as <b>&&</b> and/or <b>||</b>
     * <br>
     * For example:
     * statement_1 && (||) statement_2)
     * </br>
     * This method defines if combined conditional statement is valid.
     * This will checking validity of the COOL complex statement, composed
     * using conditional operator between first two statements, and will
     * check the previous result with the next statement/s if any.
     * </p>
     *
     * @param condition {@link ACondition} object reference
     * @return true if COOL condition is valid.
     */
    private boolean isTrueConditionStatement(ACondition condition) {
        boolean res = true;
        AStatement st, st1, st2;
        if (condition.getConditionalOperators().size() > 0) {
            for (int i = 0; i < condition.getConditionalOperators().size(); i++) {
                if (i == 0) {

                    // Check the truth between first two statements
                    st1 = condition.getConditionalStatements().get(0);
                    st2 = condition.getConditionalStatements().get(1);

                    if (condition.getConditionalOperators().get(i).equals("&&")) {
                        res = isTrueStatement(st1) && isTrueStatement(st2);
                    } else if (condition.getConditionalOperators().get(i).equals("||")) {
                        res = isTrueStatement(st1) || isTrueStatement(st2);
                    } else {
                        owner.reportAlarmMsg(owner.me.getSession() +
                                        "/" + owner.me.getRunType(),
                                owner.me.getName(),
                                9,
                                AConstants.ERROR,
                                " Syntax error in the cool state machine description. " +
                                        "Wrong conditionalOperator.");
                        owner.dalogMsg(owner.me,
                                9,
                                "ERROR",
                                " Syntax error in the cool state machine description. " +
                                        "Wrong conditionalOperator.");
                        lg.logger.severe(owner.myName +
                                " Syntax error in the cool state machine description. " +
                                "Wrong conditionalOperator.");
                        res = false;
                    }
                } else {

                    // Check the truth between the result of the
                    // previous result and the next statement
                    st = condition.getConditionalStatements().get(i + 1);
                    if (condition.getConditionalOperators().get(i).equals("&&")) {
                        res = res && isTrueStatement(st);
                    } else if (condition.getConditionalOperators().get(i).equals("||")) {
                        res = res || isTrueStatement(st);
                    } else {
                        owner.reportAlarmMsg(owner.me.getSession() +
                                        "/" + owner.me.getRunType(),
                                owner.me.getName(),
                                9,
                                AConstants.ERROR,
                                " Syntax error in the cool state machine description. " +
                                        "Wrong conditionalOperator.");
                        owner.dalogMsg(owner.me,
                                9,
                                "ERROR",
                                " Syntax error in the cool state machine description. " +
                                        "Wrong conditionalOperator.");
                        lg.logger.severe(owner.myName +
                                " Syntax error in the cool state machine description. " +
                                "Wrong conditionalOperator.");
                        res = false;
                    }
                }
            }
        } else {

            // One statement only, no conditional operator
            st = condition.getConditionalStatements().get(0);
            res = isTrueStatement(st);
        }
        return res;
    }

    /**
     * Gets all described action statements and for every statement
     * extracts the component and required states. Checks the local
     * myComponents map for consistency (checking to see if the current
     * state is defined). N.B. expected state is described as a text
     * of the package received (this is the result of execution of the
     * process necessary to achieve the required state). If there are
     * more then one process necessary to be executed to achieve the
     * state all must define the same received text.
     * <p>
     * </p>
     *
     * @param condition {@link ACondition} object
     * @return true if state in the myComponents map is defined
     * in the cool action statement.
     */
    public boolean checkStatesDescribed(ACondition condition) {
        boolean stat = true;
        for (AStatement stmt : condition.getActionStatements()) {
            if (owner.myComponents.containsKey(stmt.getLeft())) {
                stat = owner.myComponents.get(stmt.getLeft()).getState().equals(stmt.getRight());
                if (!stat) return false;
            } else if (stmt.getLeft().contains("type")) {
                ArrayList<String> n = getAgentNamesByCoolType(stmt.getLeft());
                for (String s : n) {
                    if (_isTransitionedStatementState(stmt, s) == null)
                        stat = false;
                }
            } else if (AfecsTool.isNumber(stmt.getLeft()) > 0) {
                int p = AfecsTool.isNumber(stmt.getLeft());
                ArrayList<String> n = getAgentNamesByPriority(p);
                for (String s : n) {
                    if (_isTransitionedStatementState(stmt, s) == null)
                        stat = false;
                }
            } else {
                owner.reportAlarmMsg(owner.me.getSession() +
                                "/" + owner.me.getRunType(),
                        owner.me.getName(),
                        9,
                        AConstants.ERROR,
                        " Syntax error in the cool state machine description 3 . " +
                                "No type/priority.");
                owner.dalogMsg(owner.me,
                        9,
                        "ERROR",
                        " Syntax error in the cool state machine description 3 . " +
                                "No type/priority.");
                lg.logger.severe(owner.myName +
                        " Syntax error in the cool state machine description 3 . " +
                        "No type/priority.");
                stat = false;
            }
        }
        return stat;
    }

    /**
     * <p>
     * Checks the local myComponents map to
     * see if the agent transitioned to a
     * state defined by the statement
     * </p>
     *
     * @param stmt  COOL statement object reference
     * @param aName agent/component name
     * @return the name of the component if the current state is described
     * in the COOL statement, otherwise it returns null.
     */
    private String _isTransitionedStatementState(AStatement stmt,
                                                 String aName) {
        ArrayList<String> expectedStateNames;
        // Look all the states of the component
        AComponent com = owner.myComponents.get(aName);
        for (AState st : com.getStates()) {
            if (st.getName().equals(stmt.getRight())) {
                for (AProcess pr : st.getProcesses()) {
                    for (APackage pk : pr.getReceivePackages()) {
                        if (pk.getReceivedText() != null && !pk.getReceivedText().isEmpty()) {
                            expectedStateNames = pk.getReceivedText();
                            if (expectedStateNames.contains(owner.myComponents.get(aName).getState()))
                                return aName;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * <p>
     * Gets all described action statements and for every
     * statement extracts the component and required state.
     * Returns the names of all components that are not
     * transitioned to the required state. For that checks
     * the local myComponents map.
     * </p>
     *
     * @param con COOL condition object reference
     * @return StringBuffer containing comma
     * separated names of components
     */
    public StringBuffer getNotTransitioned(ACondition con) {
        StringBuffer sb = new StringBuffer();
        for (AStatement stmt : con.getActionStatements()) {
            if (!owner.myComponents.containsKey(stmt.getLeft()) &&
                    stmt.getLeft().contains("type")) {
                ArrayList<String> n = getAgentNamesByCoolType(stmt.getLeft());
                for (String s : n) {
                    if (_isTransitionedStatementState(stmt, s) == null)
                        sb.append(s).append(", ");
                }
            } else if (AfecsTool.isNumber(stmt.getLeft()) > 0) {
                int p = AfecsTool.isNumber(stmt.getLeft());
                ArrayList<String> n = getAgentNamesByPriority(p);
                for (String s : n) {
                    if (_isTransitionedStatementState(stmt, s) == null)
                        sb.append(s).append(", ");
                }
            } else {
                owner.reportAlarmMsg(owner.me.getSession() +
                                "/" + owner.me.getRunType(),
                        owner.me.getName(),
                        9,
                        AConstants.ERROR,
                        " Syntax error in the cool state machine description 4 . " +
                                "No type/priority.");
                owner.dalogMsg(owner.me,
                        9,
                        "ERROR",
                        " Syntax error in the cool state machine description 4 . " +
                                "No type/priority.");
                lg.logger.severe(owner.myName +
                        " Syntax error in the cool state machine description 4 . " +
                        "No type/priority.");
            }
        }
        return sb;
    }

    /**
     * <p>
     * Decodes coda state machine state transition
     * service names and returns short state name,
     * assuming that service names include coda
     * state names.
     * <p>
     * </p>
     *
     * @param s coda state machine service name
     * @return coda state name
     */
    public String decodeCodaSMServiceName(String s) {
        String ss = s;
        if (s.contains("Configure")) ss = "Configure";
        else if (s.contains("Download")) ss = "Download";
        else if (s.contains("Prestart")) ss = "Prestart";
        else if (s.contains("Go")) ss = "Go";
        else if (s.contains("End")) ss = "End";
        else if (s.contains("StartRun")) ss = "StartRun";
        return ss;
    }

    /**
     * <p>
     * Return list of agents names that are
     * described to take part in the COOl
     * conditional statement.
     * </p>
     *
     * @param coolType Cool defines type
     *                 COOL types are defined like
     *                 "type_EMU" representing specific
     *                 coda type.
     * @return List of agent names
     */
    public ArrayList<String> getAgentNamesByCoolType(String coolType) {
        ArrayList<String> names = new ArrayList<>();

        String type = coolType.substring(coolType.indexOf("type_") + 5);
        for (AComponent com : owner.myComponents.values()) {
            if (com.getType().equals(type)) {
                if (!names.contains(com.getName())) {
                    names.add(com.getName());
                }
            }
        }
        return names;
    }

    /**
     * <p>
     * Returns agent names based on a priority.
     * </p>
     *
     * @param p priority
     * @return List of agent names
     * having the same priority.
     */
    public ArrayList<String> getAgentNamesByPriority(int p) {
        ArrayList<String> names = new ArrayList<>();
        for (AComponent cp : owner.myComponents.values()) {
            if (cp.getPriority() == p) {
                names.add(cp.getName());
            }
        }
        return names;
    }

    /**
     *
     * @param c AControl object as a result parsing rdf configuration
     * file for the control.
     */
    /**
     * <p>
     * Basic differentiation of the supervisor agent.
     * Records components under the control of this supervisor.
     * Completes basic subscriptions specific for supervisor agents.
     * Compiles and stores described services and their rules/conditions.
     * using {@link org.jlab.coda.afecs.cool.parser.CCompiler}
     * </p>
     *
     * @param c COOL Control ontology object
     */
    public void differentiate(AControl c) {
        owner.haveCoda2Component.set(false);
        // Store locally all registered components for this supervisor
        if (c.getComponents() != null && !c.getComponents().isEmpty()) {
            for (AComponent comp : c.getComponents()) {
                if (!comp.getName().equals(owner.myName)) {
                    if (comp.getCoda2Component().equals(AConstants.seton)) {
                        owner.haveCoda2Component.set(true);
                    }
                    owner.myComponents.put(comp.getName(), comp);
                    owner.myCompReportingTimes.put(comp.getName(), new AReportingTime());
                }
            }
            sortForReporting();
            sortForOutputFile();
        }

        // Subscribe supervisor specific subscriptions
        if (!owner.agentSubscribe()) {
            owner.reportAlarmMsg(owner.me.getSession() + "/" +
                            owner.me.getRunType(),
                    owner.me.getName(),
                    9,
                    AConstants.ERROR,
                    " Failed to complete supervisor specific subscriptions.");
            owner.dalogMsg(owner.me,
                    9,
                    "ERROR",
                    " Failed to complete supervisor specific subscriptions.");
            lg.logger.severe(owner.myName +
                    " Failed to complete supervisor specific subscriptions.");
        }

        // For all registered service compile
        // and get cool specified rule conditions
        if (owner.me.getServices() != null &&
                !owner.me.getServices().isEmpty()) {
            for (AService srv : owner.me.getServices()) {
                if (srv.getStateMachineRule() != null &&
                        srv.getStateMachineRule().getCode() != null) {

                    // Add service to the local map
                    owner.myServices.put(srv.getName(), srv);

                    // Create COOL compiler object
                    CCompiler myC =
                            new CCompiler(srv.getStateMachineRule().getCode());

                    // Add array list to the map of conditions
                    // as a result of the compilation
                    owner.myServiceConditions.put(srv.getName(), myC.compile());
                }
            }
        }
    }

    /**
     * <p>
     * Sorting components for reporting to UIs
     * </p>
     */
    public void sortForReporting() {

        int count;
        owner.sortedComponentList.clear();
        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.USR.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.SMS.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.SLC.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

        // ER sorting
        count = 0;
        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.ER.name())) {
                owner.sortedComponentList.put(c.getName(), c);
                    count++;
            }
        }
        if (count > 1) {
            AComponent nc = new AComponent();
            nc.setName("ER_class");
            nc.setType(ACodaType.ER.name());
            nc.setState("na");
            nc.setObjectType("coda3");
            owner.sortedComponentList.put(nc.getName(), nc);
        }
        // End ER sorting

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.FCS.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

        // PEB sorting
        count = 0;
        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.PEB.name())) {
                owner.sortedComponentList.put(c.getName(), c);
                count++;
            }
        }
        if (count > 1) {
            AComponent nc = new AComponent();
            nc.setName("PEB_class");
            nc.setType(ACodaType.PEB.name());
            nc.setState("na");
            nc.setObjectType("coda3");
            owner.sortedComponentList.put(nc.getName(), nc);
        }
         // End of PEB sorting

        // SEB sorting
        count = 0;
        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.SEB.name())) {
                owner.sortedComponentList.put(c.getName(), c);
                count++;
            }
        }
        if (count > 1) {
            AComponent nc = new AComponent();
            nc.setName("SEB_class");
            nc.setType(ACodaType.SEB.name());
            nc.setState("na");
            nc.setObjectType("coda3");
            owner.sortedComponentList.put(nc.getName(), nc);
        }
        // End of SEB sorting

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.EB.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.CDEB.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.DC.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.ROC.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.GT.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.TS.name())) {
                owner.sortedComponentList.put(c.getName(), c);
            }
        }

    }

    /**
     * <p>
     * Sorting components in the order of
     * being allowed to write an output file
     * </p>
     */
    public void sortForOutputFile() {

        owner.sortedByOutputList.clear();
        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.ER.name())) {
                owner.sortedByOutputList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.SEB.name())) {
                owner.sortedByOutputList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.PEB.name())) {
                owner.sortedByOutputList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.EB.name())) {
                owner.sortedByOutputList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.CDEB.name())) {
                owner.sortedByOutputList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.DC.name())) {
                owner.sortedByOutputList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.USR.name())) {
                owner.sortedByOutputList.put(c.getName(), c);
            }
        }

        for (AComponent c : owner.myComponents.values()) {
            if (c.getType().equals(ACodaType.ROC.name())) {
                owner.sortedByOutputList.put(c.getName(), c);
            }
        }
    }
}
