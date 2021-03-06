package com.douncoding.dao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import com.douncoding.dao.LessonTime;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "LESSON_TIME".
*/
public class LessonTimeDao extends AbstractDao<LessonTime, Long> {

    public static final String TABLENAME = "LESSON_TIME";

    /**
     * Properties of entity LessonTime.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Day = new Property(1, Integer.class, "day", false, "DAY");
        public final static Property StartDate = new Property(2, java.util.Date.class, "startDate", false, "START_DATE");
        public final static Property EndDate = new Property(3, java.util.Date.class, "endDate", false, "END_DATE");
        public final static Property StartTime = new Property(4, String.class, "startTime", false, "START_TIME");
        public final static Property EndTime = new Property(5, String.class, "endTime", false, "END_TIME");
        public final static Property Lid = new Property(6, long.class, "lid", false, "LID");
    };

    private DaoSession daoSession;

    private Query<LessonTime> lesson_LessonTimeListQuery;

    public LessonTimeDao(DaoConfig config) {
        super(config);
    }
    
    public LessonTimeDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"LESSON_TIME\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"DAY\" INTEGER," + // 1: day
                "\"START_DATE\" INTEGER," + // 2: startDate
                "\"END_DATE\" INTEGER," + // 3: endDate
                "\"START_TIME\" TEXT," + // 4: startTime
                "\"END_TIME\" TEXT," + // 5: endTime
                "\"LID\" INTEGER NOT NULL );"); // 6: lid
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"LESSON_TIME\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, LessonTime entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Integer day = entity.getDay();
        if (day != null) {
            stmt.bindLong(2, day);
        }
 
        java.util.Date startDate = entity.getStartDate();
        if (startDate != null) {
            stmt.bindLong(3, startDate.getTime());
        }
 
        java.util.Date endDate = entity.getEndDate();
        if (endDate != null) {
            stmt.bindLong(4, endDate.getTime());
        }
 
        String startTime = entity.getStartTime();
        if (startTime != null) {
            stmt.bindString(5, startTime);
        }
 
        String endTime = entity.getEndTime();
        if (endTime != null) {
            stmt.bindString(6, endTime);
        }
        stmt.bindLong(7, entity.getLid());
    }

    @Override
    protected void attachEntity(LessonTime entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public LessonTime readEntity(Cursor cursor, int offset) {
        LessonTime entity = new LessonTime( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1), // day
            cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)), // startDate
            cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)), // endDate
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // startTime
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // endTime
            cursor.getLong(offset + 6) // lid
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, LessonTime entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setDay(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
        entity.setStartDate(cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)));
        entity.setEndDate(cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)));
        entity.setStartTime(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setEndTime(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setLid(cursor.getLong(offset + 6));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(LessonTime entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(LessonTime entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "lessonTimeList" to-many relationship of Lesson. */
    public List<LessonTime> _queryLesson_LessonTimeList(long lid) {
        synchronized (this) {
            if (lesson_LessonTimeListQuery == null) {
                QueryBuilder<LessonTime> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.Lid.eq(null));
                lesson_LessonTimeListQuery = queryBuilder.build();
            }
        }
        Query<LessonTime> query = lesson_LessonTimeListQuery.forCurrentThread();
        query.setParameter(0, lid);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getLessonDao().getAllColumns());
            builder.append(" FROM LESSON_TIME T");
            builder.append(" LEFT JOIN LESSON T0 ON T.\"LID\"=T0.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected LessonTime loadCurrentDeep(Cursor cursor, boolean lock) {
        LessonTime entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Lesson lesson = loadCurrentOther(daoSession.getLessonDao(), cursor, offset);
         if(lesson != null) {
            entity.setLesson(lesson);
        }

        return entity;    
    }

    public LessonTime loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<LessonTime> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<LessonTime> list = new ArrayList<LessonTime>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<LessonTime> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<LessonTime> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
