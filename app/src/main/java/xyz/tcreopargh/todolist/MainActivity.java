package xyz.tcreopargh.todolist;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import me.gujun.android.taggroup.TagGroup;

/**
 * @author TCreopargh
 * @date 2019/02/01
 */
public final class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    public static final int LESS = -1;
    public static final int MORE = 1;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    List<Todo> todoList = new ArrayList<>();
    TodoListAdapter todoListAdapter;

    @BindView(R.id.todoList)
    SwipeMenuRecyclerView recyclerView;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    TextView notificationTimeText;
    TextView startTimeText;
    TextView deadlineText;

    private static int sort(Todo a, Todo b) {
        Calendar hourLater = Calendar.getInstance();

        hourLater.add(Calendar.HOUR, 1);
        Calendar now = Calendar.getInstance();
        if (a.isCompleted() && !b.isCompleted()) {
            return MORE;
        } else if (b.isCompleted() && !a.isCompleted()) {
            return LESS;
        } else if (a.getStartTime() != null
            && b.getStartTime() != null
            && a.getStartTime().compareTo(now) > 0
            && b.getStartTime().compareTo(now) <= 0) {
            return MORE;
        } else if (a.getStartTime() != null
            && b.getStartTime() != null
            && b.getStartTime().compareTo(now) > 0
            && a.getStartTime().compareTo(now) <= 0) {
            return LESS;
        } else if (a.getDeadline() != null
            && b.getDeadline() != null
            && a.getDeadline().compareTo(hourLater) < 0
            && b.getDeadline().compareTo(hourLater) >= 0) {
            return LESS;
        } else if (a.getDeadline() != null
            && b.getDeadline() != null
            && a.getDeadline().compareTo(hourLater) >= 0
            && b.getDeadline().compareTo(hourLater) < 0) {
            return MORE;
        } else if (a.isUrgent() && a.isImportant() && !(b.isUrgent() && b.isImportant())) {
            return LESS;
        } else if (b.isUrgent() && b.isImportant() && !(a.isUrgent() && a.isImportant())) {
            return MORE;
        } else if (a.isUrgent() && !b.isUrgent()) {
            return LESS;
        } else if (b.isUrgent() && !a.isUrgent()) {
            return MORE;
        } else if (a.isImportant() && !b.isImportant()) {
            return LESS;
        } else if (b.isImportant() && !a.isImportant()) {
            return MORE;
        } else {
            return a.getTitle().compareTo(b.getTitle());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        swipeRefreshLayout.setOnRefreshListener(
            () ->
                new Thread(
                    () -> {
                        loadTodoList();
                        swipeRefreshLayout.setRefreshing(false);
                    })
                    .run());

        Intent intent = new Intent(this, TodoNotificationService.class);
        startService(intent);

        fab.setOnClickListener(view -> showAddTodoDialog());

        ActionBarDrawerToggle toggle =
            new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        recyclerView.setSwipeMenuCreator(
            (leftMenu, rightMenu, position) -> {
                SwipeMenuItem delete =
                    new SwipeMenuItem(coordinatorLayout.getContext())
                        .setBackgroundColorResource(R.color.colorRed)
                        .setImage(R.drawable.ic_delete_white_24dp)
                        .setHeight(MATCH_PARENT)
                        .setWidth(200);
                leftMenu.addMenuItem(delete);
            });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initList();
        todoListAdapter = new TodoListAdapter(todoList, this);
        todoListAdapter.setOnCheckboxClickListener((buttonView, isChecked) -> saveTodoList());
        todoListAdapter.setOnSubItemCheckboxClickListener((buttonView, isChecked) -> saveTodoList());

        recyclerView.setSwipeMenuItemClickListener(
            (menuBridge, position) -> {
                menuBridge.closeMenu();
                int direction = menuBridge.getDirection();
                if (direction == SwipeMenuRecyclerView.LEFT_DIRECTION && menuBridge.getPosition() == 0) {
                    Todo tempTodo = todoList.get(position);
                    todoList.remove(position);
                    todoListAdapter.notifyItemRemoved(position);
                    saveTodoList();
                    Snackbar.make(coordinatorLayout, R.string.deleted, Snackbar.LENGTH_LONG)
                        .setAction(
                            R.string.undo,
                            v -> {
                                todoList.add(position, tempTodo);
                                todoListAdapter.notifyItemInserted(position);
                                saveTodoList();
                            })
                        .show();
                }
            });
        todoListAdapter.setOnMajorClickListener(
            (view, position) -> {
                showEditTodoDialog(position);
            });
        recyclerView.setAdapter(todoListAdapter);
        TodoNotificationService.todoList = todoList;
    }

    private void showAddTodoDialog() {
        Builder builder = new Builder(this);
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_todo_dialog, null);
        Button showMore = dialogView.findViewById(R.id.moreOptions);
        EditText titleBox = dialogView.findViewById(R.id.editTodoTitle);
        LinearLayout moreOptionsLayout = dialogView.findViewById(R.id.moreOptionsLayout);
        View metaView =
            LayoutInflater.from(this).inflate(R.layout.add_todo_meta, moreOptionsLayout, false);
        moreOptionsLayout.addView(metaView);
        CheckBox importanceBox = dialogView.findViewById(R.id.setImportant);
        CheckBox urgencyBox = dialogView.findViewById(R.id.setUrgent);
        moreOptionsLayout.setVisibility(View.GONE);
        Calendar[] notificationTime = {null};
        Calendar[] startTime = {null};
        Calendar[] deadline = {null};
        showMore.setOnClickListener(
            v -> {
                boolean isExpanded = moreOptionsLayout.getVisibility() == View.VISIBLE;
                int img0 = R.drawable.ic_arrow_drop_down_white_24dp;
                int img1 = R.drawable.ic_arrow_drop_up_white_24dp;
                showMore.setCompoundDrawablesWithIntrinsicBounds(isExpanded ? img0 : img1, 0, 0, 0);
                showMore.setCompoundDrawablePadding(8);
                showMore.setText(isExpanded ? R.string.advanced_settings : R.string.less_options);
                moreOptionsLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            });
        notificationTimeText = metaView.findViewById(R.id.notificationTimeText);
        startTimeText = metaView.findViewById(R.id.startTimeText);
        deadlineText = metaView.findViewById(R.id.deadlineText);
        startTimeText.setText(R.string.not_set);
        notificationTimeText.setText(R.string.not_set);
        deadlineText.setText(R.string.not_set);
        ImageButton setNotificationTime = metaView.findViewById(R.id.editNotificationTime);
        ImageButton setStartTime = metaView.findViewById(R.id.editStartTime);
        ImageButton setDeadline = metaView.findViewById(R.id.editDeadline);
        ImageButton addSubItem = metaView.findViewById(R.id.addSubItems);
        TagGroup tagGroup = metaView.findViewById(R.id.tagGroup);
        @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"}) // Reserved for initial list
            List<String> stringList = new ArrayList<>();
        tagGroup.setTags(stringList);
        titleBox.requestFocus();
        setNotificationTime.setOnClickListener(v -> getDateTime(notificationTime, 0));
        setStartTime.setOnClickListener(v -> getDateTime(startTime, 1));
        setDeadline.setOnClickListener(v -> getDateTime(deadline, 2));
        addSubItem.setOnClickListener(
            v -> {
                @SuppressLint("InflateParams")
                View addSubItemView =
                    LayoutInflater.from(builder.getContext()).inflate(R.layout.text_edit_dialog, null);
                EditText subItemBox = addSubItemView.findViewById(R.id.textEditBox);
                subItemBox.setHint("支持批量添加，一行一个");
                subItemBox.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                subItemBox.setGravity(Gravity.TOP);
                subItemBox.setSingleLine(false);
                subItemBox.setMaxLines(6);
                new Builder(builder.getContext())
                    .setView(addSubItemView)
                    .setTitle("添加子项目")
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(
                        R.string.confirm,
                        (dialog, which) -> {
                            String input = subItemBox.getText().toString();
                            String[] addItems = input.split("[\\r\\n]+");
                            String[] strings = tagGroup.getTags();
                            List<SubItem> list = new ArrayList<>();
                            for (String string : strings) {
                                list.add(new SubItem(string));
                            }
                            for (String addItem : addItems) {
                                list.add(new SubItem(addItem));
                            }
                            List<String> stringList1 = new ArrayList<>();
                            for (SubItem subItem : list) {
                                stringList1.add(subItem.getTitle());
                            }
                            tagGroup.setTags(stringList1);
                            dialog.dismiss();
                        })
                    .create()
                    .show();
            });
        builder
            .setTitle(R.string.new_todo)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
            .setPositiveButton(
                R.string.confirm,
                (dialog, which) -> {
                    Todo todo = new Todo(titleBox.getText().toString());
                    if (notificationTime[0] != null) {
                        todo.setNotificationTime(notificationTime[0]);
                        notificationTime[0] = null;
                    }
                    if (startTime[0] != null) {
                        todo.setStartTime(startTime[0]);
                        startTime[0] = null;
                    }
                    if (deadline[0] != null) {
                        todo.setDeadline(deadline[0]);
                        deadline[0] = null;
                    }
                    todo.setImportant(importanceBox.isChecked());
                    todo.setUrgent(urgencyBox.isChecked());
                    String[] strings = tagGroup.getTags();
                    List<SubItem> finalList = new ArrayList<>();
                    for (String string : strings) {
                        finalList.add(new SubItem(string));
                    }
                    todo.setSubItems(finalList);
                    // Todo
                    todoList.add(todo);
                    Collections.sort(todoList, MainActivity::sort);
                    saveTodoList();
                    todoListAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                })
            .create()
            .show();
    }

    private void showEditTodoDialog(int position) {
        Todo currentTodo = todoList.get(position);
        Builder builder = new Builder(this);
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_todo_dialog, null);
        Button showMore = dialogView.findViewById(R.id.moreOptions);
        EditText titleBox = dialogView.findViewById(R.id.editTodoTitle);
        LinearLayout moreOptionsLayout = dialogView.findViewById(R.id.moreOptionsLayout);
        View metaView =
            LayoutInflater.from(this).inflate(R.layout.add_todo_meta, moreOptionsLayout, false);
        moreOptionsLayout.addView(metaView);
        CheckBox importanceBox = dialogView.findViewById(R.id.setImportant);
        CheckBox urgencyBox = dialogView.findViewById(R.id.setUrgent);
        moreOptionsLayout.setVisibility(View.GONE);
        Calendar[] notificationTime = {null};
        Calendar[] startTime = {null};
        Calendar[] deadline = {null};

        importanceBox.setChecked(currentTodo.isImportant());
        urgencyBox.setChecked(currentTodo.isUrgent());
        titleBox.setText(currentTodo.getTitle());
        notificationTime[0] = currentTodo.getNotificationTime();
        startTime[0] = currentTodo.getStartTime();
        deadline[0] = currentTodo.getDeadline();

        showMore.setOnClickListener(
            v -> {
                boolean isExpanded = moreOptionsLayout.getVisibility() == View.VISIBLE;
                int img0 = R.drawable.ic_arrow_drop_down_white_24dp;
                int img1 = R.drawable.ic_arrow_drop_up_white_24dp;
                showMore.setCompoundDrawablesWithIntrinsicBounds(isExpanded ? img0 : img1, 0, 0, 0);
                showMore.setCompoundDrawablePadding(8);
                showMore.setText(isExpanded ? R.string.advanced_settings : R.string.less_options);
                moreOptionsLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
            });
        notificationTimeText = metaView.findViewById(R.id.notificationTimeText);
        startTimeText = metaView.findViewById(R.id.startTimeText);
        deadlineText = metaView.findViewById(R.id.deadlineText);
        if (notificationTime[0] == null) {
            notificationTimeText.setText(R.string.not_set);
        } else {
            notificationTimeText.setText(CustomDate.getDateString(notificationTime[0], this));
        }
        if (startTime[0] == null) {
            startTimeText.setText(R.string.not_set);
        } else {
            startTimeText.setText(CustomDate.getDateString(startTime[0], this));
        }
        if (deadline[0] == null) {
            deadlineText.setText(R.string.not_set);
        } else {
            deadlineText.setText(CustomDate.getDateString(deadline[0], this));
        }
        ImageButton setNotificationTime = metaView.findViewById(R.id.editNotificationTime);
        ImageButton setStartTime = metaView.findViewById(R.id.editStartTime);
        ImageButton setDeadline = metaView.findViewById(R.id.editDeadline);
        ImageButton addSubItem = metaView.findViewById(R.id.addSubItems);
        TagGroup tagGroup = metaView.findViewById(R.id.tagGroup);
        List<String> stringList = new ArrayList<>();
        for (SubItem subItem : currentTodo.getSubItems()) {
            stringList.add(subItem.getTitle());
        }
        tagGroup.setTags(stringList);
        titleBox.requestFocus();
        setNotificationTime.setOnClickListener(v -> getDateTime(notificationTime, 0));
        setStartTime.setOnClickListener(v -> getDateTime(startTime, 1));
        setDeadline.setOnClickListener(v -> getDateTime(deadline, 2));
        addSubItem.setOnClickListener(
            v -> {
                @SuppressLint("InflateParams")
                View addSubItemView =
                    LayoutInflater.from(builder.getContext()).inflate(R.layout.text_edit_dialog, null);
                EditText subItemBox = addSubItemView.findViewById(R.id.textEditBox);
                subItemBox.setHint("支持批量添加，一行一个");
                subItemBox.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                subItemBox.setGravity(Gravity.TOP);
                subItemBox.setSingleLine(false);
                subItemBox.setMaxLines(6);
                new Builder(builder.getContext())
                    .setView(addSubItemView)
                    .setTitle("添加子项目")
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(
                        R.string.confirm,
                        (dialog, which) -> {
                            String input = subItemBox.getText().toString();
                            String[] addItems = input.split("[\\r\\n]+");
                            String[] strings = tagGroup.getTags();
                            List<SubItem> list = new ArrayList<>();
                            for (String string : strings) {
                                list.add(new SubItem(string));
                            }
                            for (String addItem : addItems) {
                                list.add(new SubItem(addItem));
                            }
                            List<String> stringList1 = new ArrayList<>();
                            for (SubItem subItem : list) {
                                stringList1.add(subItem.getTitle());
                            }
                            tagGroup.setTags(stringList1);
                            dialog.dismiss();
                        })
                    .create()
                    .show();
            });
        builder
            .setTitle(R.string.edit_todo)
            .setView(dialogView)
            .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
            .setPositiveButton(
                R.string.confirm,
                (dialog, which) -> {
                    Todo todo = new Todo(titleBox.getText().toString());
                    if (notificationTime[0] != null) {
                        todo.setNotificationTime(notificationTime[0]);
                        notificationTime[0] = null;
                    }
                    if (startTime[0] != null) {
                        todo.setStartTime(startTime[0]);
                        startTime[0] = null;
                    }
                    if (deadline[0] != null) {
                        todo.setDeadline(deadline[0]);
                        deadline[0] = null;
                    }
                    todo.setImportant(importanceBox.isChecked());
                    todo.setUrgent(urgencyBox.isChecked());
                    String[] strings = tagGroup.getTags();
                    List<SubItem> finalList = new ArrayList<>();
                    for (String string : strings) {
                        SubItem subItem = new SubItem(string);
                        finalList.add(subItem);
                    }
                    todo.setSubItems(finalList);
                    // Todo
                    todo.setCompleted(currentTodo.isCompleted());
                    todoList.set(position, todo);
                    Collections.sort(todoList, MainActivity::sort);
                    saveTodoList();
                    todoListAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                })
            .create()
            .show();
    }

    private void getDateTime(final Calendar[] calendar, int requestCode) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog =
            DatePickerDialog.newInstance(
                (view1, year, monthOfYear, dayOfMonth) -> {
                    TimePickerDialog dialog1 =
                        TimePickerDialog.newInstance(
                            (view2, hourOfDay, minute, second) -> {
                                Calendar time = Calendar.getInstance();
                                time.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                                calendar[0] = time;
                                switch (requestCode) {
                                    case 0:
                                        notificationTimeText.setText(CustomDate.getDateString(time, this));
                                        break;
                                    case 1:
                                        startTimeText.setText(CustomDate.getDateString(time, this));
                                        break;
                                    case 2:
                                        deadlineText.setText(CustomDate.getDateString(time, this));
                                        break;
                                    default:
                                }
                                // Todo
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            DateFormat.is24HourFormat(this));
                    if (year == now.get(Calendar.YEAR)
                        && monthOfYear == now.get(Calendar.MONTH)
                        && dayOfMonth == now.get(Calendar.DAY_OF_MONTH)) {
                        dialog1.setMinTime(
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            now.get(Calendar.SECOND));
                    }
                    dialog1.show(getSupportFragmentManager(), "TimePickerDialog");
                },
                // Initial year selection
                now.get(Calendar.YEAR),
                // Initial month selection
                now.get(Calendar.MONTH),
                // Inital day selection
                now.get(Calendar.DAY_OF_MONTH));
        dialog.setMinDate(now);
        dialog.show(getSupportFragmentManager(), "DatePickerDialog");
    }

    private void initList() {
        SharedPreferences sharedPreferences = getSharedPreferences("todo_list", MODE_PRIVATE);
        String listData = sharedPreferences.getString("todo_list", null);

        if (listData != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Todo>>() {
            }.getType();
            todoList = new ArrayList<>();
            todoList = gson.fromJson(listData, type);
        }
        Collections.sort(todoList, MainActivity::sort);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadTodoList() {
        SharedPreferences sharedPreferences = getSharedPreferences("todo_list", MODE_PRIVATE);
        String listData = sharedPreferences.getString("todo_list", null);

        if (listData != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Todo>>() {
            }.getType();
            todoList = new ArrayList<>();
            todoList = gson.fromJson(listData, type);
        }
        Collections.sort(todoList, MainActivity::sort);
        todoListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveTodoList();
    }

    private void saveTodoList() {
        SharedPreferences sharedPreferences = getSharedPreferences("todo_list", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(todoList);
        sharedPreferences.edit().putString("todo_list", json).apply();
        TodoNotificationService.todoList = todoList;
    }
}
