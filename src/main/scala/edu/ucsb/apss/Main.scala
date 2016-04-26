package edu.ucsb.apss

import edu.ucsb.apss.PSS.PSSDriver
import edu.ucsb.apss.preprocessing.TweetToVectorConverter
import org.apache.log4j.Logger

import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by dimberman on 1/23/16.
  */


case class Sim(i: Long, j: Long, sim: Double) extends Ordered[Sim] {
    override def compare(that: Sim): Int = this.sim compare that.sim

    override def toString() = s"($i,$j): $sim"
}


case class PSSConfig(
                      input: String = "",
                      thresholds: Seq[Double] = Seq(0.9),
                      numLayers: Int = 21,
                      balanceStage1: Boolean = true,
                      balanceStage2: Boolean = true

                    )

object Main {

    val log = Logger.getLogger(this.getClass)

    def main(args: Array[String]) {

        val opts = new scopt.OptionParser[PSSConfig]("PSS") {
            opt[String]('i', "input")
              .required()
              .action { (x, c) =>
                  c.copy(input = x)
              } text "input is the input file"
            opt[Seq[Double]]('t', "threshold")
              .optional()
              .action { (x, c) =>
                  c.copy(thresholds = x)
              } text "threshold is the threshold for PSS, defaults to 0.9"
            opt[Int]('n', "numLayers")
              .optional()
              .action { (x, c) =>
                  c.copy(numLayers = x)
              } text "number of layers in PSS, defaults to 21"
        }


    }


    def run(config: PSSConfig) = {
        val conf = new SparkConf().setAppName("apss test").set("spark.dynamicAllocation.initialExecutors", "5").set("spark.yarn.executor.memoryOverhead", "600")
          .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        val sc = new SparkContext(conf)
        val par = sc.textFile(config.input)
        println(s"taking in from ${config.input}")
        println(s"default par: ${sc.defaultParallelism}")
        val executionValues = config.thresholds
        val buckets = config.numLayers
        val vecs = par.map((new TweetToVectorConverter).convertTweetToVector)
        val staticPartitioningValues = ArrayBuffer[Long]()
        val dynamicPartitioningValues = ArrayBuffer[Long]()
        val timings = ArrayBuffer[Long]()


        val driver = new PSSDriver((config.balanceStage1,config.balanceStage2))



        for (i <- executionValues) {
            val threshold = i
            val t1 = System.currentTimeMillis()
            val answer = driver.run(sc, vecs, buckets, threshold).persist()
            answer.count()
            val current = System.currentTimeMillis() - t1
            log.info(s"breakdown: apss with threshold $threshold using $buckets buckets took ${current / 1000} seconds")
            val top = answer.map { case (i, j, sim) => Sim(i, j, sim) }.top(10)
            println("breakdown: top 10 similarities")
            top.foreach(s => println(s"breakdown: $s"))
            staticPartitioningValues.append(driver.sParReduction)
            dynamicPartitioningValues.append(driver.dParReduction)
            timings.append(current / 1000)
            answer.unpersist()
        }





        val numPairs = driver.numVectors * driver.numVectors / 2
        log.info("breakdown:")
        log.info("breakdown:")
        log.info("breakdown: ************histogram******************")
        //        log.info("breakdown:," + buckets.foldRight("")((a,b) => a + "," + b))
        log.info("breakdown:," + executionValues.foldRight("")((a, b) => a + "," + b))
        log.info("breakdown:staticPairRemoval," + staticPartitioningValues.foldRight("")((a, b) => a + "," + b))
        log.info("breakdown:static%reduction," + staticPartitioningValues.map(a => a.toDouble / numPairs).foldRight("")((a, b) => a + "," + b))
        log.info("breakdown:dynamic," + dynamicPartitioningValues.foldRight("")((a, b) => a + "," + b))
        log.info("breakdown:timing," + timings.foldRight("")((a, b) => a + "," + b))
    }

}
