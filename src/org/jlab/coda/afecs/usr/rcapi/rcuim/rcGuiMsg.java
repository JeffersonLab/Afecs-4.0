package org.jlab.coda.afecs.usr.rcapi.rcuim;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import org.jlab.coda.afecs.usr.rcapi.RcApi;

/**
 * rcgMsg core.
 * External program to send messages to CODA runControl GUI message board.
 *
 * @author gurjyan
 * @since  12.3.2019
 */
public class rcGuiMsg {
    private static final String expid = "expid";
    private static final String session = "session";
    private static final String runType = "runtype";
    private static final String author = "author";
    private static final String message = "message";
    private static final String severity = "severity";

    private static void setArguments(JSAP jsap) {

        FlaggedOption opt1 = new FlaggedOption(expid)
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setShortFlag('e')
                .setLongFlag("expid");
        opt1.setHelp("Afecs platform name");

        FlaggedOption opt2 = new FlaggedOption(session)
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setShortFlag('s')
                .setLongFlag("session");
        opt2.setHelp("RunControl session");

        FlaggedOption opt3 = new FlaggedOption(runType)
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setShortFlag('r')
                .setLongFlag("runtype");
        opt3.setHelp("RunControl configuration");

        FlaggedOption opt4 = new FlaggedOption(author)
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setShortFlag('a')
                .setLongFlag("author");
        opt4.setHelp("Message author");

        FlaggedOption opt5 = new FlaggedOption(message)
                .setStringParser(JSAP.STRING_PARSER)
                .setRequired(true)
                .setShortFlag('m')
                .setLongFlag("message");
        opt5.setHelp("Message content");

        FlaggedOption opt6 = new FlaggedOption(severity)
                .setStringParser(JSAP.INTEGER_PARSER)
                .setRequired(true)
                .setShortFlag('v')
                .setLongFlag("severity");
        opt6.setHelp("Message severity");

        try {
            jsap.registerParameter(opt1);
            jsap.registerParameter(opt2);
            jsap.registerParameter(opt3);
            jsap.registerParameter(opt4);
            jsap.registerParameter(opt5);
            jsap.registerParameter(opt6);
        } catch (JSAPException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void main(String[] args) {

        // check arguments
        JSAP jsap = new JSAP();
        setArguments(jsap);
        JSAPResult config = jsap.parse(args);
        if (!config.success()) {
            System.err.println();
            System.err.println("rcgMsg" + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
        } else {

            // connect to the platform
            RcApi api = new RcApi();
            if (!api.pl_connect(config.getString(expid))) {
                System.out.println("Cannot connect to the Afecs platform name = " + config.getString(expid));
                System.exit(1);
            }

            // send a message
            api.rcGuiMessage(config.getString(session),
                    config.getString(runType),
                    config.getString(author),
                    config.getString(message),
                    config.getInt(severity));

            // disconnect
            api.pl_disconnect();
        }
    }

}
