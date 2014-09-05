package kr.poturns.blink.db.archive;

import org.json.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * 외부 디바이스에서 데이터를 검색할 때 보내는 메시지
 * Class를 통해 검색할 때와 ID (MeasurementId)를 통해 검색할 때 두 가지 경우를 갖는다.
 * @author Jiwon
 *
 */
public class DatabaseMessage {
	public static final int OBTAIN_DATA_BY_CLASS = 0x00;
	public static final int OBTAIN_DATA_BY_ID = 0x01;
	public static final int SYNC_BLINKAPP = 0x02;
	public static final int SYNC_MEASUREMENT = 0x03;
	
	private int Type;
	private JsonObject mJsonObject;
	
	private DatabaseMessage(){
		Type = 0;
		mJsonObject = new JsonObject();
	}
	
	public int getType() {
	    return Type;
    }

	public String getCondition() {
		return mJsonObject.get("Condition").getAsString();
    }

	public String getDateTimeTo() {
		return mJsonObject.get("DateTimeTo").getAsString();
    }

	public String getDateTimeFrom() {
		return mJsonObject.get("DateTimeFrom").getAsString();
    }

	public int getContainType() {
		return mJsonObject.get("ContainType").getAsInt();
    }

	public String getData() {
		return mJsonObject.get("data").getAsString();
    }

	public static class Builder {

		private DatabaseMessage mDatabaseMessage;
		private JsonObject mJsonObject;
		public Builder() {
			mDatabaseMessage = new DatabaseMessage();
			mJsonObject = mDatabaseMessage.mJsonObject;
			
		}
		
		public Builder setType(int type) {
			if (type == OBTAIN_DATA_BY_CLASS || type == OBTAIN_DATA_BY_ID )
				mDatabaseMessage.Type = type;
			return this;
		}
		
		public Builder setCondition(String Condition) {
			if (Condition != null)mJsonObject.addProperty("Condition", Condition);
			return this;
		}
		
		public Builder setDateTimeFrom(String DateTimeFrom) {
			if (DateTimeFrom != null)mJsonObject.addProperty("DateTimeFrom", DateTimeFrom);
			return this;
		}
		
		public Builder setDateTimeTo(String DateTimeTo) {
			if (DateTimeTo != null)mJsonObject.addProperty("DateTimeTo", DateTimeTo);
			return this;
		}
		
		public Builder setContainType(int ContainType) {
			if (0 <= ContainType && ContainType <= 2)mJsonObject.addProperty("ContainType", ContainType);
			return this;
		}
		public Builder setData(String data) {
			if (data!=null)mJsonObject.addProperty("data", data);
			return this;
	    }
		public DatabaseMessage build() {
			return mDatabaseMessage;
		}
	}
}
