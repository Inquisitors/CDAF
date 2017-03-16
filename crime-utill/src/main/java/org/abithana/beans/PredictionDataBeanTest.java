package org.abithana.beans;

import java.io.Serializable;

/**
 * Created by acer on 2/25/2017.
 */
public class PredictionDataBeanTest implements Serializable {

    private int year;
    private int month;
    private int day;
    private int Time;
    private String DayOfWeek;
    private String PdDistrict;
    //private String resolution;
    private int totalPopulation;
    private int Hispanic_Latino;
    private int One_race_total;
    private int White;
    private int Black_or_African_American;
    private int American_Indian_and_Alaska_Native;
    private int Asian;
    private int Hawaiian;
    private int Other_Race;
    private int Two_or_More_Races;

    public PredictionDataBeanTest(int year, int month, int day, int time, String dayOfWeek, String pdDistrict,  int totalPopulation, int hispanic_Latino, int one_race_total, int white, int black_or_African_American, int american_Indian_and_Alaska_Native, int asian, int hawaiian, int other_Race, int two_or_More_Races) {
        this.year = year;
        this.month = month;
        this.day = day;
        Time = time;
        DayOfWeek = dayOfWeek;
        PdDistrict = pdDistrict;
        //this.resolution = resolution;
        this.totalPopulation = totalPopulation;
        Hispanic_Latino = hispanic_Latino;
        One_race_total = one_race_total;
        White = white;
        Black_or_African_American = black_or_African_American;
        American_Indian_and_Alaska_Native = american_Indian_and_Alaska_Native;
        Asian = asian;
        Hawaiian = hawaiian;
        Other_Race = other_Race;
        Two_or_More_Races = two_or_More_Races;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getTime() {
        return Time;
    }

    public String getDayOfWeek() {
        return DayOfWeek;
    }

    public String getPdDistrict() {
        return PdDistrict;
    }

    public int getTotalPopulation() {
        return totalPopulation;
    }

    public int getHispanic_Latino() {
        return Hispanic_Latino;
    }

    public int getOne_race_total() {
        return One_race_total;
    }

    public int getWhite() {
        return White;
    }

    public int getBlack_or_African_American() {
        return Black_or_African_American;
    }

    public int getAmerican_Indian_and_Alaska_Native() {
        return American_Indian_and_Alaska_Native;
    }

    public int getAsian() {
        return Asian;
    }

    public int getHawaiian() {
        return Hawaiian;
    }

    public int getOther_Race() {
        return Other_Race;
    }

    public int getTwo_or_More_Races() {
        return Two_or_More_Races;
    }
}
