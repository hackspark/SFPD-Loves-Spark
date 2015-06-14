
val raw = sc.textFile("/resources/rows.csv").cache
println(raw.count)
raw.take(3).foreach(println)

// [-122.516, -122.360, 37.700, 37.809]
val minX = -122.516
val maxX = -122.360
val minY = 37.700
val maxY = 37.809
val granularity = .01
val numberBoxes = 15

println((maxX - minX) / granularity)
println((maxY - minY) / granularity)

def toBox(x: Double, y: Double): Int = 
    ((y - minY) / granularity).toInt * numberBoxes + ((x - minX) / granularity).toInt
println(toBox(minX, minY), toBox(minX, maxY), toBox(maxX, minY), toBox(maxX, maxY))

val otherThanQuote = " [^\"] "
val quotedString = String.format(" \" %s* \" ", otherThanQuote)
val regex = String.format(
    "(?x) "+ // enable comments, ignore white spaces
    ",                         "+ // match a comma
    "(?=                       "+ // start positive look ahead
    "  (                       "+ //   start group 1
    "    %s*                   "+ //     match 'otherThanQuote' zero or more times
    "    %s                    "+ //     match 'quotedString'
    "  )*                      "+ //   end group 1 and repeat it zero or more times
    "  %s*                     "+ //   match 'otherThanQuote'
    "  $                       "+ // match the end of the string
    ")                         ", // stop positive look ahead
    otherThanQuote, quotedString, otherThanQuote)

val rawParse = (raw
    .mapPartitionsWithIndex{case (index, iter) => if (index == 0) iter.drop(1) else iter}
    .map(line => line.split(regex, -1))
    .filter(array => {
        val x = array(9).toFloat
        val y = array(10).toFloat
        if (x >= minX && x <= maxX && y >= minY && y <= maxY) true
        else false
        // array(9) != "-120.5" && array(10) != "90"
    }))
rawParse.cache
println(rawParse.count)
rawParse.take(3).foreach(array => println(array.mkString(";")))

def toRiskLevel(count: Long): Int = {
    if (count < 5) 0 // Low
    else if (count < 30) 1 // Medium
    else 2 // High
}

def toDayPeriod(dt: java.util.Date): Int = {
    val time = dt.getHours
    if (time >= 22 || time < 6) 0 // Night
    else if (time >= 6 && time < 12) 1 // Morning
    else if (time >= 12 && time < 18) 2 // Afternoon
    else 3 // Evening
}

val sqlContext = new org.apache.spark.sql.SQLContext(sc)
import sqlContext.implicits._

case class Incident(
    IncidntNum: Int,
    Category: String,
    Descript: String,
    DayOfWeek: String,
    Date: java.sql.Date,
    Timestamp: java.sql.Timestamp,
    PdDistrict: String,
    Resolution: String,
    Address: String,
    X: Double,
    Y: Double,
    PdId: String,
    Box: Int,
    Hour: Int,
    DayPeriod: Int,
    DayOfWeekNum: Int,
    Month: Int)
val formatIn = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm")
val formatDayNum = new java.text.SimpleDateFormat("u")

val incidentsRDD = rawParse.map(i => {
    val x = i(9).toDouble
    val y = i(10).toDouble
    val dt = formatIn.parse(i(4) + " " + i(5))
    Incident(
    i(0).toInt, 
    i(1), 
    i(2), 
    i(3),
    new java.sql.Date(dt.getDay, dt.getMonth, dt.getYear),
    new java.sql.Timestamp(dt.getTime),
    i(6),
    i(7),
    i(8),
    x,
    y,
    i(12),
    toBox(x, y),
    dt.getHours,
    toDayPeriod(dt),
    formatDayNum.format(dt).toInt,
    dt.getMonth)
}).cache
val incidentsDF = incidentsRDD.toDF()
incidentsDF.registerTempTable("incidents")

println(sqlContext.sql("SELECT MIN(X), MAX(X), MIN(Y), MAX(Y) FROM incidents").collect().mkString(","))
println(sqlContext.sql("SELECT MIN(Box), MAX(Box), MIN(Hour), MAX(Hour), MIN(DayPeriod), MAX(DayPeriod), MIN(Month), MAX(Month) FROM incidents").collect().mkString(","))
println(sqlContext.sql("SELECT DayOfWeek, COUNT(*) FROM incidents GROUP BY DayOfWeek").collect().mkString(","))
println(sqlContext.sql("SELECT DayOfWeekNum, COUNT(*) FROM incidents GROUP BY DayOfWeekNum").collect().mkString(","))

sqlContext.sql("SELECT MIN(Timestamp), MAX(Timestamp) FROM incidents").collect()

val df = (incidentsDF
    .groupBy($"Box", $"Date")
    .agg(Map("*" -> "count"))).cache
println(df.agg(Map("COUNT(1)" -> "min")).collect.mkString)
println(df.agg(Map("COUNT(1)" -> "max")).collect.mkString)
println(df.agg(Map("COUNT(1)" -> "avg")).collect.mkString)

val boxAgg = sqlContext.sql(
    "SELECT COUNT(*) AS `Count`, Box, Month, DayOfWeekNum, DayPeriod " + 
    "FROM incidents GROUP BY Box, Month, DayOfWeekNum, DayPeriod").cache
println(boxAgg.count)
boxAgg.take(5).foreach(a => println(a.mkString(";")))
boxAgg.printSchema

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

val labeled = boxAgg.map(
    i => LabeledPoint(
        toRiskLevel(i.getLong(0)), 
        Vectors.dense(i.getInt(1), i.getInt(2), i.getInt(3), i.getInt(4)))).cache
println(labeled.count)
labeled.take(5).foreach(a => println(a))

labeled.filter(p => p.label == 0.0).count

import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.mllib.util.MLUtils

val numClasses = 3
val categoricalFeaturesInfo = Map[Int, Int](1 -> 12, 2 -> 8, 3 -> 4)
val impurity = "gini"
val maxDepth = 5
val maxBins = 32

val model = DecisionTree.trainClassifier(labeled, numClasses, categoricalFeaturesInfo,
  impurity, maxDepth, maxBins)
model.save(sc, "/resources/model4")

model.predict(Vectors.dense(72, 8, 2, 1))

val splits = labeled.randomSplit(Array(0.7, 0.3), seed=777)
val (trainingData, testData) = (splits(0), splits(1))

val modelEval = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
  impurity, maxDepth, maxBins)

val labelAndPreds = testData.map { point =>
  val prediction = modelEval.predict(point.features)
  (point.label, prediction)
}
val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
println("Test Error = " + testErr)
var sumP:Double = 0
var sumR:Double = 0
for(i <- 0 to 2) {
    var s1:Long = 0
    for(j <- 0 to 2)
        s1 += labelAndPreds.filter(r => r._1 == j.toDouble && r._2 == i.toDouble).count
    var s2:Long = 0
    for(j <- 0 to 2)
        s2 += labelAndPreds.filter(r => r._1 == i.toDouble && r._2 == j.toDouble).count
    val t = labelAndPreds.filter(r => r._1 == i.toDouble && r._2 == i.toDouble).count.toDouble
    val p = t / s1
    val r = t / s2
    sumP += p
    sumR += r
    println(i + " Precision: " + p + "\tRecall: " + r)
}
println("Average Precision: " + (sumP / 3))
println("Average Recall:    " + (sumR / 3))
