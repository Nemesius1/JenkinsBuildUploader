import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Artifact;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.apache.http.ConnectionClosedException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class JenkinsManager {
    private static final String ARTIFACT_FILTER = "MyOffice\\S+";
    private JenkinsServer jenkinsServer;
    private Map<String, Job> serverJobMap;
    private URI uri;

    JenkinsManager(String host){
        try {
            uri = new URI(host);
        } catch (URISyntaxException e){
            e.printStackTrace();
            System.exit(-1);
        }
        connectToServer();
        try{
            serverJobMap = jenkinsServer.getJobs();
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void connectToServer(){
        jenkinsServer = new JenkinsServer(uri);
    }

    public List<Job> getJobList(){
        return getJobList("");
    }

    List<Job> getJobList(String regexFilter){
        List<Job> jobList = new ArrayList<>();
        for (Map.Entry<String, Job> element : serverJobMap.entrySet()){
            Job job = element.getValue();
            if (job.getName().matches(regexFilter)){
                jobList.add(job);
            }
        }
        return jobList;
    }

    void downloadTargetArtifact(String jobName){
        downloadTargetArtifact(jobName, 0);
    }

    void downloadTargetArtifact(String jobName, Integer buildNumber) {
        try {
            if (serverJobMap.containsKey(jobName)) {
                JobWithDetails job = jenkinsServer.getJob(jobName).details();
                Integer lastBuildNumber = job.getLastSuccessfulBuild().getNumber();
                if (lastBuildNumber < buildNumber || buildNumber == 0){
                    System.out.println("Build number is changed. Last build number is - " + lastBuildNumber + ";");
                    buildNumber = lastBuildNumber;
                }
                BuildWithDetails build = job.getBuildByNumber(buildNumber).details();
                List<Artifact> artifactList = build.getArtifacts();
                int count = 0;
                for (Artifact artifact : artifactList) {
                    String artifactName = artifact.getFileName();
                    if (artifactName.matches(ARTIFACT_FILTER)){
                        count++;
                        System.out.println("Artifact found by filter "+ARTIFACT_FILTER);
                        try {
                            System.out.println("Start downloading");
                            InputStream inputStream = build.downloadArtifact(artifact);
                            OutputStream outputStream =
                                    new FileOutputStream(new File("./"+artifactName));
                            int read;
                            byte[] bytes = new byte[1024];
                            try {
                                while ((read = inputStream.read(bytes)) != -1) {
                                    outputStream.write(bytes, 0, read);
                                }
                            } catch (ConnectionClosedException e){
                                System.out.println(e.getMessage());
                                System.exit(-1);
                            }
                            System.out.println("Artifact downloaded. Find it at ./" + artifactName);
                        } catch (URISyntaxException e) {
                            System.out.println("Artifact downloading is failed");
                            e.printStackTrace();
                        }
                    }
                }
                if (count == 0){
                    System.out.println("There is no artifact found by filter "+ARTIFACT_FILTER);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
