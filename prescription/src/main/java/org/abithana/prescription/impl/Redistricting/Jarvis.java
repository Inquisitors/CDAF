package org.abithana.prescription.impl.Redistricting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


/** Class Jarvis **/

public class Jarvis implements Serializable

{

    private boolean CCW(Point p, Point q, Point r)

    {

        double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);



        if (val >= 0)

            return false;

        return true;

    }

    public void convexHull(ArrayList<Point> points)

    {

        int n = points.size();

        /** if less than 3 points return **/

        if (n < 3)

            return;

        int[] next = new int[n];

        Arrays.fill(next, -1);



        /** find the leftmost point **/

        int leftMost = 0;

        for (int i = 1; i < n; i++)

            if (points.get(i).x < points.get(leftMost).x)

                leftMost = i;

        int p = leftMost, q;

        /** iterate till p becomes leftMost **/

        do

        {

            /** wrapping **/

            q = (p + 1) % n;

            for (int i = 0; i < n; i++)

                if (CCW(points.get(p), points.get(i), points.get(q)))

                    q = i;



            next[p] = q;

            p = q;

        } while (p != leftMost);
        /** Display result **/

        display(points, next);

    }
    public void display(ArrayList<Point> points, int[] next)

    {

        System.out.println("\nConvex Hull points : ");

        for (int i = 0; i < next.length; i++)

            if (next[i] != -1)

                System.out.println("("+ points.get(i).x +", "+ points.get(i).y +")");

    }


}