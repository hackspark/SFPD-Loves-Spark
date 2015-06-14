
import java.io._
import scala.io.Source
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.model.DecisionTreeModel

val input = Source.fromFile("/resources/userinput.txt").getLines.next
// val input = "2015-01-01 10:00:00"
println(input)
val formatIn = new java.text.SimpleDateFormat("yyy-MM-dd HH:mm:ss")
val datetime = formatIn.parse(input)
println(datetime)
val formatDayNum = new java.text.SimpleDateFormat("u")

def toDayPeriod(dt: java.util.Date): Int = {
    val time = dt.getHours
    if (time >= 22 || time < 6) 0 // Night
    else if (time >= 6 && time < 12) 1 // Morning
    else if (time >= 12 && time < 18) 2 // Afternoon
    else 3 // Evening
}

val month = datetime.getMonth
val dayOfWeekNum = formatDayNum.format(datetime).toInt
val dayPeriod = toDayPeriod(datetime)

println(month, dayOfWeekNum, dayPeriod)

val model = DecisionTreeModel.load(sc, "/resources/model4")
var out = ""
var sum = 0
val no_box = 165
for(box <- 0 to no_box) {
    val pred = model.predict(Vectors.dense(box, 8, 2, 1)).toInt
    sum += pred
    out = out + box + "," + pred + "\n"
}
println(sum / no_box.toDouble)
// print(out)
val file = new java.io.PrintWriter(new File("/resources/useroutput.csv"))
try file.write(out) finally file.close()
