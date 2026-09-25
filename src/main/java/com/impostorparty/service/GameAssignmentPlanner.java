package com.impostorparty.service;

import com.impostorparty.model.TaskContent;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class GameAssignmentPlanner {
    private static final int TASKS_PER_HOUR = 3;

    private GameAssignmentPlanner() {
    }

    public static AssignmentPlan createPlan(int durationHours, int numImpostors,
                                            List<String> words, List<TaskContent> tasks)
            throws InsufficientContentException {
        return createPlan(durationHours, numImpostors, words, tasks, new Random());
    }

    public static AssignmentPlan createPlan(int durationHours, int numImpostors,
                                            List<String> words, List<TaskContent> tasks,
                                            Random random) throws InsufficientContentException {
        if (durationHours < 1 || durationHours > 12) {
            throw new IllegalArgumentException("durationHours must be between 1 and 12");
        }
        if (numImpostors < 1 || numImpostors > 3) {
            throw new IllegalArgumentException("numImpostors must be between 1 and 3");
        }
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }

        int wordsNeeded = 1 + numImpostors;
        List<String> uniqueWords = uniqueWords(words);
        if (uniqueWords.size() < wordsNeeded) {
            throw new InsufficientContentException("No hay suficientes palabras distintas para esta modalidad");
        }
        Collections.shuffle(uniqueWords, random);
        List<String> selectedWords = new ArrayList<>(uniqueWords.subList(0, wordsNeeded));

        int tasksPerPlayer = durationHours * TASKS_PER_HOUR;
        int tasksNeeded = tasksPerPlayer * wordsNeeded;
        List<TaskContent> uniqueTasks = uniqueTasks(tasks);
        if (uniqueTasks.size() < tasksNeeded) {
            throw new InsufficientContentException("No hay suficientes retos distintos para esta modalidad");
        }
        Collections.shuffle(uniqueTasks, random);

        List<List<TaskContent>> taskLists = new ArrayList<>(wordsNeeded);
        for (int listIndex = 0; listIndex < wordsNeeded; listIndex++) {
            int fromIndex = listIndex * tasksPerPlayer;
            int toIndex = fromIndex + tasksPerPlayer;
            taskLists.add(new ArrayList<>(uniqueTasks.subList(fromIndex, toIndex)));
        }

        return new AssignmentPlan(tasksPerPlayer, selectedWords, taskLists);
    }

    private static List<String> uniqueWords(List<String> words) {
        Map<String, String> unique = new LinkedHashMap<>();
        if (words != null) {
            for (String word : words) {
                String normalized = normalize(word);
                if (normalized != null && !normalized.isEmpty()) {
                    unique.putIfAbsent(normalized, word);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    private static List<TaskContent> uniqueTasks(List<TaskContent> tasks) {
        Map<String, TaskContent> unique = new LinkedHashMap<>();
        if (tasks != null) {
            for (TaskContent task : tasks) {
                if (task == null) {
                    continue;
                }
                String normalized = normalize(task.getDescription());
                if (normalized != null && !normalized.isEmpty()) {
                    unique.putIfAbsent(normalized, task);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[\\s\\p{Z}]+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public static final class InsufficientContentException extends Exception {
        public InsufficientContentException(String message) {
            super(message);
        }
    }

    public static final class AssignmentPlan {
        private final int tasksPerPlayer;
        private final String normalWord;
        private final List<String> impostorWords;
        private final List<List<TaskContent>> taskLists;

        private AssignmentPlan(int tasksPerPlayer, List<String> selectedWords,
                               List<List<TaskContent>> taskLists) {
            this.tasksPerPlayer = tasksPerPlayer;
            this.normalWord = selectedWords.get(0);
            this.impostorWords = immutableCopy(selectedWords.subList(1, selectedWords.size()));
            List<List<TaskContent>> copiedLists = new ArrayList<>();
            for (List<TaskContent> taskList : taskLists) {
                copiedLists.add(Collections.unmodifiableList(new ArrayList<>(taskList)));
            }
            this.taskLists = Collections.unmodifiableList(copiedLists);
        }

        public int getTasksPerPlayer() {
            return tasksPerPlayer;
        }

        public String getNormalWord() {
            return normalWord;
        }

        public List<String> getImpostorWords() {
            return impostorWords;
        }

        public List<List<TaskContent>> getTaskLists() {
            return taskLists;
        }

        private static <T> List<T> immutableCopy(List<T> values) {
            return Collections.unmodifiableList(new ArrayList<>(values));
        }
    }
}
