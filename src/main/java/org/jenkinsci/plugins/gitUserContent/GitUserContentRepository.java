package org.jenkinsci.plugins.gitUserContent;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.security.Permission;

import jenkins.model.Jenkins;

import org.jenkinsci.main.modules.sshd.SSHD;
import org.jenkinsci.plugins.gitserver.FileBackedHttpGitRepository;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;

/**
 * Exposes Git repository at http://server/jenkins/userContent.git.<br>
 * <p>
 * This implementation differs in permission handling to the forked Github source repository.
 * Now the REPO_PERMISSION is needed for pull, clone and push actions.
 * </p>
 *
 * @author Kohsuke Kawaguchi
 * @author Lukas Kranabetter
 */
@Extension
public class GitUserContentRepository extends FileBackedHttpGitRepository implements RootAction {
    @Inject
    public SSHD sshd;
    
    private static final Permission REPO_PERMISSION = Jenkins.ADMINISTER;

    public GitUserContentRepository() {
        super(new File(Jenkins.getInstance().root, "userContent"));
    }
    
    @Override
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
    	// In case of http basic authentication Git clients do not send the credentials with the first request.
    	if(!Jenkins.getInstance().hasPermission(REPO_PERMISSION))
      {
        // Send unauthorized response to start http basic authentication.
      	rsp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        rsp.setHeader("WWW-Authenticate","Basic realm=\"Jenkins user\"");
        
        return;
      }
    	
    	super.doDynamic(req, rsp);
    }

    @Override
    protected void checkPushPermission() {
        Jenkins.getInstance().checkPermission(REPO_PERMISSION);
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "userContent.git";
    }
}
