package com.muzimin.input.obs

import cn.hutool.json.{JSONArray, JSONUtil}
import com.muzimin.input.Reader
import com.obs.services.ObsClient
import com.obs.services.model.{ListObjectsRequest, ObjectListing}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import scala.io.Source
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import org.apache.spark.sql.functions._

/**
 * @author: 李煌民
 * @date: 2022-08-12 14:20
 *        ${description}
 **/
case class ObsJsonInput(
                         name: String,
                         ak: String,
                         sk: String,
                         endPoint: String,
                         bucketName: String,
                         prefix: Option[String],
                         schemaColumnOrder: Option[String]
                       ) extends Reader with ObsDataInputBase {

  override def read(spark: SparkSession): DataFrame = {
    val obsClient = getObsClient(ak, sk, endPoint)

    val request = setRequest(bucketName, prefix)

    val list = new ListBuffer[String]

    var result: ObjectListing = null
    do {
      result = obsClient.listObjects(request)

      for (obsObject <- result.getObjects) {
        val objectKey = obsObject.getObjectKey

        if (objectKey.contains("json")) {
          val jsonContent = Source.fromInputStream(obsClient.getObject(bucketName, objectKey).getObjectContent).mkString

          for (jsonObject <- JSONUtil.parseArray(jsonContent)) {
            list.append(jsonObject.toString)
          }
        }
      }
      request.setMarker(result.getNextMarker)

    } while (result.isTruncated)

    import spark.implicits._
    spark
      .sparkContext
      .makeRDD(list)
      .toDF("temp")
      .select(getJsonObject(schemaColumnOrder): _*)
  }
}
