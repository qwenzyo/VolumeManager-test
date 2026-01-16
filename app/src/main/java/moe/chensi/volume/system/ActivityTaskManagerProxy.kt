package moe.chensi.volume.system

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.IBinder
import org.joor.Reflect
import rikka.shizuku.ShizukuBinderWrapper

class ActivityTaskManagerProxy(context: Context) {
    @SuppressLint("WrongConstant")
    val activityTaskManager: Reflect =
        context.getSystemService(Context.ACTIVITY_TASK_SERVICE).run(Reflect::on)

    init {
        val service = activityTaskManager.call("getService")
        val remote = service.get<IBinder>("mRemote")
        val wrapper = remote as? ShizukuBinderWrapper ?: ShizukuBinderWrapper(remote)
        service.set("mRemote", wrapper)
    }

    fun getForegroundTask(): String? {
        val tasks = activityTaskManager.call("getTasks", 1)
            .get<List<ActivityManager.RunningTaskInfo>>()
        if (tasks.isEmpty()) {
            return null
        }

        val taskInfo = tasks[0]
        val topActivityInfo = Reflect.on(taskInfo).get<ActivityInfo?>("topActivityInfo")
        return topActivityInfo?.packageName
    }
}