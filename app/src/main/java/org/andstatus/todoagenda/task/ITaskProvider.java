package org.andstatus.todoagenda.task;

import android.app.Activity;

import java.util.List;

public interface ITaskProvider {

    List<TaskEvent> getTasks();

    boolean hasPermission();

    void requestPermission(Activity activity);
}
