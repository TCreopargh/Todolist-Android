package xyz.tcreopargh.todolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.refactor.library.SmoothCheckBox;
import java.util.Calendar;
import java.util.List;

/**
 * @author TCreopargh
 */
public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.ViewHolder> implements OnClickListener {

    private List<Todo> todoList;
    private OnItemClickListener onItemClickListener = null;
    private Context context;
    private SmoothCheckBox.OnCheckedChangeListener onCheckboxClickListener = null;
    private CompoundButton.OnCheckedChangeListener onSubItemCheckboxClickListener = null;
    private OnItemClickListener onMajorClickListener = null;

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
        Calendar now = Calendar.getInstance();
        Calendar hourLater = Calendar.getInstance();
        hourLater.add(Calendar.HOUR, 1);
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
        if (todo.getDeadline() != null && todo.getDeadline().compareTo(now) < 0) {
            holder.expiredText.setVisibility(View.VISIBLE);
            holder.expiredText.setText(context.getString(R.string.expired));
            holder.expiredText.setTextColor(context.getColor(R.color.colorBlack));
        } else if (todo.getDeadline() != null && todo.getDeadline().compareTo(hourLater) < 0) {
            holder.expiredText.setVisibility(View.VISIBLE);
            holder.expiredText.setText(context.getString(R.string.expiring_soon));
            holder.expiredText.setTextColor(context.getColor(R.color.colorAccent));
        } else {
            holder.expiredText.setVisibility(View.GONE);
        }
        holder.completeBox.setChecked(todo.isCompleted());
        holder.completeBox.setOnCheckedChangeListener((checkBox, isChecked) -> {
            todo.setCompleted(isChecked);
            if (onCheckboxClickListener != null) {
                onCheckboxClickListener.onCheckedChanged(holder.completeBox, isChecked);
            }
        });
        holder.clickableBg.setOnClickListener(v -> {
            if (onMajorClickListener != null) {
                onMajorClickListener.onItemClick(v, position);
            }
        });
        List<SubItem> subItems = todo.getSubItems();
        holder.subItemsLayout.removeAllViews();
        if (subItems != null && subItems.size() > 0) {
            for (SubItem subItem : subItems) {
                View view = LayoutInflater.from(context)
                    .inflate(R.layout.sub_item_layout, holder.subItemsLayout, false);
                CheckBox checkBox = view.findViewById(R.id.subCompleted);
                checkBox.setText(subItem.getTitle());
                checkBox.setChecked(subItem.isCompleted());
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    subItem.setCompleted(isChecked);
                    if (onSubItemCheckboxClickListener != null) {
                        onSubItemCheckboxClickListener.onCheckedChanged(checkBox, isChecked);
                    }
                });
                holder.subItemsLayout.addView(view);

            }
        }
    }

    public void setOnCheckboxClickListener(SmoothCheckBox.OnCheckedChangeListener listener) {
        this.onCheckboxClickListener = listener;
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

    public void setOnSubItemCheckboxClickListener(OnCheckedChangeListener listener) {
        this.onSubItemCheckboxClickListener = listener;
    }

    public void setOnMajorClickListener(OnItemClickListener listener) {
        this.onMajorClickListener = listener;
    }

    public interface OnCheckboxClickListener {

        void onCheckedChange(View buttonView, boolean isChecked);
    }

    public interface OnItemClickListener {

        /**
         * @param view The Clicked View
         * @param position Position of view item
         */
        void onItemClick(View view, int position);
    }

    public interface OnSubItemCheckboxClickListener {

        void onCheckedChange(View buttonView, boolean isChecked);
    }

    public interface OnMajorClickListener {

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
        TextView expiredText;
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
            expiredText = v.findViewById(R.id.expiredText);
        }
    }
}
