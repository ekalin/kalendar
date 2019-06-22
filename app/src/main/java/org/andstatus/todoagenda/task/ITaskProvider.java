package org.andstatus.todoagenda.task;

import java.util.List;

public interface ITaskProvider {

    List<TaskEvent> getTasks();
}
