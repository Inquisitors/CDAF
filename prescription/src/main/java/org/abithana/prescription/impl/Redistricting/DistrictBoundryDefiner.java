package org.abithana.prescription.impl.Redistricting;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import java.io.Serializable;
import java.util.*;


/**
 * Created by malakaganga on 1/3/17.
 */


public class DistrictBoundryDefiner implements Serializable {

    private long k = 10;
    private final double pi = Math.PI;
    private long populationTotal = 805704;
    private long x;

    private ArrayList<CensusTract> censusTracts = new ArrayList<CensusTract>(); // to hold census tracts
    private HashMap<Long, CensusTract> censusMap = new HashMap<Long, CensusTract>();
    private HashMap<Long, CensusTract> addedToClustersTracts = new HashMap<Long, CensusTract>();

    private ArrayList<Cluster> clusterCol = new ArrayList<Cluster>();
    private HashMap<Long, Cluster> clusters = new HashMap<Long, Cluster>();


    private HashMap<Long, Cluster> finishedClusters = new HashMap<Long, Cluster>();


    private HashMap<Long, Cluster> seedClusters = new HashMap<Long, Cluster>();


    public DistrictBoundryDefiner(long numberOfDistricts, long populationTotal) {
        this.k = numberOfDistricts;
        this.populationTotal = populationTotal;
        this.x = populationTotal / k;
    }
    public HashMap<Long, Cluster> getFinishedClusters() {
        return finishedClusters;
    }


    public HashMap<Long, Cluster> getSeedClusters() {
        return seedClusters;
    }

    public void setSeedClusters(HashMap<Long, Cluster> seedClusters) {
        this.seedClusters = seedClusters;
    }

    public static void main(String[] args) {
        DistrictBoundryDefiner dbd = new DistrictBoundryDefiner(10, 802355);
        dbd.redrawingDistrictBoundry();
        HashMap<Long, Cluster> map = dbd.getFinishedClusters();
        int count = 0;
        for (Long id : map.keySet()) {
            System.out.println("\n Cluster id = " + id + " X =" + map.get(id).getX() + " population = " + map.get(id)
                    .getPopulation() + " \n");
            Cluster cl = map.get(id);

            System.out.println("Area " + cl.getArea() + " Area of Mul " + cl.perimeterOfclus() + " \n");
            System.out.println("\nMin Lat =" + cl.getMinLatitude() + " Min Long = " + cl.getMinLongitude() + " Max " +
                    "Lat = " + cl.getMaxLatitude() + " Max Long = " + cl.getMaxLongitude() + "\n **** Iso coeffient"
                    + cl
                    .isoperimetricQuotient() + "*****\n");

            for (Long ids : cl.getCensusIds()) {
                count++;
            }

        }
        System.out.println("\n\n\n\n " + count + "\n\n\nGinicoefficient = " + dbd.getGiniCoefficient());

       /* HashMap<Long, Cluster> seedClusters = dbd.getSeedClusters();
        for (Cluster clu : seedClusters.values()) {
            System.out.println("\n Cluster id" + clu.getClusterId() + " Size of census tracts =" + clu.getCensusIds()
                    .size() + " ");
            System.out.println("\n Census id =" + (clu.getCensusIds()).iterator().next() + " Pereimeter = " + clu
                    .perimeterOfclus());
        }*/
    }

    public HashMap<Long, HashSet<Long>> getTractsOfDistricts() {
        HashMap<Long, Cluster> map = redrawingDistrictBoundry();
        HashMap<Long, HashSet<Long>> mapWithId = new HashMap<Long, HashSet<Long>>();
        for (Long id : map.keySet()) {
            mapWithId.put(id, map.get(id).getCensusIds());
        }
        return mapWithId;
    }

    /*
    *  Run Algo
    **/

    public HashMap<Long, Cluster> redrawingDistrictBoundry() {

    /*
    * Extract data from csv files and  create census objects
    * */
        extractDataFromBoundry();
        extractDataFromPopulation();
        extractDataFromNeighBour();


        selectFirstK();

        while (true) {
            if (clusters.isEmpty()) {
                break;
            }
            Cluster cluster = bestClusterToGrow();


            boolean isNotFinished = growingTheBestCluster(cluster);

            if (!isNotFinished) {
                clusterCol.remove(cluster);
                clusters.remove(cluster.getClusterId());
                finishedClusters.put(cluster.getClusterId(), cluster);
            }
        }

       /* for (Cluster cl : finishedClusters.values()) {
            for (long id : cl.getCensusIds()) {
                cl.cencusTracts.add(censusMap.get(id));
            }
        }*/

        return finishedClusters;


    }
/*
    * Extract data from the csv and generate censustract objects
    * */

    public void extractDataFromBoundry() {

        CsvReader csvReader = new CsvReader(); // to read the Census_2010_Tracts.csv file
        Integer[] wantedColumns = {2, 4, 11, 12}; // indicated wanted columns to read
        List<String[]> allRows = csvReader.readCsv("./Tracts/Census_2010_Tracts.csv",
                wantedColumns);


        for (int i = 1; i < allRows.size(); i++) {

            String[] columns = allRows.get(i);
            //Define two array lists to keep latitudes and longitudes of each boundary point
            ArrayList<Double> latitudes = new ArrayList<Double>();
            ArrayList<Double> longitudes = new ArrayList<Double>();

            // to keep id of cencustract
            long id = 0;

            //New census tract object
            CensusTract censusTract = new CensusTract();

            //To keep mid points
            double midLong = 0, midLat = 0;

            for (int j = 0; j < columns.length; j++) {

                if (j == 0) { // Boundary points column

                    String points = columns[j].substring(16, columns[j].length() - 3); // get the string with lats
                    // and longs

                    String[] individualPoints = points.split(", "); // get individual points into a string array

                    for (int k = 0; k < individualPoints.length; k++) {

                        String[] latNlong = individualPoints[k].split(" ");
                        longitudes.add(Double.parseDouble(latNlong[0]));
                        latitudes.add(Double.parseDouble(latNlong[1]));

                    }
                } else if (j >= columns.length - 2) {
                    double point = Double.parseDouble(columns[j]);
                    if (point < 0) {
                        midLong = point;
                    } else {
                        midLat = point;
                    }
                } else {
                    id = Long.parseLong(columns[j]);
                }

            }
            //Add details to the created census tract
            censusTract.setCensusId(id);
            censusTract.setMidLongitude(midLong);
            censusTract.setMidLatitude(midLat);
            censusTract.setPolygonLonPoints(longitudes);
            censusTract.setPolygonLatPoints(latitudes);
            censusTract.setMinLatitude();
            censusTract.setMinLongitude();
            censusTract.setMaxLatitude();
            censusTract.setMaxLongitude();

            censusTract.setPerimeter();

            GeometryFactory fact = new GeometryFactory();
            Coordinate cor;
            Coordinate[] coordinates = new Coordinate[longitudes.size()];
            for (int j = 0; j < longitudes.size(); j++) {
                cor = new Coordinate(longitudes.get(j), latitudes.get(j));
                coordinates[j] = cor;
            }
            LinearRing linear = new GeometryFactory().createLinearRing(coordinates);
            Polygon poly = new Polygon(linear, null, fact);

            //add created polygon into block
            censusTract.setBlockPolygon(poly);

            //add census tract to list of tracts and map of tracts


            if (id == Long.parseLong("6075017902") || id == Long.parseLong("6075990100") || id == Long.parseLong
                    ("6075980401")) {
                continue;
            }
            censusTracts.add(censusTract);
            censusMap.put(id, censusTract);


        }
    }

    public void extractDataFromNeighBour() {

        CsvReader csvReader = new CsvReader();
        Integer[] wantedColumns = {0, 1}; // indicated wanted columns to read
        List<String[]> allRows = csvReader.readCsv("./Tracts/nlist_2010.csv",
                wantedColumns);

        HashMap<Long, HashSet<Long>> neighbourMap = new HashMap<Long, HashSet<Long>>();

        for (int i = 1; i < allRows.size(); i++) {
            String[] column = allRows.get(i);
            if (column[0].substring(0, 4).equals("6075") && column[1].substring(0, 4).equals("6075")) {

                long id = 0;
                long value = 0;

                id = Long.parseLong(column[0]);
                value = Long.parseLong(column[1]);
                if (id == Long.parseLong("06075017902") || id == Long.parseLong("6075990100") || id == Long.parseLong
                        ("6075980401") || value == Long.parseLong
                        ("6075990100") || value == Long.parseLong("06075017902") || value == Long.parseLong
                        ("6075980401")) {
                    continue;
                }


                if (neighbourMap.containsKey((long) id)) {
                    HashSet<Long> neighbours = neighbourMap.get((long) id);
                    neighbours.add((long) value);
                } else {
                    HashSet<Long> neighbors = new HashSet<Long>();
                    neighbors.add((long) value);
                    neighbourMap.put((long) id, neighbors);
                }
            }
        }
        for (long id : neighbourMap.keySet()) {
            censusMap.get(id).setNeighbourSet(neighbourMap.get(id));
        }
    }


    public void extractDataFromPopulation() {

        CsvReader csvReader = new CsvReader();
        Integer[] wantedColumns = {0, 9}; // indicated wanted columns to read
        List<String[]> allRows = csvReader.readCsv("./Tracts/all_140_in_06.P1.csv",
                wantedColumns);


        for (int i = 1; i < allRows.size(); i++) {
            String[] columns = allRows.get(i);

            if (columns[0].substring(0, 5).equals("06075")) {

                long id = 0;
                long value = 0;

                for (int j = 0; j < columns.length; j++) {

                    if (j == 0) {
                        id = Long.parseLong(columns[j]);
                    } else {
                        value = Long.parseLong(columns[j]);
                    }
                }
                if (id == Long.parseLong("06075017902") || id == Long.parseLong("6075990100") || id == Long.parseLong
                        ("6075980401")) {
                    continue;
                }

                censusMap.get(id).setPopulation(value); // set the population of each census  tract


            }
        }


    }



/*
    * Select first k seeds
    * */

    public HashMap<Long, Cluster> selectFirstK() {

        Collections.sort(censusTracts, CensusTract.populationComparator); // order according to population


        int count = 1;
        long clusterId = 0;

        Cluster cluster = new Cluster();
        cluster.setClusterId(clusterId);
        cluster.setX(x);

        addCensusTractToCluster(cluster, censusTracts.get(0)); // add first censustract to cluster

        clusters.put(clusterId, cluster);
        clusterCol.add(cluster);

         /*Put new cluster into seed collection*/
        Cluster clusterSeed0 = new Cluster();
        clusterSeed0.setClusterId(clusterId);
        clusterSeed0.setX(x);
        addCensusTractToCluster(clusterSeed0, censusTracts.get(0));
        seedClusters.put(clusterId, clusterSeed0);


        while (true) {
            if ((clusterId + 1) >= k || count >= 195) {
                break;
            }
            if (seedChecker(censusTracts.get(count))) {
                clusterId++;
                cluster = new Cluster();
                cluster.setClusterId(clusterId);
                cluster.setX(x);

                addCensusTractToCluster(cluster, censusTracts.get(count)); // add censustract to cluster

                clusters.put(clusterId, cluster);
                clusterCol.add(cluster);

                /*Put new cluster into seed collection*/
                Cluster clusterSeed = new Cluster();
                clusterSeed.setClusterId(clusterId);
                clusterSeed.setX(x);
                addCensusTractToCluster(clusterSeed, censusTracts.get(count));
                seedClusters.put(clusterId, clusterSeed);
            }
            count++;
        }


        return clusters;
    }

    /*
    * Finding best cluster to grow
    * */

    public Cluster bestClusterToGrow() {
        Cluster cluster;
        HashSet<Long> neighboursOfCluster;
        ArrayList<CensusTract> actualNeighbours;


        for (Cluster cluster1 : clusterCol) {

            neighboursOfCluster = new HashSet<Long>();
            actualNeighbours = new ArrayList<CensusTract>();

            for (long censusId : cluster1.getCensusIds()) {
                neighboursOfCluster.addAll(getValidNeighbours(censusMap.get(censusId).getNeighbourSet()));
            }
            for (Long id : neighboursOfCluster) {
                actualNeighbours.add(censusMap.get(id));
            }

            if (actualNeighbours.size() == 0) {
                continue;
            }

            //Collections.sort(actualNeighbours, CensusTract.perimeterComparator);
            // neighbours are sorted according to
            // perimeter
            ArrayList<CensusWithF> sameClusDiffF = new ArrayList<CensusWithF>();

            //loop over all neighbours

            for (CensusTract ct : actualNeighbours) {

                double maxLong = (cluster1.getMaxLongitude() >= ct.getMaxLongitude()) ? cluster1.getMaxLongitude() : ct
                        .getMaxLongitude();

                double maxLat = (cluster1.getMaxLatitude() >= ct.getMaxLatitude()) ? cluster1.getMaxLatitude() : ct
                        .getMaxLatitude();

                double minLong = (cluster1.getMinLongitude() <= ct.getMinLongitude()) ? cluster1.getMinLongitude() : ct
                        .getMinLongitude();

                double minLat = (cluster1.getMinLatitude() <= ct.getMinLatitude()) ? cluster1.getMinLatitude() : ct
                        .getMinLatitude();

                double previousG = cluster1.getG(); // get existing area

                double newG = perimeterOfRectangle(minLat, minLong, maxLat, maxLong); // get area after added the census

                CensusWithF cf = new CensusWithF();
                cf.setCensus(ct);
                cf.setF((cluster1.getH() - ct.getPopulation()) + 5000 * (newG - previousG)); // area gap + population
                // reduction
                sameClusDiffF.add(cf);

            }

            Collections.sort(sameClusDiffF, CensusWithF.fComparator); // sort according to area gap
            CensusTract ct = sameClusDiffF.get(0).getCensus(); // get the min gap neighbour


            //Now compare clusters after adding best polygons for their cost of growing

            long numNeighbours = numberOfNeighboursOfClusterAfterAddingCensus(cluster1, ct);

            cluster1.setF((cluster1.getH() - ct.getPopulation()) - (250 * numNeighbours));// set the f as
            // minimum neighbours + min population so we can choose minimum population + minimum cost to grow
            System.out.println("ClusId N = " + cluster1.getClusterId() + " NeighBours N =" + numNeighbours + " H " +
                    (cluster1.getH() - ct.getPopulation()));
            System.out.println("");

        }


        ArrayList<Cluster> sortCluster = new ArrayList<Cluster>();
        sortCluster.addAll(clusterCol);
        Collections.sort(sortCluster, Cluster.fComparatorBack);
        cluster = sortCluster.get(0);

        Iterator<Cluster> it = sortCluster.iterator();
        while (it.hasNext()) {
            Cluster clusterT = it.next();
            if (cluster.getH() > 0) {
                cluster = clusterT;
                System.out.println("******************************************************************************");
                System.out.println("Selected T = " + clusterT.getClusterId());
                System.out.println("******************************************************************************");
                break;
            }

        }
        System.out.println("******************************************************************************");
        System.out.println("Selected = " + cluster.getClusterId());
        System.out.println("******************************************************************************");
        System.out.println("");
        return cluster;
    }

    private HashSet<Long> getNeighbours(long censusId) {
        return censusMap.get(censusId).getNeighbourSet();
    }

    /*
    * To get the number of neighbours of a given cluster
    * */
    public long numberOfNeighboursOfCluster(Cluster cluster) {
        HashSet<Long> neighboursOfCluster = new HashSet<Long>();

        for (long censusId : cluster.getCensusIds()) {
            neighboursOfCluster.addAll(getValidNeighbours(censusMap.get(censusId).getNeighbourSet()));
        }

        return neighboursOfCluster.size();
    }

    /*
    * To get the number of neighbours after adding a census to a given cluster.
    * */
    public long numberOfNeighboursOfClusterAfterAddingCensus(Cluster cluster, CensusTract censusTract) {
        HashSet<Long> neighboursOfCluster = new HashSet<Long>();

        for (long censusId : cluster.getCensusIds()) {
            neighboursOfCluster.addAll(getValidNeighbours(censusMap.get(censusId).getNeighbourSet()));
        }
        neighboursOfCluster.addAll(getValidNeighbours(censusTract.getNeighbourSet()));

        return neighboursOfCluster.size() - 1;
    }


    /*
    * Adding the best polygon to best cluster
    * */

    public boolean growingTheBestCluster(Cluster cluster) {
        HashSet<Long> neighboursOfCluster;
        ArrayList<CensusTract> actualNeighbours;
        neighboursOfCluster = new HashSet<Long>();

        actualNeighbours = new ArrayList<CensusTract>();

        // getting the valid neighbours of cluster
        for (long censusId : cluster.getCensusIds()) {
            neighboursOfCluster.addAll(getValidNeighbours(censusMap.get(censusId).getNeighbourSet()));
        }

        for (Long id : neighboursOfCluster) {
            actualNeighbours.add(censusMap.get(id));
        }

        ArrayList<Cluster> temporaryClusters = new ArrayList<Cluster>();

        if (actualNeighbours.size() == 0) {
            return false;
        }


        for (CensusTract ct : actualNeighbours) {
            Cluster cluster1 = new Cluster();
            cluster1.setG(cluster.getG());
            cluster1.setH(cluster.getH());


            double maxLong = (cluster.getMaxLongitude() >= ct.getMaxLongitude()) ? cluster.getMaxLongitude() : ct
                    .getMaxLongitude();

            double maxLat = (cluster.getMaxLatitude() >= ct.getMaxLatitude()) ? cluster.getMaxLatitude() : ct
                    .getMaxLatitude();

            double minLong = (cluster.getMinLongitude() <= ct.getMinLongitude()) ? cluster.getMinLongitude() : ct
                    .getMinLongitude();

            double minLat = (cluster.getMinLatitude() <= ct.getMinLatitude()) ? cluster.getMinLatitude() : ct
                    .getMinLatitude();

            double previousG = cluster.getG();
            double newG = perimeterOfRectangle(minLat, minLong, maxLat, maxLong);
//Multiply By 5000
            double f = (cluster1.getH() - ct.getPopulation()) + 5000 * (newG - previousG);
            cluster1.setF(f);
            cluster1.bestPoly = ct;

            temporaryClusters.add(cluster1);
        }

        Collections.sort(temporaryClusters, Cluster.fComparator);
        CensusTract bestCensus = temporaryClusters.get(0).bestPoly;
        addCensusTractToCluster(cluster, bestCensus);
        return true;

    }

    public double getGiniCoefficient() {
        ArrayList<Long> population = new ArrayList<>();
        for (Cluster cluster : finishedClusters.values()) {
            population.add(cluster.getPopulation());
        }
        Collections.sort(population);
        int n = population.size();
        double giniCoefficient = 0;

        double upperSigma = 0;
        double lowerSigma = 0;

        for (int i = 1; i <= n; i++) {
            upperSigma += (n + 1 - i) * population.get(i - 1);

            lowerSigma += population.get(i - 1);

        }

        giniCoefficient = (n + 1 - (2 * upperSigma / lowerSigma)) / n;

        System.out.println(" " + giniCoefficient);

        return giniCoefficient;
    }

    /*
    *  Select censustracts which are not already being added to a cluster
    * */
    public HashSet<Long> getValidNeighbours(HashSet<Long> neighbours) {
        Iterator<Long> it = neighbours.iterator();
        while (it.hasNext()) {
            Long id = it.next();
            if (addedToClustersTracts.containsKey(id)) {
                it.remove();
            }
        }
        return neighbours;
    }

/*
    *
    * Check whether two clusters are too close to be different seeds
    * */


    public boolean seedChecker(CensusTract censusTract1) {
        double distance = 0;

        for (CensusTract census : addedToClustersTracts.values()) {
            distance = distanceBetweenTracts(census, censusTract1);
            if (distance < areaOfMbr()) {
                return false;
            }
        }

        return true;
    }
/*
    * Calculate distance from one cluster to other
    * */


    public double distanceBetweenTracts(CensusTract censusTract, CensusTract censusTract1) {

        double distance = 0;
        DistanceCalculator distanceCalculator = new DistanceCalculator();
        distance = distanceCalculator.distance(censusTract.getMidLatitude(), censusTract.getMidLongitude(),
                censusTract1.getMidLatitude(), censusTract1.getMidLongitude(), "K");
        return distance;
    }
/*
    * Maximum rectangle area which cover whole state
    * */

    public double areaOfMbr() {

        double minimumLatitude = minLatitude(censusTracts);
        double minimumLongiitude = minLongitude(censusTracts);
        double maximumLatitude = maximumLatitude(censusTracts);
        double maximumLongiitude = maximumLongitude(censusTracts);
        DistanceCalculator distanceCalculator = new DistanceCalculator();

        double distanceOfSSide = distanceCalculator.distance(minimumLatitude, minimumLongiitude, maximumLatitude,
                minimumLongiitude, "K");
        double distanceOfLSide = distanceCalculator.distance(minimumLatitude, minimumLongiitude, minimumLatitude,
                maximumLongiitude, "K");
        double recArea = distanceOfSSide * distanceOfLSide;

        return Math.sqrt(recArea / (k * pi)) - 1.05;

    }

    /*
    * Get perimeter of cluster covering rectangle
    * */
    public double perimeterOfRectangle(double minimumLatitude, double minimumLongiitude, double maximumLatitude,
                                       double maximumLongiitude) {

        DistanceCalculator distanceCalculator = new DistanceCalculator();

        double distanceOfSSide = distanceCalculator.distance(minimumLatitude, minimumLongiitude, maximumLatitude,
                minimumLongiitude, "K");
        double distanceOfLSide = distanceCalculator.distance(minimumLatitude, minimumLongiitude, minimumLatitude,
                maximumLongiitude, "K");
        double recPerimeter = (distanceOfSSide * distanceOfLSide);

        return recPerimeter;

    }
/*
    * Minimum latitude among census tracts
    * */


    public double minLatitude(ArrayList<CensusTract> censusTracts) {
        ArrayList<CensusTract> sortCollection = new ArrayList<CensusTract>();
        sortCollection.addAll(censusTracts);
        Collections.sort(sortCollection, CensusTract.latitudeComparator); // order according to minimum latitude
        return sortCollection.get(0).getMinLatitude();
    }
/*
    * Minimum longitude among census tracts
    * */


    public double minLongitude(ArrayList<CensusTract> censusTracts) {
        ArrayList<CensusTract> sortCollection = new ArrayList<CensusTract>();
        sortCollection.addAll(censusTracts);
        Collections.sort(sortCollection, CensusTract.longitudeComparator); // order according to minimum longitude
        return sortCollection.get(0).getMinLongitude();
    }

/*
    * Maximum latitude among census tracts
    * */


    public double maximumLatitude(ArrayList<CensusTract> censusTracts) {
        ArrayList<CensusTract> sortCollection = new ArrayList<CensusTract>();
        sortCollection.addAll(censusTracts);
        Collections.sort(sortCollection, CensusTract.latitudeComparatorMax); // order according to maximum latitude
        return sortCollection.get(censusTracts.size() - 1).getMaxLatitude();
    }
/*
    * Maximum longitude among census tracts
    * */


    public double maximumLongitude(ArrayList<CensusTract> censusTracts) {
        ArrayList<CensusTract> sortCollection = new ArrayList<CensusTract>();
        sortCollection.addAll(censusTracts);
        Collections.sort(sortCollection, CensusTract.longitudeComparatorMax); // order according to maximum longitude
        return sortCollection.get(censusTracts.size() - 1).getMaxLongitude();
    }

    /*
        * Add given censustract into given cluster seed.
        * */
    public void addCensusTractToCluster(Cluster cluster, CensusTract ct) {

        /*
        * Put censustract to taken list
        * */

        addedToClustersTracts.put(ct.getCensusId(), ct); // put the tract into list which contains tracts which are
        // already added to clusters

        cluster.setPopulation(cluster.getPopulation() + ct.getPopulation());

        double maxLong = (cluster.getMaxLongitude() >= ct.getMaxLongitude()) ? cluster.getMaxLongitude() : ct
                .getMaxLongitude();
        cluster.setMaxLongitude(maxLong);

        double maxLat = (cluster.getMaxLatitude() >= ct.getMaxLatitude()) ? cluster.getMaxLatitude() : ct
                .getMaxLatitude();
        cluster.setMaxLatitude(maxLat);

        double minLong = (cluster.getMinLongitude() <= ct.getMinLongitude()) ? cluster.getMinLongitude() : ct
                .getMinLongitude();
        cluster.setMinLongitude(minLong);

        double minLat = (cluster.getMinLatitude() <= ct.getMinLatitude()) ? cluster.getMinLatitude() : ct
                .getMinLatitude();
        cluster.setMinLatitude(minLat);

        cluster.setH(cluster.getX() - cluster.getPopulation());

        cluster.setCensusIds(ct.getCensusId());

        double g = perimeterOfRectangle(minLat, minLong, maxLat, maxLong);
        cluster.setG(g);

        cluster.cencusTracts.add(ct);

    }


}

