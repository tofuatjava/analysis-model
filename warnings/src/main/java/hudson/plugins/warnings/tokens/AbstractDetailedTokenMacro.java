package hudson.plugins.warnings.tokens;

import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.tokens.AbstractTokenMacro;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Provides a token that evaluates detailed informations to the plug-in
 * build result.
 *
 * @author Benedikt Spranger
 */
public abstract class AbstractDetailedTokenMacro extends AbstractTokenMacro {

    public AbstractDetailedTokenMacro(String tokenName, Class<? extends ResultAction<? extends BuildResult>>... resultActions) {
        super(tokenName, resultActions);
    }

    private final List<String> modules = new ArrayList<String>();
    private boolean verbose = false;
    private boolean showLow = true;
    private boolean showNormal = true;
    private boolean showHigh = true;
    private String linechar = "-";

    @Parameter
    public int indent = 0;

    @Parameter(alias="modules")
    public void setModules(String module) {
        modules.add(module);
    }

    @Parameter(alias="verbose")
    public void setVerbosity(boolean verbosity) {
        verbose = verbosity;
    }

    @Parameter(alias="low")
    public void setLow(boolean show) {
        showLow = show;
    }

    @Parameter(alias="normal")
    public void setNormal(boolean show) {
        showNormal = show;
    }

    @Parameter(alias="high")
    public void setHigh(boolean show) {
        showHigh = show;
    }

    @Parameter(alias="linechar")
    public void setLineChar(String c) {
        linechar = c.substring(0, 1);
    }

    protected String evalWarnings(final BuildResult result, Collection<FileAnnotation> warnings) {
        String messages = "";

        if (modules.isEmpty())
            modules.add("all");

        for (String module : modules) {
            boolean allWarn = module.equals("all");
            String heading = module + " annotations:";
            String tmp = "";

            for (FileAnnotation annotation : warnings) {
                Priority prio = annotation.getPriority();
                if (prio == Priority.LOW && !showLow)
                    continue;

                if (prio == Priority.NORMAL && !showNormal)
                    continue;

                if (prio == Priority.HIGH && !showHigh)
                    continue;

                if (allWarn || annotation.getType().equals(module)) {
                    if (allWarn && verbose)
                        tmp += annotation.getType() + ": ";
                    tmp += createMessage(annotation);
                }
            }

            if (tmp.length() > 0) {
                String ind = (indent > 0) ? StringUtils.repeat(" ", indent) : "";

                messages += ind;
                messages += heading + "\n";

                messages += ind;
                messages += StringUtils.repeat(linechar, heading.length()) + "\n";

                messages += tmp;
                messages += "\n";
            }
        }

        return messages;
    }

    private String createMessage(FileAnnotation annotation) {
        String ind = (indent > 0) ? StringUtils.repeat(" ", indent) : "";
        String message = ind;

        if (annotation.getPrimaryLineNumber() > 0) {
            message += annotation.getFileName().replaceAll("^.*workspace/", "");
            message += ":" + annotation.getPrimaryLineNumber() + " ";
        }

        message += annotation.getMessage().replace("<br>", "\n" + ind) + "\n";

        String toolTip;
        toolTip = annotation.getToolTip().replace("<br>", "\n");

        if (toolTip != null) {
            toolTip = ind + toolTip.replace("\n", "\n" + ind).trim();
            message += toolTip + "\n";
        }

        return message;
    }
}
