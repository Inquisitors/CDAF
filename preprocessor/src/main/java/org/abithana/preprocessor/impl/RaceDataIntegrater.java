package org.abithana.preprocessor.impl;

import org.abithana.beans.CrimeDataBean;
import org.abithana.beans.PopulationBean;
import org.abithana.beans.PredictionDataBean;
import org.abithana.beans.PredictionDataBeanTest;
import org.abithana.ds.PredictionDataStore;
import org.abithana.ds.PreprocessedCrimeDataStore;
import org.abithana.utill.Config;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.util.List;

/**
 * Created by acer on 2/22/2017.
 */
public class RaceDataIntegrater implements Serializable {

    Config instance=Config.getInstance();
    PreprocessedCrimeDataStore preprocessedCrimeDataStore=PreprocessedCrimeDataStore.getInstance();
    PredictionDataStore predictionDataStore=PredictionDataStore.getInstance();
    DataFrame preprocessedDF=preprocessedCrimeDataStore.getDataFrame();
    Checker checker=new Checker();

    public RaceDataIntegrater(){
        preprocessedDF=preprocessedCrimeDataStore.getDataFrame();
    }
    public boolean integrateRaceData(boolean state){

        if(state) {

            if(predictionDataStore.getDataWithRace()!=null){
                predictionDataStore.setPredictionDf(preprocessedCrimeDataStore.getDataFrame());
            }
            else {
                preprocessedDF=preprocessedCrimeDataStore.getDataFrame();
                preprocessedDF.show(40);
                try {
                    List<PredictionDataBean> predictionBeansList = preprocessedDF.javaRDD().map(new Function<Row, PredictionDataBean>() {
                        public PredictionDataBean call(Row row) {

                            double lat = row.getAs("latitude");
                            double lon = row.getAs("longitude");
                            double[] positions = {lon, lat};
                            //TODO call and get other data
                            RaceBlock block = checker.getRaceData(positions);

                            if (block == null)
                                return new PredictionDataBean(0,0,0,0,""+0,""+0,""+0,0,0,0,0,0,0,0,0,0,0);
                            PredictionDataBean crimeDataBean = new PredictionDataBean((int) row.getAs("year"), (int) row.getAs("month"), (int) row.getAs("day"), (int) row.getAs("time"), "" + row.getAs("dayOfWeek"), "" + row.getAs("category"), "" + row.getAs("pdDistrict"),
                                    block.getTotalPopulation(), block.getHispanic_Latino(), block.getOne_race_total(), block.getWhite(), block.getBlack_or_African_American(), block.getAmerican_Indian_and_Alaska_Native(), block.getAsian(), block.getHawaiian(), block.getOther_Race(), block.getTwo_or_More_Races());
                            return crimeDataBean;

                        }
                    }).collect();

                    DataFrame df = instance.getSqlContext().createDataFrame(predictionBeansList, PredictionDataBean.class);
                    df.show(500);
                    df.registerTempTable(predictionDataStore.getTableName());
                    predictionDataStore.setPredictionDf(df);
                    predictionDataStore.setDataWithRace(df);
                    return true;
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

        predictionDataStore.setPredictionDf(preprocessedCrimeDataStore.getDataFrame());

        return true;
    }

    public DataFrame integrateRaceDataForTest(DataFrame df){

        preprocessedDF=preprocessedCrimeDataStore.getDataFrame();
        preprocessedDF.show(40);
        try {
            List<PredictionDataBeanTest> predictionBeansList = df.javaRDD().map(new Function<Row, PredictionDataBeanTest>() {
                public PredictionDataBeanTest call(Row row) {

                    double lat = row.getAs("latitude");
                    double lon = row.getAs("longitude");
                    double[] positions = {lon, lat};
                    //TODO call and get other data
                    RaceBlock block = checker.getRaceData(positions);

                    if (block == null)
                        return new PredictionDataBeanTest(0,0,0,0,""+0,""+0,0,0,0,0,0,0,0,0,0,0);
                    PredictionDataBeanTest crimeDataBean = new PredictionDataBeanTest((int) row.getAs("year"), (int) row.getAs("month"), (int) row.getAs("day"), (int) row.getAs("time"), "" + row.getAs("dayOfWeek"), "" + row.getAs("pdDistrict"),
                            block.getTotalPopulation(), block.getHispanic_Latino(), block.getOne_race_total(), block.getWhite(), block.getBlack_or_African_American(), block.getAmerican_Indian_and_Alaska_Native(), block.getAsian(), block.getHawaiian(), block.getOther_Race(), block.getTwo_or_More_Races());
                    return crimeDataBean;

                }
            }).collect();

            DataFrame df1 = instance.getSqlContext().createDataFrame(predictionBeansList, PredictionDataBeanTest.class);

            return df1;
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

}
