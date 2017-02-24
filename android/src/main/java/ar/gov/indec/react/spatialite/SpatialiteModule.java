package ar.gov.indec.react.spatialite;

import android.support.annotation.BoolRes;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.spatialite.Cursor;
import org.spatialite.database.SQLiteDatabase;
import org.spatialite.database.SQLiteOpenHelper;

public class SpatialiteModule extends ReactContextBaseJavaModule {
    SQLiteOpenHelper eventsData;
    SQLiteDatabase db;

    public SpatialiteModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @ReactMethod
    public void createConnection(String connectionString, final Promise promise) {
        try {
            if (db != null && db.isOpen()) {
                promise.resolve(db.isOpen());
            }
            SQLiteDatabase.loadLibs(this.getReactApplicationContext());
            eventsData = new SQLiteOpenHelper(this.getReactApplicationContext(), connectionString, null, 1) {
                @Override
                public void onCreate(SQLiteDatabase db) {
                }

                @Override
                public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                }

                @Override
                public void onOpen(SQLiteDatabase db) {
                    promise.resolve(db.isOpen());
                }
            };
            db = eventsData.getWritableDatabase();
            if (!db.isOpen()) {
                promise.reject("DatabaseNotOpened", "Getting Database Failed for: " + connectionString);
            }
        } catch (RuntimeException re) {
            Log.e("SPATIALITE-ERROR", re.toString());
            promise.reject(re);
        }
    }

    @ReactMethod
    public void executeQuery(String query, Promise promise) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            String[] columnNames = cursor.getColumnNames();

            WritableArray array = Arguments.createArray();
            while (cursor.moveToNext()) {
                WritableMap row = Arguments.createMap();
                for (String column : columnNames) {
                    switch (cursor.getType(cursor.getColumnIndex(column))) {
                        case Cursor.FIELD_TYPE_NULL:
                            row.putNull(column);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            row.putInt(column, cursor.getInt(cursor.getColumnIndex(column)));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            row.putDouble(column, cursor.getDouble(cursor.getColumnIndex(column)));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            row.putString(column, cursor.getString(cursor.getColumnIndex(column)));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            throw new UnsupportedOperationException("FIELD_TYPE_BLOB is not supported.");
                        default:
                            throw new UnsupportedOperationException("Type is not supported: " + cursor.getType(cursor.getColumnIndex(column)));
                    }
                }
                array.pushMap(row);
            }
            promise.resolve(array);
        } catch (Exception e) {
            Log.e("SPATIALITE-ERROR", e.toString());
            promise.reject(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @ReactMethod
    public void executeSql(String sql, Promise promise) {
        try {
            db.execSQL(sql);
            promise.resolve(null);
        } catch (RuntimeException e) {
            Log.e("SPATIALITE-ERROR", e.toString());
            promise.reject(e);
        }
    }

    @ReactMethod
    public void closeConnection(Promise promise) {
        try {
            db.close();
            promise.resolve(db.isOpen());
        } catch (RuntimeException re) {
            Log.e("SPATIALITE-ERROR", re.toString());
            promise.reject(re);
        }
    }

    @ReactMethod
    public void isReadOnly(Promise promise) {
        promise.resolve(db.isReadOnly());
    }

    @ReactMethod
    public void getVersion(Promise promise) {
        executeQuery("SELECT spatialite_version() AS spatialiteVersion, proj4_version() AS proj4Version, geos_version() AS geosVersion;", promise);
    }

    @Override
    public String getName() {
        return "ReactNativeSpatialite";
    }
}
