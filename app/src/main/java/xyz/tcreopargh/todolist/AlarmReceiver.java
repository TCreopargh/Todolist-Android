package xyz.tcreopargh.todolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        Toast.makeText(context, title, Toast.LENGTH_LONG).show();
    }
}
