package models
import play.api.libs.json.Json
import play.api.libs.json.Reads._ 
import play.api.libs.json._
import play.api.libs.functional.syntax._

import ActiveRecord._
import java.util.Date
import java.text.SimpleDateFormat
/**
 * Created by kindone on 15. 1. 24..
 */

class Reminder(var date:Date, var repeatInterval:Option[Long],
                 var repeatYear:Option[Int], var repeatMonth:Option[Int],
                 var repeatWeek:Option[Int], var repeatWeekday:Option[Int],
                 var repeatDay:Option[Int]) extends Entity {
  
  def frozen = transactional {
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    Reminder.Frozen(Some(id), df.format(date), repeatInterval,
        repeatYear, repeatMonth, repeatWeek, repeatWeekday, repeatDay)
  }
}

object Reminder extends ActiveRecord[Reminder]
{
  val jsonFormat = Json.format[models.Reminder.Frozen]
  
  case class Frozen(id:Option[String], date:String, repeatInterval:Option[Long],
    repeatYear:Option[Int], repeatMonth:Option[Int], repeatWeek:Option[Int], repeatWeekday:Option[Int],
    repeatDay:Option[Int]) 
  
  def create(frozen:Frozen) = transactional {
    val dateObj = javax.xml.bind.DatatypeConverter.parseDateTime(frozen.date).getTime
    new Reminder(dateObj, frozen.repeatInterval, frozen.repeatYear, frozen.repeatMonth, 
        frozen.repeatWeek, frozen.repeatWeekday, frozen.repeatDay)
  }
  
  def update(frozen:Frozen) = transactional {
    val reminder = Reminder.find(frozen.id.get).get
    val dateObj = javax.xml.bind.DatatypeConverter.parseDateTime(frozen.date).getTime
    reminder.date = dateObj
    reminder.repeatInterval = frozen.repeatInterval
    reminder.repeatYear = frozen.repeatYear
    reminder.repeatMonth = frozen.repeatMonth
    reminder.repeatWeek = frozen.repeatWeek
    reminder.repeatWeekday = frozen.repeatWeekday
    reminder.repeatDay = frozen.repeatDay
    
    reminder
  }
}
