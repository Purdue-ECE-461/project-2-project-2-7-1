package com.project1;

public class Module implements Comparable<Module>{
    private static int METRICS_COUNT = 5;
    private GithubStats stats;
    private String url = "";
    private double license = 0;
    private double busFactor = 0;
    private double responsiveMaintainer = 0;
    private double correctness = 0;
    private double rampUp = 0;
    private double total = 0;
    private double dependency = 0;

    private double contributorsThreshold;
    private double healthThreshold;
    private double commentRatioThreshold;
    private double participationThreshold;
    private double issueRatioThreshold;
    private double testRatioThreshold;
    private double dependencyRatio;


    public Module(GithubStats _stats){stats = _stats;}

    public void calculateMetrics(){
        if(stats == null) {return;}

        contributorsThreshold = stats.getContributorsNum() > 50 ? 1.00 : 0.01 * stats.getContributorsNum();
        healthThreshold = stats.getRepositoryHealth() > 50 ? 1.00 : 0.00;
        commentRatioThreshold = stats.getCommentRatio() > 0.1 ? 1.00 : 10 * stats.getCommentRatio();

        double avgParticipation = 0;
        if(stats.getParticipation() != null && stats.getParticipation().size() > 0){
            avgParticipation = (double)(stats.getParticipation().stream().reduce(0, Integer::sum)) / (double) stats.getParticipation().size();
        }
        participationThreshold = avgParticipation > 5 ? 1.00 : avgParticipation * 0.2;

        issueRatioThreshold = stats.getIssuesRatio() > 0.4 ? 1.00 : stats.getIssuesRatio() * 2.5;
        testRatioThreshold = stats.getTestRatio() > 0.1 ? 1.00 : stats.getTestRatio() * 10;

        dependencyRatio = stats.getDependencyRatio();

        calculateLicense();
        calculateBusFactor();
        calculateResponsiveMaintainer();
        calculateCorrectness();
        calculateRampUp();
        calculateDependencies();
        calculateTotal();
    }

    private void calculateLicense(){
        if(stats == null) return;
        if(stats.getLicense()){
            license = 1.00;
        }
    }

    private void calculateBusFactor(){
        if(stats == null) return;

        busFactor = (commentRatioThreshold * 0.2) + (contributorsThreshold * 0.6) + (healthThreshold * 0.2);
    }

    public void calculateResponsiveMaintainer(){
        if(stats == null) return;

        responsiveMaintainer = (issueRatioThreshold * 0.4) + (participationThreshold * 0.6);
    }

    private void calculateCorrectness(){
        if(stats == null) return;

        correctness = (0.2 * testRatioThreshold) + (0.8 * issueRatioThreshold) ;
    }

    private void calculateRampUp(){
        if(stats == null) return;

        rampUp = (0.6 * commentRatioThreshold) + (0.2 * healthThreshold) + (0.2 * contributorsThreshold);
    }

    private void calculateDependencies(){
        dependency = dependencyRatio;
    }
    private void calculateTotal(){
        if(stats == null) return;

        total = ((rampUp + busFactor + correctness + responsiveMaintainer + dependency)/METRICS_COUNT)*license;
    }

    @Override
    public int compareTo(Module module) {

        if (this.total > module.total) {
            return 1;
        }
        else if (this.total < module.total) {
            return -1;
        }
        else {
            return 0;
        }
    }

    @Override
    public String toString(){
        return (this.getUrl() + " " + 
                String.format("%.2f", this.getTotal()) + " " + 
                String.format("%.2f", this.getRampUp()) + " " + 
                String.format("%.2f", this.getCorrectness()) + " " + 
                String.format("%.2f", this.getBusFactor()) + " " + 
                String.format("%.2f", this.getResponsiveMaintainer()) + " " + 
                String.format("%.2f", this.getLicense()) + " " +
                String.format("%.2f", this.getDependency()));
    }

    public double getResponsiveMaintainer(){
        return responsiveMaintainer;
    }

    public double getBusFactor() {
        return busFactor;
    }

    public double getCorrectness() {
        return correctness;
    }

    public double getLicense() {
        return license;
    }

    public double getRampUp() {
        return rampUp;
    }

    public double getDependency() { return dependency;}

    public double getTotal() {
        return total;
    }

    public String getUrl() {return url;}

    public void setUrl(String _url){this.url = _url;}
}
