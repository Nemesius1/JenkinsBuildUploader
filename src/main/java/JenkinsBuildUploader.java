import com.offbytwo.jenkins.model.Job;

import java.util.List;
import java.util.Scanner;

public class JenkinsBuildUploader {
    public static void main(String[] args) {
        JenkinsManager jenkinsManager = new JenkinsManager("http://build-kh.dm.com/");
        List<Job> jobList =  jenkinsManager.getJobList("KH-Win\\S+Qt-editors");
        Scanner scanner = new Scanner(System.in);
        int jobNumber;
        int jobCount = 0;
        for (Job job : jobList){
            jobCount++;
            System.out.println(jobCount + " - " + job.getName());
        }
        System.out.println("Select build from build list");
        jobNumber = scanner.nextInt();
        scanner.close();
        if (0 < jobNumber && jobList.size() >= jobNumber){
            String currentJobName = jobList.get(jobNumber-1).getName();
            System.out.println("Job: " + currentJobName + " selected");
            jenkinsManager.downloadTargetArtifact(currentJobName);
        } else {
            System.out.println("This is incorrect value");
        }
    }
}
