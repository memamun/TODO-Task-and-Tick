package com.example.exam;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.google.android.material.snackbar.Snackbar;

import android.app.DatePickerDialog;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.Calendar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.app.NotificationChannel;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.*;
import java.util.stream.Collectors;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.search.SearchBar;
import java.util.Arrays;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import java.util.Locale;
import java.util.Comparator;
import android.content.BroadcastReceiver;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.Toolbar;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.core.content.FileProvider;
import android.widget.ImageView;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.widget.Button;
import androidx.recyclerview.widget.DividerItemDecoration;
import android.graphics.Rect;
import androidx.appcompat.view.ActionMode;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskContextMenuListener {
    private TaskAdapter adapter;
    private List<Task> taskList;
    private List<Task> displayList = new ArrayList<>();
    private SharedPreferences prefs;
    private final Gson gson = new Gson();
    private static final String TASKS_KEY = "tasks";
    private TextView emptyState;
    private View rootView;
    private Task recentlyDeletedTask;
    private int recentlyDeletedTaskPosition;
    private String[] CATEGORIES;
    private String[] PRIORITIES;
    private EditText searchInput;
    private TabLayout tabLayout;
    private final List<Task> filteredList = new ArrayList<>();
    private String filterCategory = null;
    private String filterPriority = null;
    private String filterStatus = null;
    private final List<Integer> selectedPositions = new ArrayList<>();
    private static final String CHANNEL_ID = "todo_channel";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;
    private static final String PREF_KEY_LANGUAGE = "language";
    private static final String PREF_KEY_THEME = "theme";
    private static final String LANGUAGE_EN = "en";
    private static final String LANGUAGE_BN = "bn";
    private ImageView notificationIcon;
    private String currentLanguage;
    private ActionMode actionMode;
    
    // Activity Result Launchers to replace deprecated startActivityForResult
    private final ActivityResultLauncher<Intent> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                        if (os != null) {
                            os.write(gson.toJson(taskList).getBytes());
                            Toast.makeText(this, "Exported!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) { 
                        Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show(); 
                    }
                }
            });
            
    private final ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    try (InputStream is = getContentResolver().openInputStream(uri)) {
                        String json = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
                        Type type = new com.google.gson.reflect.TypeToken<ArrayList<Task>>(){}.getType();
                        List<Task> imported = gson.fromJson(json, type);
                        if (imported != null) { 
                            taskList.clear(); 
                            taskList.addAll(imported); 
                            onTasksChanged(); 
                        }
                        Toast.makeText(this, "Imported!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) { 
                        Toast.makeText(this, "Import failed", Toast.LENGTH_SHORT).show(); 
                    }
                }
            });

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_multi_select, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Optionally update menu items here
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete_selected) {
                deleteSelectedTasks();
                mode.finish();
                return true;
            } else if (id == R.id.action_mark_done) {
                markSelectedDone();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            disableMultiSelect();
            actionMode = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("todo_prefs", MODE_PRIVATE);
        // Always follow system theme
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        // Restore language
        String currentLanguage = prefs.getString(PREF_KEY_LANGUAGE, Locale.getDefault().getLanguage());
        setLocale(currentLanguage);
        Log.d("TODO_APP", "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Use standard ImageView for theme toggle, notification, and language
        notificationIcon = findViewById(R.id.notificationIcon);
        notificationIcon.setOnClickListener(v -> showNotificationDialog());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        emptyState = findViewById(R.id.emptyState);
        rootView = findViewById(android.R.id.content);
        searchInput = findViewById(R.id.searchInput);
        tabLayout = findViewById(R.id.tabLayout);
        
        taskList = loadTasks();
        adapter = new TaskAdapter(displayList, this::onTasksChanged, this::onTaskClicked);
        adapter.setOnTaskContextMenuListener(this);
        adapter.setOnSelectionChangedListener(this::updateActionModeTitle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Add spacing between items for better visual separation
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(8));

        // Now it's safe to update the notification badge
        updateNotificationBadge();

        // Reset filters and search after theme change
        if (searchInput != null) searchInput.setText("");
        filterCategory = null;
        filterPriority = null;
        filterStatus = null;
        if (tabLayout != null && tabLayout.getTabCount() > 0) tabLayout.getTabAt(0).select();

        filterTasks(); // Now shows all tasks

        updateEmptyState();
        setupSearch();

        // Localized categories and priorities
        CATEGORIES = new String[] {
            getString(R.string.category_work),
            getString(R.string.category_personal),
            getString(R.string.category_urgent),
            getString(R.string.category_shopping),
            getString(R.string.category_other)
        };
        PRIORITIES = new String[] {
            getString(R.string.priority_high),
            getString(R.string.priority_medium),
            getString(R.string.priority_low)
        };

        setupFilterTabs();

        // Sort by priority by default
        sortTasksByPriority();

        fabAdd.setOnClickListener(v -> showAddTaskDialog());

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position < 0 || position >= displayList.size()) return; // Safety check

                Task removedTask = displayList.get(position);
                recentlyDeletedTask = removedTask;
                recentlyDeletedTaskPosition = position;

                // Remove from the master list
                int indexInTaskList = taskList.indexOf(removedTask);
                if (indexInTaskList != -1) {
                    taskList.remove(indexInTaskList);
                }
                saveTasks(); // Ensure deletion is saved

                filterTasks(); // This will update displayList and notify the adapter

                showUndoSnackbar();
                updateNotificationBadge(); // Ensure notification icon updates after swipe delete
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);

        createNotificationChannel();

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Now reset search/filter and call filterTasks() at the very end:
        searchInput.setText("");
        filterCategory = null;
        filterPriority = null;
        filterStatus = null;
        if (tabLayout.getTabCount() > 0) tabLayout.getTabAt(0).select();
        filterTasks();
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ExamDialog);
        builder.setTitle(getString(R.string.dialog_add_task));
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_task, null);
        
        // Find views
        final com.google.android.material.textfield.TextInputEditText input = dialogView.findViewById(R.id.editTaskText);
        final TextView dueDateView = dialogView.findViewById(R.id.dueDateView);
        final Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        final Spinner prioritySpinner = dialogView.findViewById(R.id.prioritySpinner);
        
        // Set up spinners
        ArrayAdapter<String> adapterCat = new ArrayAdapter<>(this, R.layout.spinner_item, CATEGORIES);
        adapterCat.setDropDownViewResource(R.layout.spinner_dropdown_item);
        
        ArrayAdapter<String> adapterPri = new ArrayAdapter<>(this, R.layout.spinner_item, PRIORITIES);
        adapterPri.setDropDownViewResource(R.layout.spinner_dropdown_item);
        
        categorySpinner.setAdapter(adapterCat);
        prioritySpinner.setAdapter(adapterPri);
        
        // Set up date picker
        final Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {null};
        dueDateView.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, y, m, d) -> {
                String dateStr = formatDate(y, m, d);
                dueDateView.setText(dateStr);
                selectedDate[0] = dateStr;
            }, year, month, day);
            
            // Set the locale to ensure dialog is in the correct language
            Context context = datePickerDialog.getContext();
            Resources resources = context.getResources();
            Configuration config = resources.getConfiguration();
            Locale locale = new Locale(currentLanguage);
            config.setLocale(locale);
            context.createConfigurationContext(config);
            
            datePickerDialog.show();
        });
        
        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.dialog_add), (dialog, which) -> {
            String taskText = input.getText().toString().trim();
            String dueDate = selectedDate[0];
            String category = categorySpinner.getSelectedItem() != null ? 
                    categorySpinner.getSelectedItem().toString() : null;
            String priority = prioritySpinner.getSelectedItem() != null ? 
                    prioritySpinner.getSelectedItem().toString() : "Medium";
            if (!taskText.isEmpty()) {
                Task newTask = new Task(taskText, false, dueDate, category, priority);
                Log.d("TODO_APP", "Task created: " + newTask.getText() + ", due: " + dueDate + ", category: " + category + ", priority: " + priority);
                taskList.add(newTask);
                sortTasksByPriority();
                int newPosition = taskList.indexOf(taskList.stream()
                        .filter(t -> t.getText().equals(taskText) && 
                                (t.getDueDate() == null ? dueDate == null : t.getDueDate().equals(dueDate)))
                        .findFirst()
                        .orElse(null));
                Log.d("TODO_APP", "New task position after sort: " + newPosition);
                saveTasks();
                filterTasks();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        input.requestFocus();
    }

    private void showEditTaskDialog(int position) {
        Task task = taskList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ExamDialog);
        builder.setTitle(getString(R.string.dialog_edit_task));
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_task, null);
        
        // Find views
        final com.google.android.material.textfield.TextInputEditText input = dialogView.findViewById(R.id.editTaskText);
        final TextView dueDateView = dialogView.findViewById(R.id.dueDateView);
        final Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        final Spinner prioritySpinner = dialogView.findViewById(R.id.prioritySpinner);
        
        // Set up spinners
        ArrayAdapter<String> adapterCat = new ArrayAdapter<>(this, R.layout.spinner_item, CATEGORIES);
        adapterCat.setDropDownViewResource(R.layout.spinner_dropdown_item);
        
        ArrayAdapter<String> adapterPri = new ArrayAdapter<>(this, R.layout.spinner_item, PRIORITIES);
        adapterPri.setDropDownViewResource(R.layout.spinner_dropdown_item);
        
        categorySpinner.setAdapter(adapterCat);
        prioritySpinner.setAdapter(adapterPri);
        
        // Set existing values
        input.setText(task.getText());
        if (task.getDueDate() != null) {
            dueDateView.setText(task.getDueDate());
        }
        if (task.getCategory() != null) {
            int idx = Arrays.asList(CATEGORIES).indexOf(task.getCategory());
            if (idx >= 0) categorySpinner.setSelection(idx);
        }
        if (task.getPriority() != null) {
            int idx = Arrays.asList(PRIORITIES).indexOf(task.getPriority());
            if (idx >= 0) prioritySpinner.setSelection(idx);
        }
        
        // Set up date picker
        final Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {task.getDueDate()};
        dueDateView.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, y, m, d) -> {
                String dateStr = formatDate(y, m, d);
                dueDateView.setText(dateStr);
                selectedDate[0] = dateStr;
            }, year, month, day);
            
            // Set the locale to ensure dialog is in the correct language
            Context context = datePickerDialog.getContext();
            Resources resources = context.getResources();
            Configuration config = resources.getConfiguration();
            Locale locale = new Locale(currentLanguage);
            config.setLocale(locale);
            context.createConfigurationContext(config);
            
            datePickerDialog.show();
        });
        
        builder.setView(dialogView);
        builder.setPositiveButton(getString(R.string.dialog_save), (dialog, which) -> {
            String taskText = input.getText().toString().trim();
            String dueDate = selectedDate[0];
            String category = categorySpinner.getSelectedItem() != null ? 
                    categorySpinner.getSelectedItem().toString() : null;
            String priority = prioritySpinner.getSelectedItem() != null ? 
                    prioritySpinner.getSelectedItem().toString() : "Medium";
            if (!taskText.isEmpty()) {
                task.setText(taskText);
                task.setDueDate(dueDate);
                task.setCategory(category);
                task.setPriority(priority);
                
                // Sort to update positions
                sortTasksByPriority();
                int newPosition = taskList.indexOf(task);
                
                // Use specific notification method
                if (position == newPosition) {
                    adapter.notifyItemChanged(newPosition);
                } else {
                    // Handle moved item
                    adapter.notifyItemMoved(position, newPosition);
                    adapter.notifyItemChanged(newPosition);
                }
                
                onTasksChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        input.requestFocus();
    }

    private void showUndoSnackbar() {
        Snackbar.make(rootView, getString(R.string.dialog_task_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.dialog_undo), v -> {
                    // Add back to the master list at the correct position
                    if (recentlyDeletedTaskPosition >= 0 && recentlyDeletedTaskPosition <= taskList.size()) {
                        taskList.add(recentlyDeletedTaskPosition, recentlyDeletedTask);
                    } else {
                        taskList.add(recentlyDeletedTask); // fallback
                    }
                    saveTasks(); // Ensure undo is saved
                    filterTasks(); // This will update displayList and notify the adapter
                }).show();
    }

    private void onTasksChanged() {
        // Only save tasks if this is a result of a user action (add, delete, edit, undo, bulk)
        saveTasks();
        updateEmptyState();
        filterTasks();
        updateNotificationBadge();
        
        adapter.notifyDataSetChanged();
    }

    private void updateEmptyState() {
        View emptyStateContainer = findViewById(R.id.emptyStateContainer);
        if (displayList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    private void onTaskClicked(int position) {
        showEditTaskDialog(position);
    }

    private void saveTasks() {
        if (taskList == null) {
            Log.e("TODO_APP", "saveTasks() called with null taskList!");
            return;
        }
        String json = gson.toJson(taskList);
        Log.d("TODO_APP", "Saving tasks: " + json + ", size: " + taskList.size());
        prefs.edit().putString(TASKS_KEY, json).apply();
    }

    private List<Task> loadTasks() {
        String json = prefs.getString(TASKS_KEY, null);
        Log.d("TODO_APP", "Loading tasks: " + json);
        if (json == null || json.isEmpty() || json.equals("null")) {
            Log.d("TODO_APP", "No tasks found in SharedPreferences, returning empty list");
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Task>>(){}.getType();
        List<Task> loaded = gson.fromJson(json, type);
        Log.d("TODO_APP", "Loaded " + (loaded != null ? loaded.size() : 0) + " tasks from SharedPreferences");
        return loaded != null ? loaded : new ArrayList<>();
    }

    private void sortTasksByPriority() {
        taskList.sort(Comparator.comparingInt(a -> getPriorityValue(a.getPriority())));
    }

    private int getPriorityValue(String priority) {
        if (priority == null) return 2;
        switch (priority) {
            case "High": return 0;
            case "Medium": return 1;
            case "Low": 
            default: return 2;
        }
    }

    private void setupSearch() {
        // Live filtering as you type
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupFilterTabs() {
        // Add tabs for filter categories
        TabLayout.Tab allTab = tabLayout.newTab().setText(getString(R.string.filter_all));
        tabLayout.addTab(allTab);
        
        // Add category tabs
        for (String category : CATEGORIES) {
            tabLayout.addTab(tabLayout.newTab().setText(category));
        }
        
        // Add priority tabs
        for (String priority : PRIORITIES) {
            tabLayout.addTab(tabLayout.newTab().setText(priority));
        }
        
        // Add status tabs
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.filter_done)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.filter_active)));
        
        // Set listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab == null) return;
                String filter = tab.getText() != null ? tab.getText().toString() : "";
                if (filter.equals(getString(R.string.filter_all))) {
                    filterCategory = null;
                    filterPriority = null;
                    filterStatus = null;
                } else if (Arrays.asList(CATEGORIES).contains(filter)) {
                    filterCategory = filter;
                    filterPriority = null;
                    filterStatus = null;
                } else if (Arrays.asList(PRIORITIES).contains(filter)) {
                    filterCategory = null;
                    filterPriority = filter;
                    filterStatus = null;
                } else if (filter.equals(getString(R.string.filter_done))) {
                    filterCategory = null;
                    filterPriority = null;
                    filterStatus = "done";
                } else if (filter.equals(getString(R.string.filter_active))) {
                    filterCategory = null;
                    filterPriority = null;
                    filterStatus = "active";
                }
                filterTasks();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Required implementation
            }
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Clear filter when tab is tapped again
                if (tab != null && tab.getText() != null && !tab.getText().toString().equals(getString(R.string.filter_all))) {
                    TabLayout.Tab allTab = tabLayout.getTabAt(0);
                    if (allTab != null) {
                        allTab.select();
                    }
                }
            }
        });
    }

    private void filterTasks() {
        CharSequence searchBarText = searchInput.getText();
        String query = searchBarText != null ? searchBarText.toString().toLowerCase() : "";
        displayList.clear();
        for (Task t : taskList) {
            boolean match = (query.isEmpty() || (t.getText() != null && t.getText().toLowerCase().contains(query)));
            if (filterCategory != null) match &= filterCategory.equals(t.getCategory());
            if (filterPriority != null) match &= filterPriority.equals(t.getPriority());
            if (filterStatus != null) {
                if (filterStatus.equals("done")) match &= t.isDone();
                else if (filterStatus.equals("active")) match &= !t.isDone();
            }
            if (match) displayList.add(t);
        }
        Log.d("TODO_APP", "filterTasks: taskList size=" + taskList.size() + ", displayList size=" + displayList.size() + ", query='" + query + "', filterCategory=" + filterCategory + ", filterPriority=" + filterPriority + ", filterStatus=" + filterStatus);
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }
    
    private void updateAdapterWithList(List<Task> newList) {
        displayList.clear();
        displayList.addAll(newList);
        adapter.notifyDataSetChanged();
    }

    // Bulk actions
    private void updateActionModeTitle() {
        if (actionMode != null) {
            int count = adapter.getSelectedTasks().size();
            actionMode.setTitle(count + " selected");
        }
    }

    private void enableMultiSelect() {
        adapter.setMultiSelectMode(true);
        selectedPositions.clear();
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        updateActionModeTitle();
        Toast.makeText(this, "Multi-select enabled. Tap tasks to select.", Toast.LENGTH_SHORT).show();
    }

    private void disableMultiSelect() {
        adapter.setMultiSelectMode(false);
        selectedPositions.clear();
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
        // Use more specific notification instead of notifyDataSetChanged
        for (int i = 0; i < adapter.getItemCount(); i++) {
            adapter.notifyItemChanged(i);
        }
    }

    // Export/Import
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_privacy_policy) {
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
            return true;
        } else if (id == R.id.action_developer_info) {
            startActivity(new Intent(this, DeveloperInfoActivity.class));
            return true;
        } else if (id == R.id.action_share_app) {
            shareApk();
            return true;
        } else if (id == R.id.action_export) {
            exportTasks();
            return true;
        } else if (id == R.id.action_import) {
            importTasks();
            return true;
        } else if (id == R.id.action_multiselect) {
            enableMultiSelect();
            return true;
        } else if (id == R.id.action_delete_selected) {
            deleteSelectedTasks();
            return true;
        } else if (id == R.id.action_mark_done) {
            markSelectedDone();
            return true;
        } else if (id == R.id.action_change_language) {
            toggleLanguage();
            return true;
        } else if (id == R.id.action_test_notification) {
            testNotification();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void exportTasks() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "tasks.json");
        exportLauncher.launch(intent);
    }

    private void importTasks() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/json");
        importLauncher.launch(intent);
    }

    // Theme customization
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        
        // Modern way to update locale configuration (API 17+)
        Resources resources = getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        
        // Create context with updated configuration
        Context context = createConfigurationContext(config);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        
        currentLanguage = lang;
        
        // Update task dates for the new locale
        updateTaskDates();
    }

    private void updateTaskDates() {
        if (taskList == null) return;
        
        // Since dates are now always in English, we don't need to do any conversion
        // just reset any display dates that might have been set previously
        for (Task task : taskList) {
            task.setDisplayDueDate(null);
        }
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // Helper method to convert Bengali numerals back to ASCII digits
    private String fromBengaliNumerals(String input) {
        if (input == null) return null;
        
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= '০' && c <= '৯') {
                // Convert Bengali digits to ASCII digits
                result.append((char) (c - '০' + '0'));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    private void updateNotificationBadge() {
        // Show a badge if there are any due tasks (not done and due date today or earlier)
        boolean hasDue = false;
        Calendar today = Calendar.getInstance();
        
        // Debug count of overdue tasks
        int overdueTasks = 0;
        
        for (Task t : taskList) {
            if (!t.isDone() && t.getDueDate() != null && !t.getDueDate().isEmpty()) {
                try {
                    // Dates are always in English format now so no need for conversion
                    String[] parts = t.getDueDate().split("-");
                    int y = Integer.parseInt(parts[0]);
                    int m = Integer.parseInt(parts[1]) - 1;
                    int d = Integer.parseInt(parts[2]);
                    Calendar due = Calendar.getInstance();
                    due.set(y, m, d, 0, 0, 0);
                    due.set(Calendar.MILLISECOND, 0);
                    if (!due.after(today)) {
                        hasDue = true;
                        overdueTasks++;
                    }
                } catch (Exception ignore) {}
            }
        }
        
        Log.d("TODO_APP", "Updating notification badge: hasDue=" + hasDue + ", overdueTasks=" + overdueTasks);
        
        // Force update of the notification icon
        notificationIcon.setImageResource(0); // Clear image first
        
        // Check if we're in dark mode
        boolean isDarkMode = (getResources().getConfiguration().uiMode & 
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        
        if (hasDue) {
            // Use the appropriate notification image with dot for badge
            if (isDarkMode) {
                // Use white notification icon in dark mode
                notificationIcon.setImageResource(R.drawable.notification_dark);
                // Apply color filter to make it white
                notificationIcon.setColorFilter(getResources().getColor(android.R.color.white));
            } else {
                notificationIcon.setImageResource(R.drawable.notification_dark);
                // Clear any color filter
                notificationIcon.clearColorFilter();
            }
        } else {
            // Use the regular notification image
            if (isDarkMode) {
                // Use white notification icon in dark mode
                notificationIcon.setImageResource(R.drawable.notification);
                // Apply color filter to make it white
                notificationIcon.setColorFilter(getResources().getColor(android.R.color.white));
            } else {
                notificationIcon.setImageResource(R.drawable.notification);
                // Clear any color filter
                notificationIcon.clearColorFilter();
            }
        }
    }

    // Local notifications for due tasks
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "To-Do Reminders";
            String description = "Channel for To-Do reminders";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied - show a message to explain why notifications are important
                Toast.makeText(this, "Notification permission is needed for task reminders", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class ReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String taskText = intent.getStringExtra("taskText");
            if (taskText == null) taskText = "Task reminder";
            
            // Check for notification permission 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // Permission not granted, can't show notification
                    return;
                }
            }
            
            try {
                // Set the correct notification icon based on night mode
                int iconRes = R.drawable.notification_dark;
                
                // For system notifications in Android, white icons are automatically used
                // in the status bar in both light and dark themes per Material Design guidelines
                
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(iconRes)
                        .setContentTitle("Task Due")
                        .setContentText(taskText)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                int notificationId = taskText.hashCode();
                notificationManager.notify(notificationId, builder.build());
            } catch (SecurityException e) {
                // Handle gracefully
                Log.e("ReminderReceiver", "Cannot show notification: " + e.getMessage());
            }
        }
    }

    private void shareApk() {
        try {
            String appPath = getPackageManager().getApplicationInfo(getPackageName(), 0).sourceDir;
            File apkFile = new File(appPath);
            Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", apkFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.android.package-archive");
            shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to share APK", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleLanguage() {
        String newLang = currentLanguage.equals(LANGUAGE_EN) ? LANGUAGE_BN : LANGUAGE_EN;
        prefs.edit().putString(PREF_KEY_LANGUAGE, newLang).apply();
        setLocale(newLang);
        recreate();
    }
    
    // Helper method to convert digits to Bengali numerals
    private String toBengaliNumerals(String input) {
        if (input == null) return null;
        if (!currentLanguage.equals(LANGUAGE_BN)) return input;
        
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                // Convert ASCII digits to Bengali digits (Unicode range)
                result.append((char) (c - '0' + '০'));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    // Format date based on current language - always keep date in English
    private String formatDate(int year, int month, int day) {
        // Always use English locale for date formatting
        return String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
    }

    private void showNotificationDialog() {
        // Get overdue tasks
        List<Task> overdueTasks = getOverdueTasks();
        
        if (overdueTasks.isEmpty()) {
            Toast.makeText(this, getString(R.string.notification_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create dialog with floating notification layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.floating_notification, null);
        builder.setView(dialogView);
        
        // Set up the RecyclerView
        RecyclerView recyclerView = dialogView.findViewById(R.id.notification_tasks_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        NotificationTaskAdapter adapter = new NotificationTaskAdapter(this, overdueTasks);
        recyclerView.setAdapter(adapter);
        
        // Create and show the dialog
        AlertDialog dialog = builder.create();
        
        // Set up dismiss button
        Button dismissButton = dialogView.findViewById(R.id.notification_dismiss_button);
        dismissButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private List<Task> getOverdueTasks() {
        List<Task> overdueTasks = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        
        for (Task task : taskList) {
            if (!task.isDone() && task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                try {
                    // Dates are always in English format now so no need for conversion
                    String[] parts = task.getDueDate().split("-");
                    int y = Integer.parseInt(parts[0]);
                    int m = Integer.parseInt(parts[1]) - 1;
                    int d = Integer.parseInt(parts[2]);
                    Calendar due = Calendar.getInstance();
                    due.set(y, m, d, 0, 0, 0);
                    due.set(Calendar.MILLISECOND, 0);
                    if (!due.after(today)) {
                        overdueTasks.add(task);
                    }
                } catch (Exception ignore) {}
            }
        }
        
        // Sort by priority: High > Medium > Low
        overdueTasks.sort(Comparator.comparingInt(a -> getPriorityValue(a.getPriority())));
        
        return overdueTasks;
    }

    // Testing method to create an overdue task and show notification
    private void testNotification() {
        // Create yesterday's date
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        int year = yesterday.get(Calendar.YEAR);
        int month = yesterday.get(Calendar.MONTH);
        int day = yesterday.get(Calendar.DAY_OF_MONTH);
        String yesterdayStr = formatDate(year, month, day);
        
        // Create a test overdue task
        Task overdueTask = new Task("Test overdue task", false, yesterdayStr, "Urgent", "High");
        taskList.add(overdueTask);
        saveTasks();
        updateEmptyState();
        filterTasks();
        updateNotificationBadge();
        
        // Create and show a system notification
        try {
            // Check if we're in dark mode
            boolean isDarkMode = (getResources().getConfiguration().uiMode & 
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            
            // Set icon resource (system notifications will automatically use white in dark mode)
            int iconRes = R.drawable.notification_dark;
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(iconRes)
                    .setContentTitle("Task Due")
                    .setContentText("Test overdue task")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                        == PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(1001, builder.build());
                }
            } else {
                notificationManager.notify(1001, builder.build());
            }
        } catch (Exception e) {
            Log.e("TODO_APP", "Failed to show test notification: " + e.getMessage());
        }
        
        // Show a toast and open the notification dialog
        Toast.makeText(this, "Added overdue task and showing notification", Toast.LENGTH_SHORT).show();
        showNotificationDialog();
    }

    // Custom item decoration for consistent vertical spacing
    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            // Don't add spacing for the last item
            if (parent.getAdapter() != null && parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = verticalSpaceHeight;
            }
        }
    }

    // Context menu actions from TaskAdapter
    @Override
    public void onEditTask(int position) {
        showEditTaskDialog(position);
    }
    @Override
    public void onDeleteTask(int position) {
        if (position >= 0 && position < displayList.size()) {
            Task removedTask = displayList.get(position);
            int indexInTaskList = taskList.indexOf(removedTask);
            if (indexInTaskList != -1) {
                taskList.remove(indexInTaskList);
                saveTasks();
                filterTasks();
                updateNotificationBadge();
            }
        }
    }
    @Override
    public void onMarkDoneTask(int position) {
        if (position >= 0 && position < displayList.size()) {
            Task task = displayList.get(position);
            task.setDone(true);
            saveTasks();
            filterTasks();
            updateNotificationBadge();
        }
    }

    private void toggleTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private void deleteSelectedTasks() {
        List<Task> toRemove = adapter.getSelectedTasks();

        // Get indices to remove (in reverse order to avoid index shifting)
        List<Integer> indicesToRemove = new ArrayList<>();
        for (Task task : toRemove) {
            indicesToRemove.add(taskList.indexOf(task));
        }
        indicesToRemove.sort((a, b) -> Integer.compare(b, a)); // Sort in descending order

        // Remove tasks and notify adapter
        for (int index : indicesToRemove) {
            if (index >= 0 && index < taskList.size()) {
                taskList.remove(index);
                adapter.notifyItemRemoved(index);
            }
        }

        adapter.clearSelection();
        disableMultiSelect();
        onTasksChanged();
        updateNotificationBadge(); // Ensure notification icon updates
    }

    private void markSelectedDone() {
        List<Task> toMark = adapter.getSelectedTasks();

        // Mark tasks as done and notify adapter
        for (Task task : toMark) {
            task.setDone(true);
            adapter.notifyItemChanged(taskList.indexOf(task));
        }

        adapter.clearSelection();
        disableMultiSelect();
        onTasksChanged();
        updateNotificationBadge(); // Ensure notification icon updates
    }
}