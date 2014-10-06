package kr.poturns.blink.schema;

/**
 * 디폴트 스키마로 기본적인 데이터 생성 시간인 DateTime을 필드로 가지고 있다. <br>
 * 
 * <br>
 * schema 클래스로 사용하기 위해서는 이 클래스를 구현해야 한다.
 * 
 * @author Jiwon
 * @author Myungjin
 * 
 * 
 */
public interface DefaultSchema {
	/**
	 * MeasurementData가 생성된 시점을 반환한다.<br>
	 * <br>
	 * 
	 * 형식은 "yyyy-MM-dd kk:mm:ss"이다.
	 */
	public String obtainDateTime();

	/** DateTime이 설정된다. */
	public void setDateTime(String dateTime);

	/**
	 * DefaultSchema가 기본적으로 구현된 클래스 <br>
	 * {@code DateTime}이라는 String 변수를 가진다. <br>
	 * <br>
	 * 
	 */
	public static abstract class Base implements DefaultSchema {
		public String DateTime;
		private static final String DATETIME_FORMAT = "^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$";

		@Override
		public final String obtainDateTime() {
			if (DateTime == null || !DateTime.matches(DATETIME_FORMAT))
				DateTime = android.text.format.DateFormat.format(
						"yyyy-MM-dd kk:mm:ss", System.currentTimeMillis())
						.toString();
			return DateTime;
		}

		@Override
		public final void setDateTime(String dateTime) {
			this.DateTime = dateTime;
		}
	}
}
