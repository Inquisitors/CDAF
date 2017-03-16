package org.abithana.preprocessor.impl;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import java.io.Serializable;
import java.util.*;

/**
 * Created by malakaganga on 1/23/17.
 */
public class Checker implements Serializable {

    private ArrayList<CensusBlock> censusTracts = new ArrayList<CensusBlock>(); // to hold census tracts
    private HashMap<Long, RaceBlock> raceBlocks = new HashMap<Long, RaceBlock>();

    public HashMap<Long, CensusBlock> getCensusMap() {
        return censusMap;
    }

    public void setCensusMap(HashMap<Long, CensusBlock> censusMap) {
        this.censusMap = censusMap;
    }

    private HashMap<Long, CensusBlock> censusMap = new HashMap<Long, CensusBlock>();

    public static void main(String[] args) {
        Checker ch = new Checker();
        double array[] = {-122.4274930, +37.7264885};

        System.out.println("Block ID = "+ch.getRaceData(array).getBlockId());


        /*HashSet<Long> ne = new HashSet<Long>();
        long id = Long.parseLong("060750169002000");
        ne = ch.getNeighbours(id);
        for (Long idC : ne) {
            System.out.print(" " + idC + ", ");
        }*/
    }


    public Checker() {
        extractDataFromBoundry();
        extractDataFromRaceFile();
    }
    public RaceBlock getRaceData(double[] point) {
        long blockId = polygonChecker(point);
        return raceBlocks.get(blockId);


    }
    public long polygonChecker(double[] point) {


        long polygonID = 0;
        double pointLong = point[0];
        double pointLat = point[1];

        for (CensusBlock censusTract : censusTracts) {

            int count = 0;
            ArrayList<Double> latPoints = censusTract.getPolygonLatPoints();
            ArrayList<Double> longPoints = censusTract.getPolygonLonPoints();

            for (int i = 0; i < latPoints.size(); i++) {
                int j = (i + 1) % latPoints.size();
                double latUp = latPoints.get(i);
                double longUp = longPoints.get(i);
                double latDown = latPoints.get(j);
                double longDown = longPoints.get(j);

                if (latUp == latDown) {
                    continue;
                } else {
                    double upPoint = (latUp > latDown) ? latUp : latDown;
                    double lowPoint = (latDown < latUp) ? latDown : latUp;
                    if (pointLat > upPoint || pointLat < lowPoint) {
                        continue;
                    }
                    if (pointLong <= breakLong(latUp, longUp, latDown, longDown, pointLat)) {
                        count++;
                    }
                }

            }
            if ((count % 2) == 1) {
                return censusTract.getCensusId();
            }


        }

        return polygonID;
    }

    public HashMap<Long, Cluster> convertToCluster(HashMap<Integer, ArrayList<Long>> idMap) {

        long i = 0;
        HashMap<Long, Cluster> finishedClusters = new HashMap<Long, Cluster>();

        for (ArrayList<Long> idList : idMap.values()) {
            long clusterId = i;
            Cluster cluster = new Cluster();
            cluster.setClusterId(clusterId);
            i++;
            for (Long cenId : idList) {
                cluster.setCensusIds(cenId);
            }
            finishedClusters.put(clusterId, cluster);

        }


        for (Cluster cl : finishedClusters.values()) {
            for (long id : cl.getCensusIds()) {
                cl.getCencusTracts().add(censusMap.get(id));
            }
        }


        return finishedClusters;


    }

    public HashSet<Long> getNeighbours(long id) {
        HashSet<Long> neighbours = new HashSet<Long>();
        CensusBlock mainBlock = censusMap.get((Long) id);
        for (CensusBlock cb : censusMap.values()) {
            if (mainBlock.getBlockPolygon().touches(cb.getBlockPolygon())) {
                neighbours.add(cb.getCensusId());
            }
        }
        return neighbours;
    }

    private double breakLong(double latUp, double longUp, double latDown, double longDown, double pointLat) {
        double breakLong = longUp - ((latUp - pointLat) * (longUp - longDown)) / (latUp - latDown);
        return breakLong;
    }
        private void extractDataFromRaceFile() {

            CsvReader csvReader = new CsvReader(); // to read the Census_2010_Tracts.csv file
            Integer[] wantedColumns = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}; // indicated wanted columns to read
            List<String[]> allRows = csvReader.readCsv("./Tracts/RaceData.csv",
                    wantedColumns);


            for (int i = 1; i < allRows.size(); i++) {

                String[] columns = allRows.get(i);
                // to keep id of cencusBlock

                //New census block object
                RaceBlock raceBlock = new RaceBlock();


                String[] idSet = columns[0].split(",");

                String[] blockIdlist = idSet[0].split(" ");
                String blockId = blockIdlist[1];

                String[] censusList = idSet[2].split(" ");
                String censusPId = censusList[3];

                int size = censusPId.length();
                String censusSId = null;

                if (size == 3) {
                    censusSId = "60750" + censusPId + "00" + blockId;
                } else if (size == 4) {
                    censusSId = "6075" + censusPId + "00" + blockId;
                } else if (size == 6) {
                    String[] blockDot = censusPId.split("\\.");
                    censusSId = "60750" + blockDot[0] + blockDot[1] + blockId;
                } else if (size == 7) {
                    String[] blockDot = censusPId.split("\\.");
                    censusSId = "6075" + blockDot[0] + blockDot[1] + blockId;
                }

                long blockRId = Long.parseLong(censusSId);
                //To keep population data
                int totalPopulation = Integer.parseInt(columns[1]);
                int Hispanic_Latino = Integer.parseInt(columns[2]);
                int One_race_total = Integer.parseInt(columns[3]);
                int White = Integer.parseInt(columns[4]);
                int Black_or_African_American = Integer.parseInt(columns[5]);
                int American_Indian_and_Alaska_Native = Integer.parseInt(columns[6]);
                int Asian = Integer.parseInt(columns[7]);
                int Hawaiian = Integer.parseInt(columns[8]);
                int Other_Race = Integer.parseInt(columns[9]);
                int Two_or_More_Races = Integer.parseInt(columns[10]);

                raceBlock.setBlockId(blockRId);
                raceBlock.setTotalPopulation(totalPopulation);
                raceBlock.setHispanic_Latino(Hispanic_Latino);
                raceBlock.setOne_race_total(One_race_total);
                raceBlock.setWhite(White);
                raceBlock.setBlack_or_African_American(Black_or_African_American);
                raceBlock.setAmerican_Indian_and_Alaska_Native(American_Indian_and_Alaska_Native);
                raceBlock.setAsian(Asian);
                raceBlock.setHawaiian(Hawaiian);
                raceBlock.setOther_Race(Other_Race);
                raceBlock.setTwo_or_More_Races(Two_or_More_Races);

                raceBlocks.put(blockRId, raceBlock);
            }
        }

    private void extractDataFromBoundry() {

        CsvReader csvReader = new CsvReader(); // to read the Census_2010_Tracts.csv file
        Integer[] wantedColumns = {0, 5, 13, 14}; // indicated wanted columns to read
        List<String[]> allRows = csvReader.readCsv("./Tracts/tl_2010_06075_tabblock10.csv",
                wantedColumns);


        for (int i = 1; i < allRows.size(); i++) {

            String[] columns = allRows.get(i);
            //Define two array lists to keep latitudes and longitudes of each boundary point
            ArrayList<Double> latitudes = new ArrayList<Double>();
            ArrayList<Double> longitudes = new ArrayList<Double>();

            // to keep id of cencustract
            long id = 0;

            //New census tract object
            CensusBlock censusTract = new CensusBlock();

            //To keep mid points
            double midLong = 0, midLat = 0;

            for (int j = 0; j < columns.length; j++) {

                if (j == 0) { // Boundary points column

                    int x = columns[j].indexOf(")");
                    String points = columns[j].substring(16, x); // get the string with lats
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
            censusTract.setPolygonLonPoints(longitudes);
            censusTract.setPolygonLatPoints(latitudes);
            censusTract.setMidLongitude(midLong);
            censusTract.setMidLatitude(midLat);
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

            censusTracts.add(censusTract);
            censusMap.put(id, censusTract);


        }
    }
}
