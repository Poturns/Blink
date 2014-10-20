package kr.poturns.blink.demo.fitnessapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Calendar;

import kr.poturns.blink.demo.fitnessapp.schema.InBodyData;
import android.content.Context;
import android.util.Log;

/** @author Myungjin.Kim */
public class FitnessUtil {
	public static final String FILE_INBODY = "inbody";

	/**
	 * 해당 {@link SQLiteHelper}의 Table에 해당하는 운동의 {@code count}만큼 칼로리를 계산한다.
	 * 
	 * @param table
	 *            <li>{@link SQLiteHelper#TABLE_PUSH_UP}</li> <li>
	 *            {@link SQLiteHelper#TABLE_SIT_UP}</li><li>
	 *            {@link SQLiteHelper#TABLE_SQUAT}</li> <t>중 하나.
	 * @param count
	 *            운동 횟수
	 * @return 해당 운동 횟수만큼 소모한 kcal,<br>
	 *         <t> {@code table}에 해당하는 운동이 아니거나 {@code count < 1}이면 0
	 */
	public static final double calculateCalorie(String table, int count) {
		if (count < 1)
			return 0;
		if (table.equals(SQLiteHelper.TABLE_PUSH_UP))
			return 0.825 * ((double) count);
		else if (table.equals(SQLiteHelper.TABLE_SIT_UP))
			return 0.9 * ((double) count);
		else if (table.equals(SQLiteHelper.TABLE_SQUAT))
			return ((double) count) / 225d * 100d;
		else
			return 0;
	}

	/** InBody 데이터를 읽어온다 */
	public static final InBodyData readInBodyFromFile(Context context)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		ObjectInputStream ois = null;
		InBodyData object = null;
		try {
			fis = context.openFileInput(FILE_INBODY);
			bis = new BufferedInputStream(fis);
			ois = new ObjectInputStream(bis);
			object = (InBodyData) ois.readObject();
		} finally {
			closeStream(ois);
			closeStream(bis);
			closeStream(fis);
		}
		return object;
	}

	/** InBody 데이터를 저장한다 */
	public static boolean saveInBodyFile(Context context, InBodyData obj)
			throws IOException {
		boolean state = false;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			fos = context.openFileOutput(FILE_INBODY, Context.MODE_PRIVATE);
			bos = new BufferedOutputStream(fos);
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			state = true;
		} finally {
			closeStream(oos);
			closeStream(bos);
			closeStream(fos);
		}
		return state;
	}

	private static void closeStream(Closeable close) {
		if (close != null) {
			try {
				close.close();
			} catch (IOException e) {
			}
		}
	}

	/** 오늘 운동한 횟수를 얻는다. */
	public static int getTodayExerciseCount(Context context, String table) {
		Calendar c = Calendar.getInstance();
		return SQLiteHelper.getInstance(context).select(table,
				String.valueOf(c.get(Calendar.YEAR)),
				c.get(c.get(Calendar.MONTH) + 1), c.get(Calendar.DATE));
	}

	/** 오늘 소모한 총 칼로리 양을 얻는다. */
	public static int getTodayBurnedCalorie(Context context) {
		double squatCal = calculateCalorie(SQLiteHelper.TABLE_SQUAT,
				getTodayExerciseCount(context, SQLiteHelper.TABLE_SQUAT));
		double pushupCal = calculateCalorie(SQLiteHelper.TABLE_PUSH_UP,
				getTodayExerciseCount(context, SQLiteHelper.TABLE_PUSH_UP));
		double situpCal = calculateCalorie(SQLiteHelper.TABLE_SIT_UP,
				getTodayExerciseCount(context, SQLiteHelper.TABLE_SIT_UP));

		Log.i("test", "소모칼리로 : "+(int) (situpCal + pushupCal + squatCal));
		return (int) (situpCal + pushupCal + squatCal);
	}
}
