package org.abithana.prescription.impl.Redistricting;

import scala.annotation.serializable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jayz on 26/02/2017.
 */
public class CalculateIsoperimetricQuotient implements Serializable {
    public double getArea(ArrayList<CensusTract> cencusTracts) {
        double area = 0;
        for (CensusTract mainBlock : cencusTracts) {
            area += mainBlock.getBlockPolygon().getArea();
        }



        return area * 111 * 111;
    }

    public double perimeterOfCencus(ArrayList<Double> polygonLonPoints, ArrayList<Double> polygonLatPoints) {
        DistanceCalculator distanceCalculator = new DistanceCalculator();

        double perimeter = 0;
        for (int i = 1; i < polygonLatPoints.size(); i++) {
            perimeter += distanceCalculator.distance(polygonLatPoints.get(i - 1), polygonLonPoints.get(i - 1), polygonLatPoints.get(i),
                    polygonLonPoints.get(i), "K");
        }
        return perimeter;
    }

    public double isoperimetricQuotient(ArrayList<CensusTract> cencusTracts) {

        ArrayList<Point> listOfAllBoudaryPoints = new ArrayList<Point>();

        for (CensusTract cts : cencusTracts) {
            for (int i = 0; i< cts.getPolygonLonPoints().size(); i++) {
                Point point = new Point();
                point.x =  cts.getPolygonLonPoints().get(i);
                point.y = cts.getPolygonLatPoints().get(i);
                listOfAllBoudaryPoints.add(point);
            }

        }

        Jarvis jarvis = new Jarvis();
        jarvis.convexHull(listOfAllBoudaryPoints);
        double isoQuotient = 0;
       // isoQuotient = (4 * Math.PI * getArea(cencusTracts))/ (perimeterOfRect() * perimeterOfRect());
        return isoQuotient;
    }
}
