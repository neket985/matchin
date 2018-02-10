import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import jobs.MyJob
import okhttp3.*
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*
import org.quartz.CronExpression
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.CronTrigger
import org.quartz.JobBuilder.newJob
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory
import org.quartz.SchedulerFactory
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.JobDetailImpl


object Main {
    private val url = "https://docs.google.com/forms/d/e/1FAIpQLScdwxIecqujMM3PHEHac9jy2p-32VXwo_vdW_VE30Fat-OT_A/formResponse"
    private val scheduler = StdSchedulerFactory().scheduler
    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("Service started")
        val config = ConfigFactory.load()
        val availHours = config.getStringList("availableHours")
        val datesOfSchedule = config.getConfig("datesOfLessons")
        for (month in 2..5) {
            val dateConf = datesOfSchedule.getConfig(month.toString())
            availHours.forEach { hour ->
                val dates = dateConf.getStringList(hour)
                if(dates.isNotEmpty()) {
                    initQuartzJob(month, dates, hour)
                }
            }
        }

        scheduler.start()

    }

    private fun initQuartzJob(month: Int, dates: List<String>, hour: String) {
        val monthExpr = Month.getByNum(month)!!.name
        val dateExpr = dates.joinToString (",")
        val minute = random.nextInt(60)
        val cronExpr = "0 $minute $hour $dateExpr $monthExpr ? 2018"
        val job = newJob(MyJob::class.java)
                .withIdentity("job-$month-$hour", "group1")
                .build()

        val trigger = newTrigger()
                .withIdentity("trigger-$month-$hour", "group1")
                .withSchedule(cronSchedule(cronExpr))
                .build()

        scheduler.scheduleJob(job, trigger)

        logger.info("Job with cron expression \"$cronExpr\" initialized")
    }

    private val random = Random()
    private val logger = LoggerFactory.getLogger(Main::class.java)
}