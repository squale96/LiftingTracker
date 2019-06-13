package com.squale.liftingtracker.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;

import com.squale.liftingtracker.models.Workout;
import com.squale.liftingtracker.models.Exercise;

public class ExerciseDAO {
    public static final String TAG = "ExerciseDAO";

    private Context context;

    // Database fields
    private SQLiteDatabase database;
    private DatabaseHelperWorkout databaseHelper;
    private String[] mAllColumns = {DatabaseHelperWorkout.COL_EXERCISE_ID,
            DatabaseHelperWorkout.COL_EXERCISE_NAME, DatabaseHelperWorkout.COL_EXERCISE_WORKOUT_ID};

    public ExerciseDAO(Context context) {
        databaseHelper = new DatabaseHelperWorkout(context);
        this.context = context;
        // open the database
        try {
            open();
        } catch (SQLException e) {
            Log.e(TAG, "SQLException on opening database " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void open() throws SQLException {
        database = databaseHelper.getWritableDatabase();
    }

    public void close() {
        databaseHelper.close();
    }

    public long createExercise(Exercise exercise) {

        long id = -1;
        DatabaseHelperWorkout databaseHelperWorkout = DatabaseHelperWorkout.getInstance(context);
        SQLiteDatabase sqLiteDatabase = databaseHelperWorkout.getWritableDatabase();
        long workoutID = exercise.getWorkout().getId();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelperWorkout.COL_EXERCISE_NAME, exercise.getName());
        contentValues.put(DatabaseHelperWorkout.COL_EXERCISE_WORKOUT_ID, workoutID);
        try {
            id = sqLiteDatabase.insertOrThrow(DatabaseHelperWorkout.TABLE_EXERCISE,  null, contentValues);
        } catch (SQLiteException e){
            Log.e(TAG, "Exception: " + e.getMessage());
            Toast.makeText(context, "Operation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            sqLiteDatabase.close();
        }

        return id;
    }

    public void deleteExercise(Exercise exercise) {
        long id = exercise.getId();
        System.out.println("the deleted employee has the id: " + id);
        database.delete(DatabaseHelperWorkout.TABLE_EXERCISE, DatabaseHelperWorkout.COL_EXERCISE_ID
                + " = " + id, null);
    }

        public List<Exercise> getAllExercises() {
            List<Exercise> listExercises = new ArrayList<>();

            Cursor cursor = database.query(DatabaseHelperWorkout.TABLE_EXERCISE, mAllColumns,
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Exercise exercise = cursorToExercise(cursor);
            listExercises.add(exercise);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return listExercises;
    }

    public List<Exercise> getExercisesOfWorkout(long workoutId) {
        List<Exercise> listExercise = new ArrayList<>();

        Cursor cursor = database.query(DatabaseHelperWorkout.TABLE_EXERCISE, mAllColumns,
                DatabaseHelperWorkout.COL_WORKOUT_ID + " = ?",
                new String[]{String.valueOf(workoutId)}, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Exercise exercise = cursorToExercise(cursor);
            listExercise.add(exercise);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return listExercise;
    }

    public Exercise getExerciseById(long id) {
        Cursor cursor = database.query(DatabaseHelperWorkout.TABLE_EXERCISE, mAllColumns,
                DatabaseHelperWorkout.COL_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursorToExercise(cursor);
    }

    private Exercise cursorToExercise(Cursor cursor) {
        Exercise exercise = new Exercise();
        exercise.setId(cursor.getLong(0));
        exercise.setName(cursor.getString(1));

        // get The company by id
        long workoutId = cursor.getLong(2);
        WorkoutDAO dao = new WorkoutDAO(context);
        Workout workout = dao.getWorkoutById(workoutId);
        if (workout != null)
            exercise.setWorkout(workout);

        return exercise;
    }
}
