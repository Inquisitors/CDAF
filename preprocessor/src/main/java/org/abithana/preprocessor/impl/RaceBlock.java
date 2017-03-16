package org.abithana.preprocessor.impl;

import java.io.Serializable;

/**
 * Created by Jayz on 22/02/2017.
 */
public class RaceBlock implements Serializable {

    private long blockId;
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

    public long getBlockId() {
        return blockId;
    }

    public void setBlockId(long blockId) {
        this.blockId = blockId;
    }

    public int getTotalPopulation() {
        return totalPopulation;
    }

    public void setTotalPopulation(int totalPopulation) {
        this.totalPopulation = totalPopulation;
    }

    public int getHispanic_Latino() {
        return Hispanic_Latino;
    }

    public void setHispanic_Latino(int hispanic_Latino) {
        Hispanic_Latino = hispanic_Latino;
    }

    public int getOne_race_total() {
        return One_race_total;
    }

    public void setOne_race_total(int one_race_total) {
        One_race_total = one_race_total;
    }

    public int getWhite() {
        return White;
    }

    public void setWhite(int white) {
        White = white;
    }

    public int getBlack_or_African_American() {
        return Black_or_African_American;
    }

    public void setBlack_or_African_American(int black_or_African_American) {
        Black_or_African_American = black_or_African_American;
    }

    public int getAmerican_Indian_and_Alaska_Native() {
        return American_Indian_and_Alaska_Native;
    }

    public void setAmerican_Indian_and_Alaska_Native(int american_Indian_and_Alaska_Native) {
        American_Indian_and_Alaska_Native = american_Indian_and_Alaska_Native;
    }

    public int getAsian() {
        return Asian;
    }

    public void setAsian(int asian) {
        Asian = asian;
    }

    public int getHawaiian() {
        return Hawaiian;
    }

    public void setHawaiian(int hawaiian) {
        Hawaiian = hawaiian;
    }

    public int getOther_Race() {
        return Other_Race;
    }

    public void setOther_Race(int other_Race) {
        Other_Race = other_Race;
    }

    public int getTwo_or_More_Races() {
        return Two_or_More_Races;
    }

    public void setTwo_or_More_Races(int two_or_More_Races) {
        Two_or_More_Races = two_or_More_Races;
    }
}
