import com.google.common.collect.Maps;
import java.util.Map;
    options = getOptionsBuilder(console);
    git("init", "--bare", repoGitDir.toString());
  public OptionsBuilder getOptionsBuilder(
      TestingConsole console) throws IOException {
    return new OptionsBuilder()
        .setConsole(this.console)
        .setOutputRootToTmpDir();
  }

    return GitRepository.newBareRepo(path, getEnv(), /*verbose=*/true);
  }

  private Map<String, String> getEnv() {
    Map<String, String> env = Maps.newHashMap(options.general.getEnvironment());
    env.putAll(getGitEnv());
    return env;
        .matchesNext(MessageType.PROGRESS, "Git Destination: Fetching: file:.* refs/heads/master")
        .matchesNext(MessageType.WARNING,
            "Git Destination: 'refs/heads/master' doesn't exist in 'file://.*")
    thrown.expectMessage("'refs/heads/testPullFromRef' doesn't exist");
    GitRepository localRepo = GitRepository.newRepo(true, localPath, getEnv()).init(