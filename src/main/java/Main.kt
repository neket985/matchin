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
import ratpack.server.RatpackServer
import java.io.File
import java.io.StringWriter
import java.util.HashMap
import com.mitchellbosecke.pebble.template.PebbleTemplate
import com.mitchellbosecke.pebble.PebbleEngine
import ratpack.handling.Context
import java.net.InetAddress


object Main {
    private val scheduler = StdSchedulerFactory().scheduler
    @JvmStatic
    fun main(args: Array<String>) {
        initScheduler()

        val serverConf = config.getConfig("server")
        val baseDir = File(serverConf.getString("baseDir"))
        logger.info("текст на русском")

        RatpackServer.start {
            it
                    .serverConfig {
                        it
                                .port(serverConf.getInt("port"))
                                .baseDir(baseDir)
                    }
                    .handlers { chain ->
                        chain
                                .get("logs") { ctx ->
                                    val filesList = File(baseDir.absolutePath + "/log")
                                            .listFiles()
                                            .map { it.name }
                                    ctx.renderPebble("index.pebble", "filesList" to filesList)
                                }
                                .prefix("logs", { nested -> nested.fileSystem("log", { it.files() }) })
                    }
        }.start()
    }

    private fun Context.renderPebble(name: String, vararg params: Pair<String, Any>) {
        val engine = PebbleEngine.Builder()
                .build()
        val compiledTemplate = engine.getTemplate("templates/$name")

        val context = HashMap<String, Any>()
        context.putAll(params)

        val writer = StringWriter()
        compiledTemplate.evaluate(writer, context)

        val output = writer.toString()

        this.response.contentType("text/html").send(output)
    }

    private fun initScheduler() {
        logger.info("Service started")
        val availHours = config.getStringList("availableHours")
        val datesOfSchedule = config.getConfig("datesOfLessons")
        for (month in 2..5) {
            val dateConf = datesOfSchedule.getConfig(month.toString())
            availHours.forEach { hour ->
                val dates = dateConf.getStringList(hour)
                if (dates.isNotEmpty()) {
                    initQuartzJob(month, dates, hour)
                }
            }
        }

        scheduler.start()
    }

    private fun initQuartzJob(month: Int, dates: List<String>, hour: String) {
        val monthExpr = Month.getByNum(month)!!.name
        val dateExpr = dates.joinToString(",")
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

    private val config = ConfigFactory.load()
    private val random = Random()
    private val logger = LoggerFactory.getLogger(Main::class.java)
}