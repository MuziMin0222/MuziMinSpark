package com.muzimin.configuration

import com.muzimin.configuration.job.{Catalog, Input, Instrumentation, Output}
import com.muzimin.input.Reader

/**
 * @author : 李煌民
 * @date : 2021-09-14 11:31
 *       ${description}
 **/
case class Configuration(
                          steps: Option[Seq[String]],
                          inputs: Option[Map[String, Input]],
                          variables: Option[Map[String, String]],   //sql中要配置的变量
                          instrumentation: Option[Instrumentation], // influxDB配置
                          output: Option[Output],
                          outputs: Option[Map[String, Output]],
                          catalog: Option[Catalog], //Spark Catalog 配置
                          cacheOnPreview: Option[Boolean],
                          showQuery: Option[Boolean],
                          //streaming: Option[Streaming],
                          //periodic: Option[Periodic],
                          var logLevel: Option[String], //日志级别配置
                          //var showPreviewLines: Option[Int],
                          //var explain: Option[Boolean],
                          var appName: Option[String] //任务的appName配置
                          //var continueOnFailedStep: Option[Boolean],
                          //var cacheCountOnOutput: Option[Boolean],
                          //var ignoreDeequValidations: Option[Boolean],
                          //var failedDFLocationPrefix: Option[String]
                        ) {

  //require 表示step文件必须要有，否则程序直接退出
  require(steps.isDefined)
  //如果日志级别没有传入，默认是INFO
  logLevel = Option(logLevel.getOrElse("INFO"))
  //设置默认的AppName
  appName = Option(appName.getOrElse("MuziMinSpark"))

  //将配置文件中配置的input，转为临时表，临时表的表名就是map的key
  def getReaders: Seq[Reader] = {
    inputs.getOrElse(Map())
      .map {
        case (name, input) => {
          input.getReader(name)
        }
      }.toSeq
  }

  override def toString: String = {
    s"""
       |steps -> $steps
       |inputs -> $inputs
       |variables -> $variables
       |instrumentation -> $instrumentation
       |output -> $output
       |outputs -> $outputs
       |catalog -> $catalog
       |cacheOnPreview -> $cacheOnPreview
       |showQuery -> $showQuery
       |logLevel -> $logLevel
       |appName -> $appName
       |""".stripMargin
  }
}
