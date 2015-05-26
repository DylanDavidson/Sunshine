/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.nylad.sunshine.app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(me.nylad.sunshine.app.data.WeatherDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(me.nylad.sunshine.app.data.WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(me.nylad.sunshine.app.data.WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(me.nylad.sunshine.app.data.WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new me.nylad.sunshine.app.data.WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + me.nylad.sunshine.app.data.WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(me.nylad.sunshine.app.data.WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(me.nylad.sunshine.app.data.WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(me.nylad.sunshine.app.data.WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(me.nylad.sunshine.app.data.WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(me.nylad.sunshine.app.data.WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testLocationTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        ContentValues cv = new ContentValues();
        cv.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        cv.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, testCityName);
        cv.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, testLatitude);
        cv.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, testLongitude);
        // Insert ContentValues into database and get a row ID back
        long rowID = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null,cv);
        // Query the database and receive a Cursor back
        Cursor result = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,
                null,
                WeatherContract.LocationEntry._ID + " = " + rowID,
                null, null, null, null, null
        );
        // Move the cursor to a valid database row
        result.moveToFirst();
        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        assertEquals(
                testLocationSetting,
                result.getString(result.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING))
        );
        assertEquals(
                testCityName,
                result.getString(result.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY_NAME))
        );
        assertEquals(
                testLatitude,
                result.getDouble(result.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LAT))
        );
        assertEquals(
                testLongitude,
                result.getDouble(result.getColumnIndex(WeatherContract.LocationEntry.COLUMN_COORD_LONG))
        );
        // Finally, close the cursor and database
        db.close();
        result.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.
        long locationRowID = TestUtilities.insertNorthPoleLocationValues(this.mContext);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();

        ContentValues cv = TestUtilities.createWeatherValues(locationRowID);


        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        // First step: Get reference to writable database

        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)

        // Insert ContentValues into database and get a row ID back

        long rowID = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, cv);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME,
                null,
                WeatherContract.WeatherEntry._ID + " = " + rowID,
                null, null, null, null, null
        );
        // Move the cursor to a valid database row
        cursor.moveToFirst();
        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("bleh", cursor, cv);
        // Finally, close the cursor and database
        db.close();
        cursor.close();
    }


    /*
        Students: This is a helper method for the testWeatherTable quiz. You can move your
        code from testLocationTable to here so that you can call this code from both
        testWeatherTable and testLocationTable.
     */
    public long insertLocation() {
        return -1L;
    }
}
