package org.abithana.ds;

import org.abithana.beans.DataStoreBeans;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.storage.StorageLevel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by acer on 2/22/2017.
 */
public class PredictionDataStore implements Serializable{


    private static PredictionDataStore predictionDataStore=new PredictionDataStore();
    private static DataFrame predictionDf;
    private String predictionTableName="predictionData";
    private static DataFrame defaultDataSet=predictionDataStore.getDataFrame();
    private static DataFrame dataWithRace;


    private PredictionDataStore(){

    }

    public static PredictionDataStore getInstance(){
        return predictionDataStore;
    }

    public String[] showColumns(String tableName) {
        if(tableName==predictionTableName){
            return predictionDf.columns();
        }
        else{
            return null;
        }
    }

    public DataFrame getDataFrame() {
        return predictionDf;
    }


    public JavaRDD<DataStoreBeans> getRDD(String tableName) {
        return null;
    }

    public JavaRDD<Vector> getDataVector() {
        return null;
    }

    public List<Row> getList(String sqlQuery) {
        return null;
    }

    public String getTableName() {
        return predictionTableName;
    }

    public void setTableName(String prepTableName) {
        this.predictionTableName = prepTableName;
    }


    public void setPredictionDf(DataFrame predictionDf) {
        PredictionDataStore.predictionDf = predictionDf;
    }

    public void setPredictionTableName(String predictionTableName) {
        this.predictionTableName = predictionTableName;
    }

    public  void setDefaultDataSet(DataFrame defaultDataSet) {
        PredictionDataStore.defaultDataSet = defaultDataSet;
    }

    public  void setDataWithRace(DataFrame dataWithRace) {
        PredictionDataStore.dataWithRace = dataWithRace;
    }

    public DataFrame getDataWithRace(){
        return dataWithRace;
    }
}
