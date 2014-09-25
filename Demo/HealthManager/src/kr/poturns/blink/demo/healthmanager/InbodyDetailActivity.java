package kr.poturns.blink.demo.healthmanager;

import kr.poturns.blink.demo.healthmanager.schema.InbodyDomain;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 
 * @author Ho.Kwon
 * @since 2014.09.23
 * 
 *
 */
public class InbodyDetailActivity extends ListActivity {
    
    InbodyDomain mInbodyDomain = new InbodyDomain(); // InbodyList를 관리할 ArrayList.
    InbodyDetailAdapter adapter; // ListView 관리용  Adapter

    ImageView bodytypeImage;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_inbody_detail_list);
        
        bodytypeImage = (ImageView)findViewById(R.id.bodytypeimage);
        if(mInbodyDomain.type.equals("비만형")){
        	bodytypeImage.setImageResource(R.drawable.fatperson_white);
        }else if(mInbodyDomain.type.equals("평균형")){
        	bodytypeImage.setImageResource(R.drawable.avgperson_white);
        }else if(mInbodyDomain.type.equals("근육형")){
        	bodytypeImage.setImageResource(R.drawable.musclebodytype);
        }
        
        adapter = new InbodyDetailAdapter(this,mInbodyDomain); // 동적 리스트 관리 Adapter
        setListAdapter(adapter);
        
    }


}