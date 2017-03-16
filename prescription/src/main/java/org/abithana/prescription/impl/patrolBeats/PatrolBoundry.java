package org.abithana.prescription.impl.patrolBeats;

import com.graphhopper.GraphHopper;
import org.abithana.prescription.beans.ClusterFitness;
import org.abithana.prescription.beans.DistanceBean;
import org.abithana.prescriptionBeans.BlockCentroidBean;
import org.abithana.prescriptionBeans.LeaderBean;
import org.abithana.statBeans.CordinateBean;
import org.abithana.statBeans.HistogramBean;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;


/**
 * Created by Thilina on 1/5/2017.
 */
public class PatrolBoundry implements Serializable {

    private final double PI = Math.PI;
    private int fitness=0;

    double minDistanceApart=0.0;
    private int threashold;
    static int toalWork;
    int boundries;

    private List<LeaderBean> leaderList=new ArrayList<>();
    private List<BlockCentroidBean> follwers=new ArrayList<>();
    private HashMap<Long,List<Long>> AllLeaderNeighbours=new HashMap<Long,List<Long>>();
    private HashMap<Long,BlockCentroidBean> follwersMap=new HashMap<>();

    static GraphHopper hopper= Routing.getRoute();
    Routing routing=new Routing();
    Checker checker=new Checker();

    public void findPatrolBoundries(){

        System.out.println("========================================================");
        System.out.println("           AGGREGATING Blocks TOGETHER                  ");
        System.out.println("========================================================");

        IntStream.range(0,leaderList.size()).parallel().forEach(i->{
            List<Long> foll=collectAllNeighbours(leaderList.get(i));
            AllLeaderNeighbours.put(leaderList.get(i).getLeaderBlock(),foll);
        });



        //set threashold as max workload of mone block
        Collections.sort(leaderList, LeaderBean.leaderWorkComparator);
        int MaxWork=leaderList.get(leaderList.size()-1).getLeaderWork();
        if(threashold<MaxWork){
            threashold=MaxWork;
            System.out.println("New Threshold : "+MaxWork);
        }

        for (int i = 0; i < 2; i++) {
            int repeat=0;
            int currntSize=0;
            int prevsize=0;  int chance = 0;
            while(follwers.size()>=1) {
                prevsize = currntSize;

                int j = 0;

                    while (j < boundries) {
                        LeaderBean leaderBean;
                        if (i == 0) {
                            Collections.sort(leaderList, LeaderBean.leaderWorkComparator);
                            leaderBean = leaderList.get(j);

                        } else {
                            int randomcluster = ThreadLocalRandom.current().nextInt(0, boundries);
                            System.out.println(randomcluster);
                            leaderBean = leaderList.get(randomcluster);
                            if (getcalcThreashold() < leaderBean.getLeaderWork()) {
                                System.out.println("Leader cannot grow with " + leaderBean.getLeaderWork());
                                j++;
                                continue;
                            }

                        }
                       // System.out.println(leaderBean.getLeaderBlock() + "  Leader work  " + leaderBean.getLeaderWork());

                        if (getcalcThreashold() < leaderBean.getLeaderWork()) {
                            System.out.println("Leader cannot grow with " + leaderBean.getLeaderWork());
                            j++;
                            continue;
                        }
                        if (addtoCluster(leaderBean) == 1) {
                            j=0;
                            break;
                        }
                        j++;
                    }
                    currntSize = follwers.size();
                    if (currntSize == prevsize) {
                        repeat++;
                    }
                   // System.out.println("follower size " + follwers.size() + "  " + follwersMap.size());
                    //terminate when cluster not grow any larger
                    if (repeat == 10) {
                        break;
                    }
            }
        }

        long seed = System.nanoTime();
        Collections.shuffle(leaderList, new Random(seed));

        for(LeaderBean leaderBean:leaderList){
            System.out.println("================Leader "+leaderBean.getLeaderBlock()+" Total leader work : "+leaderBean.getLeaderWork()+"=================");
            for(long i:leaderBean.getFollowers()){
                System.out.print(i + "  ");
            }
            System.out.println();
        }

    }

    private ClusterFitness calcFitness(LeaderBean leaderBean,long l,int distance,int work){
        List<Long> tempNeighbours=new ArrayList<>();
        tempNeighbours = collectAllNeighbours(leaderBean);
        double compactness=0;
        if(follwersMap.get(l)!=null) {
            tempNeighbours.addAll(getNeighbours(follwersMap.get(l).getBlockID()));

            Set<Long> ClusterSpredSet = new HashSet<>();
            ClusterSpredSet.addAll(tempNeighbours);

            if(leaderBean.getFollowerBeans().size()>10) {
                Collections.sort(leaderBean.getFollowerBeans(), BlockCentroidBean.latComparator);

                double minlat = leaderBean.getFollowerBeans().get(0).getLat();
                double maxlat = leaderBean.getFollowerBeans().get(leaderBean.getFollowerBeans().size() - 1).getLat();

                Collections.sort(leaderBean.getFollowerBeans(), BlockCentroidBean.lonComparator);
                double minlon = leaderBean.getFollowerBeans().get(0).getLon();
                double maxlon = leaderBean.getFollowerBeans().get(leaderBean.getFollowerBeans().size() - 1).getLon();

                double r1 = getMaxHorizontalDistance(maxlat, minlat, maxlon, minlon) / 1000;
                double r2 = getMaxVerticalDistance(maxlat, minlat, maxlon, minlon) / 1000;

                leaderBean.getFollowerBeans().add(follwersMap.get(l));
                Collections.sort(leaderBean.getFollowerBeans(), BlockCentroidBean.latComparator);
                double minlatNew = leaderBean.getFollowerBeans().get(0).getLat();
                double maxlatnew = leaderBean.getFollowerBeans().get(leaderBean.getFollowerBeans().size() - 1).getLat();

                Collections.sort(leaderBean.getFollowerBeans(), BlockCentroidBean.lonComparator);
                double minlonNew = leaderBean.getFollowerBeans().get(0).getLon();
                double maxlonnew = leaderBean.getFollowerBeans().get(leaderBean.getFollowerBeans().size() - 1).getLon();

                double r1new = getMaxHorizontalDistance(maxlatnew, minlatNew, maxlonnew, minlonNew) / 1000;
                double r2new = getMaxVerticalDistance(maxlatnew, minlatNew, maxlonnew, minlonNew) / 1000;

                compactness = 50000 * ((r2new * r1new) - (r1 * r2));
                leaderBean.getFollowerBeans().remove(follwersMap.get(l));
            }

            int G=ClusterSpredSet.size()*100;
            int D=5000/(distance+1);
            int L=(int)compactness;
            //int W=(getcalcThreashold())-(leaderBean.getLeaderWork()+work);
            fitness =G + D - L ;


            ClusterFitness cf = new ClusterFitness(l, fitness);

            return cf;
        }
        return new ClusterFitness(0, -1000000000);
    }

    private int addtoCluster(LeaderBean leaderBean){

        //TODO make a HashMap to store neighbours
        List<Long> neighbours = collectAllNeighbours(leaderBean);

        //remove duplicates
        Set<Long> hs = new HashSet<>();
        hs.addAll(neighbours);
        neighbours.clear();
        neighbours.addAll(hs);

        if(neighbours.size()==0){
            System.out.println("neighbours null");
            return -1;
        }

        ArrayList<ClusterFitness> fitnessArrayList=new ArrayList<>();
        ArrayList<DistanceBean> distanceList=new ArrayList<>();

        for(long l:neighbours){
            if(follwersMap.get(l)==null){
                continue;
            }

            BlockCentroidBean blockCentroidBean=follwersMap.get(l);
            int distance =(int) routing.calc(hopper, blockCentroidBean.getLat(), blockCentroidBean.getLon(), leaderBean.getLat(), leaderBean.getLon())[0];
            distanceList.add(new DistanceBean(l, distance));

        }

        Collections.sort(distanceList, DistanceBean.distanceComparator);

        int i=0;
        while(i<4 && i < distanceList.size())
        {
            long blockId=distanceList.get(i).getBlockId();
            int distance=distanceList.get(i).getDistance();
            int work=follwersMap.get(distanceList.get(i).getBlockId()).getWork();
            //int distance2=follwersMap.get(distanceList.get(i).getBlockId()).getWork()/distance;
            ClusterFitness cf=calcFitness(leaderBean,blockId,distance,work);
            if(cf.getFitness()>-1000000000){
                fitnessArrayList.add(cf);
            }
            i++;
        }


        Collections.sort(fitnessArrayList,ClusterFitness.fitnessComparator);

        if(fitnessArrayList.size()>0) {
            long blockId  = fitnessArrayList.get(0).getBlockId();
            BlockCentroidBean centroidBean = follwersMap.get(blockId);

            if(centroidBean==null){

                for (int j = 0; j < fitnessArrayList.size(); j++) {
                    blockId = fitnessArrayList.get(j).getBlockId();
                    centroidBean = follwersMap.get(blockId);
                    if (centroidBean != null)
                        break;
                }
            }

            if(centroidBean!=null) {

                int distance = (int)calcRoadDistance(leaderBean.getLat(), leaderBean.getLon(), centroidBean.getLat(), centroidBean.getLon())[0];
                leaderBean.addFollower(follwersMap.get(blockId).getBlockID());
                leaderBean.addFollowerBean(follwersMap.get(blockId));
                leaderBean.incrementLeaderWork(follwersMap.get(blockId).getWork() +distance );
                toalWork = toalWork + distance;
                AllLeaderNeighbours.put(leaderBean.getLeaderBlock(), neighbours);
                follwers.remove(follwersMap.remove(blockId));
                follwersMap.remove(blockId);
                return 1;
            }
        }
        return -1;
    }


    public HashMap<Integer,ArrayList<Long>> getBoundryTractids()
    {
        HashMap<Integer,ArrayList<Long>> allset=new HashMap<>();
        try{
            IntStream.range(0,leaderList.size()).parallel().forEach(i->{
                LeaderBean leaderBean=leaderList.get(i);
                ArrayList<Long> tractidList=new ArrayList<>();
                if(leaderBean.getFollowers().size()!=0)
                    tractidList.addAll(leaderBean.getFollowers()) ;
                tractidList.add(leaderBean.getLeaderBlock());
                allset.put(i,tractidList);
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return allset;
    }

    /*
    * Calculate response time for each Patrol beat Generated
    * */
    public HashMap<Integer,Double> evaluateBeatsResposeime(){

        HashMap<Integer,Double> avarageResponseTime=new HashMap<>();
        try{
            IntStream.range(0,leaderList.size()).parallel().forEach(k->{
                LeaderBean leaderBean = leaderList.get(k);
                List<BlockCentroidBean> list = leaderBean.getFollowerBeans();
                list.add(new BlockCentroidBean(leaderBean.getLat(), leaderBean.getLon(), leaderBean.getLeaderBlock(), leaderBean.getLeaderWork()));
                long total911Time = 0;
                int noOfBlocks = list.size();
                for (int i = 0; i < noOfBlocks; i++) {
                    for (int j = 0; j < noOfBlocks; j++) {
                        if (i != j) {
                            total911Time = total911Time + calcRoadDistance(list.get(i).getLat(), list.get(i).getLon(), list.get(j).getLat(), list.get(j).getLon())[1];
                        }
                    }
                }
                double avg911=0;
                if(noOfBlocks==0 || noOfBlocks==1){
                    avg911=total911Time;
                }
                else {
                    avg911 = total911Time / (noOfBlocks * (noOfBlocks - 1));
                }
                System.out.println("neighbours :"+list.size()+" avd responsetime"+avg911/(60*60));
                avarageResponseTime.put(k, avg911/(60*60));
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return avarageResponseTime;

    }
    /*
    *Calculate gini index for eveluate workload distribution
     *  */


    public HashMap<Integer,Integer> evaluateBeatsWorkload(){

        HashMap<Integer,Integer> workloadList=new HashMap<>();
        try{
            for(int i=0;i<leaderList.size();i++){
                workloadList.put(i,leaderList.get(i).getLeaderWork());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return workloadList;
    }

    public double evaluateBeatsWorkloadCoefficint()
    {

        GiniCoefficient g=new GiniCoefficient();
        ArrayList<Long> workload=new ArrayList<>();
        int sum=0;
        int sumDiffsSquared=0;
        try{
            for(LeaderBean lb:leaderList) {
                workload.add((long)lb.getLeaderWork());
            }

            return g.getGiniCoefficient(workload);

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    /*
    *Calculate Compactnes of the police patrol beats
    * ratio between area and smallest circle area
    * Isoperimetric inequality
    */
    public HashMap<Integer,Double> evaluateBeatsCompactness(){

        HashMap<Integer,Double> compactnessMap=new HashMap<>();
        try{

            IntStream.range(0,leaderList.size()).parallel().forEach(k->{
                double comapactness=0;
                double comapactness2=0;
                LeaderBean lb=leaderList.get(k);
                List<BlockCentroidBean> blockList=lb.getFollowerBeans();
                blockList.add(new BlockCentroidBean(lb.getLat(),lb.getLon(),lb.getLeaderBlock()));
                List<Long> blockIdList=lb.getFollowers();
                if(blockIdList.size()<1){
                    comapactness=1;
                    comapactness2=1;
                }
                else{
                    Collections.sort(blockList,BlockCentroidBean.latComparator);
                    double minlat=blockList.get(0).getLat();
                    double maxlat=blockList.get(blockList.size()-1).getLat();

                    Collections.sort(blockList,BlockCentroidBean.lonComparator);
                    double minlon=blockList.get(0).getLon();
                    double maxlon=blockList.get(blockList.size()-1).getLon();

                    double r1=getMaxHorizontalDistance(maxlat,minlat,maxlon,minlon)/1000;
                    double r2=getMaxVerticalDistance(maxlat,minlat,maxlon,minlon)/1000;

                    double perimenter=2*(r1+r2);
                    double effectiveArea=checker.getArea(blockIdList);
                    double squreArea=r1*r2;

                    comapactness=effectiveArea/squreArea;
                    comapactness2=(4*22*effectiveArea)/(7*perimenter*perimenter);
                    if(comapactness>=1){
                        comapactness=0.71;
                    }
                }
                System.out.println("follwers :"+blockIdList.size()+" compactness  "+ comapactness);
                System.out.println("compactness2  "+ comapactness2);
                compactnessMap.put(k,comapactness);
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return compactnessMap;
    }

    /*
    * Check isNeighbour
    * */
    public boolean isNeighbour(LeaderBean leaderBean,long tractID){

        return leaderBean.getFollowers().contains(tractID);
    }
    public long[] calcRoadDistance(Double latFrom, Double  lonFrom, Double latTo, Double lonTo){
        return routing.calc( hopper,latFrom, lonFrom, latTo, lonTo);
    }
    /*
    * COllect all neighbours of current cluster lead by parameter leaderBean
    * */
    public List<Long> collectAllNeighbours(LeaderBean leaderBean){

        List<Long> allNeighbours=new ArrayList<>();

        allNeighbours.addAll(getNeighbours(leaderBean.getLeaderBlock()));

        for(long i:leaderBean.getFollowers()){
            allNeighbours.addAll(getNeighbours(i));
        }
        return allNeighbours;

    }

    public List<Long> getNeighbours(long tractID){

        List<Long> set=new ArrayList<>();
        set=checker.getNeighbours(tractID);
        return set;
    }


    public List<LeaderBean> getLearders(List<BlockCentroidBean> list,int noOfPatrols){

        Collections.sort(list,BlockCentroidBean.latComparator);
        double minLat=list.get(0).getLat();
        double maxLat=list.get(list.size()-1).getLat();

        Collections.sort(list,BlockCentroidBean.lonComparator);
        double minLon=list.get(0).getLon();
        double maxLon=list.get(list.size()-1).getLon();

        Collections.sort(list,BlockCentroidBean.workComparator);

        double d1=  getMaxHorizontalDistance(maxLat,minLat,maxLon,minLon);
        double d2 = getMaxVerticalDistance(maxLat,minLat,maxLon,minLon);

        double recArea = d1 * d2;

        minDistanceApart= Math.sqrt(recArea/(noOfPatrols * PI));

        for(BlockCentroidBean bean:list){
            if(leaderList.size()<noOfPatrols) {
                if (isMinDistanceApart(bean.getLat(), bean.getLon())) {
                    LeaderBean leaderBean=new LeaderBean(bean.getLat(), bean.getLon(),bean.getBlockID(),bean.getWork());
                    leaderList.add(leaderBean);
                }
                else {
                    BlockCentroidBean centroidBean=new BlockCentroidBean(bean.getLat(), bean.getLon(),bean.getBlockID(),bean.getWork());
                    follwers.add(centroidBean);
                    follwersMap.put(centroidBean.getBlockID(),centroidBean);
                }
            }
            else {
                BlockCentroidBean centroidBean=new BlockCentroidBean(bean.getLat(), bean.getLon(),bean.getBlockID(),bean.getWork());
                follwers.add(centroidBean);
                follwersMap.put(centroidBean.getBlockID(),centroidBean);
            }
        }

        System.out.println("=================Initial Leader list SIZE IS =====================");
        System.out.println(leaderList.size());


        while (leaderList.size()<noOfPatrols){
            BlockCentroidBean bean=follwers.remove(0);
            follwersMap.remove(follwers.remove(0).getBlockID());
            LeaderBean lb=new LeaderBean(bean.getLat(),bean.getLon(),bean.getBlockID(),bean.getWork());
            leaderList.add(lb);
        }

       /* *//*Smooth initally selected leadrs work*//*
        int sum=0;
        int sumDiffsSquared=0;
        int dev=0;
        int[] workload=new int[leaderList.size()];
        try{
            for(LeaderBean lb:leaderList) {
                int work=lb.getLeaderWork();
                System.out.println("initial work "+work);
                sum+=work;
            }

            double avg = sum/leaderList.size();

            for(int i=0;i<leaderList.size();i++){
                if(leaderList.get(i).getLeaderWork()>3*avg){
                    leaderList.get(i).incrementLeaderWork((int)(-2*avg));
                    continue;
                }
                else if(leaderList.get(i).getLeaderWork()>2*avg){
                    leaderList.get(i).incrementLeaderWork((int)(-1*avg));
                    continue;
                }
                *//*else if(leaderList.get(i).getLeaderWork()>avg){
                    leaderList.get(i).incrementLeaderWork((int)(-0.5*avg));
                    continue;
                }*//*
                else if(leaderList.get(i).getLeaderWork()<avg/3){
                    leaderList.get(i).incrementLeaderWork((int)(2*avg));
                    continue;
                }
                else if(leaderList.get(i).getLeaderWork()<avg/2){
                    leaderList.get(i).incrementLeaderWork((int)(avg));
                    continue;
                }
                *//*else if(leaderList.get(i).getLeaderWork()<avg){
                    leaderList.get(i).incrementLeaderWork((int)(-0.5*avg));
                    continue;
                }*//*
            }
            for(int i=0;i<leaderList.size();i++){
                System.out.println(leaderList.get(i).getLeaderWork());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
*/
        System.out.println("=================Follwer Map SIZE IS =====================");
        System.out.println(follwersMap.size());

        return leaderList;

    }
    public boolean isMinDistanceApart(double lat,double lon){
        if(leaderList.isEmpty()){
            return true;
        }
        else{
            for(LeaderBean leaderBean:leaderList){
                double distance=distanceInMeters(leaderBean.getLat(),lat,leaderBean.getLon(),lon);
                if(distance<minDistanceApart){
                    return false; //if one point is close by not consider it as a leader
                }
            }
        }
        return true;
    }


    public double getMaxHorizontalDistance(double maxLat,double minLat,double maxLong,double minLong){

        double val1=distanceInMeters(maxLat,maxLat,maxLong,minLong);
        double val2=distanceInMeters(minLat,minLat,maxLong,minLong);
        if(val1>=val2)
            return val1;
        else
            return val2;
    }

    public double getMaxVerticalDistance(double maxLat,double minLat,double maxLong,double minLong){

        double val1=distanceInMeters(maxLat,minLat,maxLong,maxLong);
        double val2=distanceInMeters(maxLat,minLat,minLong,minLong);
        if(val1>=val2)
            return val1;
        else
            return val2;
    }



    private double distanceInMeters(double lat1, double lat2, double lon1,
                                    double lon2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        //return Math.sqrt(distance);
        return distance;
    }

    public void setTotalWork(int Totalwork, int n){
        toalWork=Totalwork;
        System.out.println("ToTal work is "+toalWork);
        threashold=toalWork/n;
        System.out.println("Threshold is "+toalWork/n);
        boundries=n;
    }


    public int getcalcThreashold(){
        if(threashold < toalWork/boundries) {
            threashold = (int)(1.1 * toalWork) / boundries;
            System.out.println("Threshold updated to "+ threashold);

        }
        return threashold;
    }

    public List<CordinateBean> getSeedPoints(){
        List<CordinateBean> list=new ArrayList<>();
        for(LeaderBean l:leaderList){
            list.add(new CordinateBean(l.getLat(),l.getLon()));
        }
        return list;
    }

    public List<Integer> getSeedPointID(){
        List<Integer> list=new ArrayList<>();
        for(int i=0;i<leaderList.size();i++){
            list.add(i);
        }
        return list;
    }

}
