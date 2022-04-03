package service;

import models.database.Job;
import repository.JobsRepository;

import java.util.List;

public class JobsService {

    private JobsRepository jobsRepository = new JobsRepository();

    public void removeJobByFunction(String functionName) {
        jobsRepository.removeByFunction(functionName);
    }

    public List<Job> findJobByFunction(String functionName) {
        return jobsRepository.findJobsByFunction(functionName);
    }
}
