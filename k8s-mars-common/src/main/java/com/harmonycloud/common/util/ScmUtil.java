package com.harmonycloud.common.util;

import com.harmonycloud.common.enumm.RepositoryTypeEnum;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.git.gitexe.command.remoteinfo.GitRemoteInfoCommand;
import org.apache.maven.scm.provider.git.gitexe.command.remoteinfo.GitRemoteInfoConsumer;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * Created by anson on 17/6/2.
 */
public class ScmUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScmUtil.class);

    public static ActionReturnUtil checkCredentials(String repositoryType, String repositoryUrl, String username, String password) {
        if(RepositoryTypeEnum.SVN.getType().equalsIgnoreCase(repositoryType)){
            return checkSvnCredential(repositoryUrl, username, password);
        }else if(RepositoryTypeEnum.GIT.getType().equalsIgnoreCase(repositoryType)){
            return checkGitCredential(repositoryUrl, username, password);
        }else{
            return ActionReturnUtil.returnError();
        }
    }

    static ActionReturnUtil checkSvnCredential(String repositoryUrl, String username, String password) {
        SVNRepository repository;
        try {
            repository = SVNRepositoryFactoryImpl
                    .create(SVNURL.parseURIEncoded(repositoryUrl));
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username,
                    password.toCharArray());
            repository.setAuthenticationManager(authManager);
            repository.getRepositoryRoot(true);
            repository.getRepositoryUUID(true);
            SVNNodeKind nodeKind = repository.checkPath("", -1);
            if (nodeKind != SVNNodeKind.DIR) {
                return ActionReturnUtil.returnErrorWithMap("message","Credentials looks fine but the repository URL is invalid");
            }
        } catch (SVNException e) {
            return ActionReturnUtil.returnErrorWithMap("message","Unable to access to repository");
        }
        return ActionReturnUtil.returnSuccess();
    }

    static ActionReturnUtil checkGitCredential(String repositoryUrl, String username, String password) {
        try {
            GitScmProviderRepository repository = null;
            repository = new GitScmProviderRepository(repositoryUrl, username, password);
            Commandline commandline= GitRemoteInfoCommand.createCommandLine(repository);
            GitRemoteInfoConsumer consumer=new GitRemoteInfoConsumer(new DefaultLog(),commandline.toString());
            CommandLineUtils.StringStreamConsumer systemErr =new CommandLineUtils.StringStreamConsumer();
            int exitCode = CommandLineUtils.executeCommandLine(commandline, null, consumer, systemErr, 3);
            if(exitCode == 0){
                return ActionReturnUtil.returnSuccess();
            }else{
                return ActionReturnUtil.returnErrorWithMap("message", systemErr.getOutput());
            }
        } catch (ScmException e) {
            LOGGER.warn("GitCredential验证失败", e);
        } catch (CommandLineException e) {
            LOGGER.warn("GitCredential验证失败", e);
        }
        return ActionReturnUtil.returnError();
    }
}
