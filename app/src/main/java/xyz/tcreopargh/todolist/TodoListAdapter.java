package xyz.tcreopargh.todolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.refactor.library.SmoothCheckBox;
import java.util.Calendar;
import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.ViewHolder> implements OnClickListener {

    private List<Todo> todoList;
    private OnItemClickListener onItemClickListener = null;
    private Context context;

    public TodoListAdapter(List<Todo> todoList, Context context) {
        this.todoList = todoList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item_layout, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        holder.todoTitle.setText(todo.getTitle());
        if (!todo.isImportant()) {
            holder.importance.setVisibility(View.GONE);
        } else {
            holder.importance.setVisibility(View.VISIBLE);
        }
        if (!todo.isUrgent()) {
            holder.urgency.setVisibility(View.GONE);
        } else {
            holder.urgency.setVisibility(View.VISIBLE);
        }
        if (todo.getNotificationTime() == null) {
            holder.alertIcon.setVisibility(View.GONE);
            holder.alarmTime.setVisibility(View.GONE);
        } else {
            holder.alertIcon.setVisibility(View.VISIBLE);
            holder.alarmTime.setVisibility(View.VISIBLE);
            Calendar notificationTime = todo.getNotificationTime();
            String intervalText = CustomDate.getIntervalString(notificationTime, context);
            holder.alarmTime.setText(intervalText);
        }
        holder.completeBox.setChecked(todo.isCompleted());
        holder.completeBox.setOnCheckedChangeListener((checkBox, isChecked) -> todo.setCompleted(isChecked));
        List<SubItem> subItems = todo.getSubItems();
        holder.subItemsLayout.removeAllViews();
        if (subItems != null && subItems.size() > 0) {
            for (SubItem subItem : subItems) {
                View view = LayoutInflater.from(context)
                    .inflate(R.layout.sub_item_layout, holder.subItemsLayout, false);
                CheckBox checkBox = view.findViewById(R.id.subCompleted);
                checkBox.setText(subItem.getTitle());
                checkBox.setChecked(subItem.isCompleted());
                holder.subItemsLayout.addView(view);
            }
        }
        holder.clickableBg
            .setOnClickListener(v -> holder.completeBox.setChecked(!holder.completeBox.isChecked(), true));
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

    public static interface OnItemClickListener {

        void onItemClick(View view, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        SmoothCheckBox completeBox;
        ImageView alertIcon;
        TextView urgency;
        TextView importance;
        LinearLayout subItemsLayout;
        TextView todoTitle;
        TextView alarmTime;
        View clickableBg;

        public ViewHolder(@NonNull View v) {
            super(v);
            completeBox = v.findViewById(R.id.completedBox);
            alertIcon = v.findViewById(R.id.alertIcon);
            urgency = v.findViewById(R.id.urgency);
            importance = v.findViewById(R.id.importance);
            subItemsLayout = v.findViewById(R.id.subItemsLayout);
            todoTitle = v.findViewById(R.id.todoTitle);
            alarmTime = v.findViewById(R.id.alarmTime);
            clickableBg = v.findViewById(R.id.clickableBg);
        }
    }
}
