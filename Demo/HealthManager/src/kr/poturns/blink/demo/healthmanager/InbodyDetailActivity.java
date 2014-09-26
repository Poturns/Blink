package kr.poturns.blink.demo.healthmanager;

import kr.poturns.blink.schema.Inbody;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
/**
 * 
 * @author Ho.Kwon
 * @since 2014.09.23
 * 
 *
 */
public class InbodyDetailActivity extends ListActivity {
    
    Inbody mInbody = new Inbody(); // InbodyList를 관리할 ArrayList.
    InbodyDetailAdapter adapter; // ListView 관리용  Adapter

    ImageView bodytypeImage;
    Gson gson = new Gson();
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_inbody_detail_list);
        
        mInbody = gson.fromJson(getIntent().getStringExtra("Inbody"), Inbody.class);
        
        bodytypeImage = (ImageView)findViewById(R.id.bodytypeimage);
        if(mInbody.type.equals("비만형")){
        	bodytypeImage.setImageResource(R.drawable.fatperson_white);
        }else if(mInbody.type.equals("평균형")){
        	bodytypeImage.setImageResource(R.drawable.avgperson_white);
        }else if(mInbody.type.equals("근육형")){
        	bodytypeImage.setImageResource(R.drawable.musclebodytype);
        }
        
        adapter = new InbodyDetailAdapter(this,mInbody); // 동적 리스트 관리 Adapter
        setListAdapter(adapter);
        
    }


}