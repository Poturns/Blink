package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kr.poturns.blink.db.archive.BlinkLog;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * <li>
 * device -> {@link ExternalDeviceAppLog#FIELD_DEVICE}</li><br>
 * <li>app -> {@link ExternalDeviceAppLog#FIELD_APP}</li><br>
 * <li>type -> {@link ExternalDeviceAppLog#FIELD_TYPE}</li><br>
 * <li>content -> {@link ExternalDeviceAppLog#FIELD_CONTENT}</li><br>
 * <li>dateTime -> {@link ExternalDeviceAppLog#FIELD_DATETIME}</li>
 * 
 */
class ExternalDeviceAppLog implements Parcelable {
	public static final int FIELD_SIZE = 5;
	public static final int FIELD_DEVICE = 0;
	public static final int FIELD_APP = 1;
	public static final int FIELD_TYPE = 2;
	public static final int FIELD_CONTENT = 3;
	public static final int FIELD_DATETIME = 4;
	public static final int FIELD_NOT = -1;
	public String[] fieldArray = new String[FIELD_SIZE];

	public static final List<ExternalDeviceAppLog> convert(
			List<BlinkLog> logList) {
		ArrayList<ExternalDeviceAppLog> list = new ArrayList<ExternalDeviceAppLog>();
		for (BlinkLog log : logList) {
			list.add(new ExternalDeviceAppLog(log));
		}
		return list;
	}

	public ExternalDeviceAppLog() {
		for (int i = 0; i < FIELD_SIZE; i++)
			fieldArray[i] = "";
	}

	public ExternalDeviceAppLog(BlinkLog log) {
		fieldArray[FIELD_DEVICE] = log.Device;
		fieldArray[FIELD_TYPE] = String.valueOf(log.Type);
		fieldArray[FIELD_APP] = log.App;
		fieldArray[FIELD_CONTENT] = log.Content;
		fieldArray[FIELD_DATETIME] = log.DateTime;
	}

	public ExternalDeviceAppLog(String device, String app, String type,
			String content, String dateTime) {
		fieldArray[FIELD_DEVICE] = device;
		fieldArray[FIELD_APP] = app;
		fieldArray[FIELD_TYPE] = type;
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
	 *            device -> {@link ExternalDeviceAppLog#FIELD_DEVICE}</li><br>
	 *            <li>app -> {@link ExternalDeviceAppLog#FIELD_APP}</li><br>
	 *            <li>type -> {@link ExternalDeviceAppLog#FIELD_TYPE}</li><br>
	 *            <li>content -> {@link ExternalDeviceAppLog#FIELD_CONTENT}</li><br>
	 *            <li>dateTime -> {@link ExternalDeviceAppLog#FIELD_DATETIME}</li>
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
	 * 
	 * @return <b>order</b>에 적절한 <li>{@link ExternalDeviceAppLog#FIELD_DEVICE}</li>
	 * <br>
	 *         <li>{@link ExternalDeviceAppLog#FIELD_APP}</li><br>
	 *         <li>{@link ExternalDeviceAppLog#FIELD_TYPE}</li><br>
	 *         <li>{@link ExternalDeviceAppLog#FIELD_CONTENT}</li><br>
	 *         <li>{@link ExternalDeviceAppLog#FIELD_DATETIME}</li> 중 하나,<br>
	 *         또는 {@link ExternalDeviceAppLog#FIELD_NOT}
	 */
	public static int getFieldConstantByOrder(int order) {
		if (order < 0 || order > FIELD_SIZE) {
			return -1;
		}
		switch (order) {
		case 0:
			return FIELD_DEVICE;
		case 1:
			return FIELD_APP;
		case 2:
			return FIELD_TYPE;
		case 3:
			return FIELD_CONTENT;
		case 4:
			return FIELD_DATETIME;
		default:
			return FIELD_NOT;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String[] array = new String[] { "Device : ", "App : ", "Type : ",
				"Content : ", "DateTime : " };
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
		/** 현재 정렬하려는 Field */
		int mComparatorField;
		/** 현재 정렬하는 순서가 오름차순 인지 여부 */
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
