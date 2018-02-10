package jobs

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.text.SimpleDateFormat
import java.util.*

class MyJob : Job {
    override fun execute(ctx: JobExecutionContext) {
        students.sortedBy {
            random.nextInt()
        }.forEach {
            registerAtLesson(it)
            val secondForSleap = random.nextInt(30)
            Thread.sleep(secondForSleap*1000L)
        }
    }

    companion object {
        private fun registerAtLesson(student: Config) {
            val lastName = student.getString("lastName")
            val firstName = student.getString("firstName")
            val patronymic = student.getString("patronymic")
            val group = student.getString("group")
            val lessonTitle = student.getString("lessonTitle")


            val formBody = FormBody.Builder()
                    .add("entry.466865208", lastName)
                    .add("entry.978916071", firstName)
                    .add("entry.125931493", patronymic)
                    .add("entry.2127907320", group)
                    .add("entry.1518597535", lessonTitle)
                    .add("entry.163619523", student.getString("phone"))
                    .add("entry.1361591872", formatter.format(Date()))
                    .build()
//        val response = client.newCall(
//                Request.Builder()
//                        .url(url)
//                        .post(formBody)
//                        .build()
//        ).execute()
//
//        if (response.isSuccessful) {
//            response.body()?.bytes()?.let {
//                val data = String(it)
//                logger.debug(data)
//                if (data.contains("Ответ записан")) {
//                    logger.info("Student $lastName $firstName $patronymic from $group group is REGISTERED at $lessonTitle")
//                } else {
//                    logger.error("Student $lastName $firstName $patronymic from $group group is NOT REGISTERED at $lessonTitle")
//                }
//            }
//        } else {
//            logger.error("Error while processing request\n" + response.message())
//        }
        }

        private val config = ConfigFactory.load()
        private val students = config.getConfigList("students")
        private val random = Random()
        private val formatter = SimpleDateFormat("yyyy-MM-dd")
        private val client = OkHttpClient.Builder().build()
    }
}