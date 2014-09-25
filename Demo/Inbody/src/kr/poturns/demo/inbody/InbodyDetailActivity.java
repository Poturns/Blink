package kr.poturns.blink.demo.healthmanager;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
/**
 * 
 * @author Ho.Kwon
 * @since 2014.09.23
 * 
 *
 */
public class InbodyDetailActivity extends ListActivity {
    
    ArrayList<InbodyListDomain> listItems=new ArrayList<InbodyListDomain>(); // InbodyList를 관리할 ArrayList.


    InbodyDetailAdapter adapter; // ListView 관리용  Adapter


    Button button_fat;
    Button button_avg;
    Button button_muscle; // 각 체형 별 데이터 발생 버튼
    
    ImageView bodytypeImage;
    TextView bodytypeText;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_inbody_detail_list);
      //R.id\
     
    
        adapter = new InbodyDetailAdapter(this,listItems); // 동적 리스트 관리 Adapter
        setListAdapter(adapter);
        
        bodytypeImage = (ImageView)findViewById(R.id.bodytypeimage);
        bodytypeText = (TextView)findViewById(R.id.bodytypetext);
        
        button_fat = (Button)findViewById(R.id.fatbodytypeBtn);
        button_muscle = (Button)findViewById(R.id.musclebodytypeBtn);
        button_avg = (Button)findViewById(R.id.avgbodytypeBtn);
       
        button_fat.setOnClickListener(new Button.OnClickListener(){ // fat person inbody 정보 발생.

			@Override
			public void onClick(View arg0) {
				Log.i("auction", "click!!");
				// TODO Auto-generated method stub
				bodytypeImage.setImageResource(R.drawable.fatperson_white);
				bodytypeText.setText("체형 : 비만형");
				 listItems.clear();
				//  adapter.notifyDataSetChanged();
				  adapter.setBodyType("fat");
				for(int i=0; i<11; i++){ //
			     InbodyListDomain inbodyListDomain = new InbodyListDomain();
			        listItems.add(inbodyListDomain);
			        adapter.notifyDataSetChanged();
				}
			}
        	
        });
        button_avg.setOnClickListener(new Button.OnClickListener(){// average person inbody 정보 발생.

 			@Override
 			public void onClick(View arg0) {
 				Log.i("auction", "click!!");
 				bodytypeImage.setImageResource(R.drawable.avgperson_white);
 				bodytypeText.setText("체형 : 평균형");
 				 listItems.clear();
				 
				  adapter.setBodyType("avg");
 				// TODO Auto-generated method stub
 				for(int i=0; i<11; i++){
 			     InbodyListDomain inbodyListDomain = new InbodyListDomain();
 			        listItems.add(inbodyListDomain);
 			        adapter.notifyDataSetChanged();
 				}
 			}
         	
         });
        button_muscle.setOnClickListener(new Button.OnClickListener(){// muscle person inbody 정보 발생.

 			@Override
 			public void onClick(View arg0) {
 				Log.i("auction", "click!!");
 				 adapter.setBodyType("muscle");
 				bodytypeImage.setImageResource(R.drawable.musclebodytype);
 				bodytypeText.setText("체형 : 근육형");
 				// listItems=new ArrayList<InbodyListDomain>();
 				 listItems.clear();
 				// TODO Auto-generated method stub
 				for(int i=0; i<11; i++){
 			     InbodyListDomain inbodyListDomain = new InbodyListDomain();
 			        listItems.add(inbodyListDomain);
 			        adapter.notifyDataSetChanged();
 				}
 			}
         	
         });
    }


}