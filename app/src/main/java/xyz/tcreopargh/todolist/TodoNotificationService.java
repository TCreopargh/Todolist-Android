package xyz.tcreopargh.todolist;

import static androidx.core.app.NotificationCompat.PRIORITY_LOW;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author TCreopargh
 */
public class TodoNotificationService extends Service {

    public static List<Todo> todoList = new ArrayList<>();

    public TodoNotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int requestCode = 1;
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TodoNotificationService.class);
        startService(intent);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Calendar now = Calendar.getInstance();
        for (int i = 0; i < todoList.size(); i++) {
            Todo todo = todoList.get(i);
            if (todo.getNotificationTime() != null) {
                Notification notification = getNotification("TodoList_No." + String.valueOf(i), todo.getTitle(),
                    todo.getNotificationTime());
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(requestCode, notification);
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Notification getNotification(String id, String name, Calendar when) {
        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = createChannel(id, name);
        } else {
            channel = "";
        }

        return new NotificationCompat.Builder(this, channel)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle(getString(R.string.app_name))
            .setContentText(name).setPriority(PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setWhen(when.getTimeInMillis())
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build();

    }

    @NonNull
    @RequiresApi(api = VERSION_CODES.O)
    private synchronized String createChannel(String id, String name) {
        NotificationManager mNotificationManager = (NotificationManager) this
            .getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel(id, name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(getColor(R.color.colorAccent));
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return id;
    }
}
