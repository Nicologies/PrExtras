package com.nicologies.prextras;

import jetbrains.buildServer.util.StringUtil;
import org.eclipse.egit.github.core.RepositoryId;

import java.security.InvalidParameterException;

public class RepoInfoParser {
    public static RepositoryId Parse(String repoUrl){
        String owner = "";
        String repoName = "";
        if (StringUtil.isEmptyOrSpaces(repoUrl)) {
            throw new InvalidParameterException("Empty repo url");
        }
        if (repoUrl.toLowerCase().startsWith("http")) {
            String[] split = repoUrl.split("/"); // http://github.com/owner/repo
            owner = split[3];
            repoName = split[4].replace(".git", "");
        } else if (repoUrl.toLowerCase().startsWith("git")) {
            //git@github.com:owner/repo
            String[] split = repoUrl.split("/");
            owner= split[0].replace("git@github.com:", "");
            repoName = split[1].replace(".git", "");
        }

        if (StringUtil.isEmptyOrSpaces(owner)){
            throw new InvalidParameterException("unable to get owner from url " + repoUrl);
        }

        if(StringUtil.isEmptyOrSpaces(repoName)){
            throw new InvalidParameterException("unable to get repo name from url" + repoUrl);
        }
        return new RepositoryId(owner, repoName);
    }
}
