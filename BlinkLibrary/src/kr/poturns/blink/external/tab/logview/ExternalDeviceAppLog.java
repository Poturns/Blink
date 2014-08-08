package kr.poturns.blink.external.tab.logview;

import java.util.Comparator;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <li>
 * device -> {@link ExternalDeviceAppLog#FIELD_DEVICE}</li><li>app ->
 * {@link ExternalDeviceAppLog#FIELD_APP}</li><li>
 * content -> {@link ExternalDeviceAppLog#FIELD_CONTENT}</li><li>dateTime ->
 * {@link ExternalDeviceAppLog#FIELD_DATETIME}</li>
 * 
 */
public class ExternalDeviceAppLog implements Parcelable {
	static final int FIELD_SIZE = 4;
	public static final int FIELD_DEVICE = 0;
	public static final int FIELD_APP = 1;
	public static final int FIELD_CONTENT = 2;
	public static final int FIELD_DATETIME = 3;
	public String[] fieldArray = new String[FIELD_SIZE];

	public ExternalDeviceAppLog() {
		for (int i = 0; i < FIELD_SIZE; i++)
			fieldArray[i] = "";
	}

	public ExternalDeviceAppLog(String device, String app, String content,
			String dateTime) {
		fieldArray[FIELD_DEVICE] = device;
		fieldArray[FIELD_APP] = app;
		fieldArray[FIELD_CONTENT] = content;
		fieldArray[FIELD_DATETIME] = dateTime;
	}

	public ExternalDeviceAppLog(String[] fieldArray) {
		if (fieldArray.length < FIELD_SIZE)
			throw new RuntimeException(
					"could not convert to ExternalDeviceAppLog object");
		for (int i = 0; i < FIELD_SIZE; i++) {
			this.fieldArray[i] = fieldArray[i];
		}
	}

	ExternalDeviceAppLog(Parcel p) {
		p.readStringArray(fieldArray);
	}

	/**
	 * 로그 객체의 멤버를 얻는다.
	 * 
	 * @param fieldConstant
	 *            <li>
	 *            device -> {@link ExternalDeviceAppLog#FIELD_DEVICE}</li><li>
	 *            app -> {@link ExternalDeviceAppLog#FIELD_APP}</li><li>
	 *            content -> {@link ExternalDeviceAppLog#FIELD_CONTENT}</li><li>
	 *            dateTime -> {@link ExternalDeviceAppLog#FIELD_DATETIME}</li> <br>
	 *            중 하나
	 * @return fieldConstant에 알맞는 멤버
	 * @throws RuntimeException
	 *             적절하지 않은 fieldConstant가 들어온 경우
	 */
	public String getField(int fieldConstant) {
		if (fieldConstant < 0 || fieldConstant > FIELD_SIZE) {
			throw new RuntimeException("field contstant error");
		}
		return fieldArray[fieldConstant];
	}

	/**
	 * 정수 순서에 맞는 ExternalDeviceAppLog의 Field를 가리키는 Field 상수를 얻는다.
	 */
	public static int getComparatorFieldNumberByOrder(int order) {
		if (order < 0 || order > FIELD_SIZE) {
			return -1;
		}
		switch (order) {
		case 0:
			return FIELD_DEVICE;
		case 1:
			return FIELD_APP;
		case 2:
			return FIELD_CONTENT;
		case 3:
			return FIELD_DATETIME;
		default:
			return -1;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String[] array = new String[] { "Device : ", "App : ", "Content : ",
				"DateTime : " };
		for (int i = 0; i < FIELD_SIZE; i++) {
			sb.append(array[i] + fieldArray[i] + "\n");
		}
		return sb.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(fieldArray);
	}

	public static final Parcelable.Creator<ExternalDeviceAppLog> CREATOR = new Parcelable.Creator<ExternalDeviceAppLog>() {

		@Override
		public ExternalDeviceAppLog[] newArray(int size) {
			return new ExternalDeviceAppLog[size];
		}

		@Override
		public ExternalDeviceAppLog createFromParcel(Parcel source) {
			return new ExternalDeviceAppLog(source);
		}
	};

	public static class LogComparator implements
			Comparator<ExternalDeviceAppLog> {
		int mComparatorField;
		boolean mIsAsendingOrder;

		public LogComparator() {
			mComparatorField = FIELD_DEVICE;
			mIsAsendingOrder = true;
		}

		public LogComparator(int field, boolean order) {
			mComparatorField = field;
			mIsAsendingOrder = order;
		}

		public void setComparatorField(int fieldConstant) {
			if (fieldConstant < 0 || fieldConstant > FIELD_SIZE) {
				return;
			}
			mComparatorField = fieldConstant;
		}

		public void setOrder(boolean isAsending) {
			mIsAsendingOrder = isAsending;
		}

		public int getComparatorField() {
			return mComparatorField;
		}

		public boolean getOrder() {
			return mIsAsendingOrder;
		}

		@Override
		public int compare(ExternalDeviceAppLog lhs, ExternalDeviceAppLog rhs) {
			int compareValue = lhs.fieldArray[mComparatorField]
					.compareTo(rhs.fieldArray[mComparatorField]);
			return mIsAsendingOrder ? compareValue : -compareValue;
		}
	}

}
