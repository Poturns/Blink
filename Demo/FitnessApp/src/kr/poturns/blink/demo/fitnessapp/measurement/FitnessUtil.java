package kr.poturns.blink.demo.fitnessapp.measurement;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import kr.poturns.blink.demo.fitnessapp.SQLiteHelper;
import android.content.Context;

public class FitnessUtil {
	public static final String FILE_INBODY = "inbody";

	public static final double calculateCalorie(String table, int count) {
		if (table.equals(SQLiteHelper.TABLE_PUSH_UP))
			return 0.825 * ((double) count);
		else if (table.equals(SQLiteHelper.TABLE_SIT_UP))
			return 0.9 * ((double) count);
		else if (table.equals(SQLiteHelper.TABLE_SQUAT))
			return ((double) count) / 225d * 100d;
		else
			return 0;
	}

	public static final String recommandFitness(InBodyData data) {
		return null;
	}

	public static final int recommandCalorie(InBodyData data) {
		return 0;
	}

	public static final InBodyData readInBodyFromFile(Context context)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		ObjectInputStream ois = null;
		InBodyData object;
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
}
