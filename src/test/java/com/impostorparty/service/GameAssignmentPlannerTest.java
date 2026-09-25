package com.impostorparty.service;

import com.impostorparty.model.TaskContent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameAssignmentPlannerTest {

    @Test
    public void createsDisjointListsAndUniqueWords() throws Exception {
        int durationHours = 4;
        int numImpostors = 3;
        int tasksPerPlayer = durationHours * 3;
        int listCount = 1 + numImpostors;
        int requiredTasks = tasksPerPlayer * listCount;

        List<String> words = new ArrayList<>(Arrays.asList(
                "Playa", " plAyA ", "Pizza", "Cine", "Guitarra", "Bicicleta"
        ));
        List<TaskContent> tasks = new ArrayList<>();
        for (int index = 0; index < requiredTasks + 10; index++) {
            tasks.add(new TaskContent(index + 1, "Tarea " + index));
        }
        tasks.add(new TaskContent(1000, " tarea 0 "));

        GameAssignmentPlanner.AssignmentPlan plan = GameAssignmentPlanner.createPlan(
                durationHours,
                numImpostors,
                words,
                tasks,
                new Random(42L)
        );

        assertEquals(tasksPerPlayer, plan.getTasksPerPlayer());
        assertEquals(listCount, plan.getTaskLists().size());
        assertEquals(tasksPerPlayer, plan.getTaskLists().get(0).size());
        assertEquals(numImpostors, plan.getImpostorWords().size());

        Set<Integer> assignedTaskIds = new HashSet<>();
        Set<String> assignedDescriptions = new HashSet<>();
        int assignedTasks = 0;
        for (List<TaskContent> taskList : plan.getTaskLists()) {
            assertEquals(tasksPerPlayer, taskList.size());
            for (TaskContent task : taskList) {
                assertTrue(assignedTaskIds.add(task.getId()));
                assertTrue(assignedDescriptions.add(normalize(task.getDescription())));
                assignedTasks++;
            }
        }
        assertEquals(requiredTasks, assignedTasks);

        Set<String> assignedWords = new HashSet<>();
        assertTrue(assignedWords.add(normalize(plan.getNormalWord())));
        for (String word : plan.getImpostorWords()) {
            assertTrue(assignedWords.add(normalize(word)));
        }
        assertEquals(listCount, assignedWords.size());
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
