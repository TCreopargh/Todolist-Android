package xyz.tcreopargh.todolist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class SubItemsEditAdapter extends ArrayAdapter<SubItem> {

    private int resourceId;
    private List<SubItem> subItems;

    public SubItemsEditAdapter(@NonNull Context context, int resourceId, List<SubItem> subItems) {
        super(context, resourceId, subItems);
        this.resourceId = resourceId;
        this.subItems = subItems;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SubItem subItem = getItem(position);
        View view =
            convertView == null ? LayoutInflater.from(getContext()).inflate(resourceId, parent, false) : convertView;
        TextView title = view.findViewById(R.id.editSubItemTitle);
        ImageButton delete = view.findViewById(R.id.deleteSubItem);
        title.setText(subItem != null ? subItem.getTitle() : null);
        delete.setOnClickListener(v -> {
            subItems.remove(position);
            notifyDataSetChanged();
        });
        return view;
    }
}
