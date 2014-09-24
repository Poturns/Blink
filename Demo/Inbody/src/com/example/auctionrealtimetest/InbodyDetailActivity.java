package com.example.auctionrealtimetest;

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

public class InbodyDetailActivity extends ListActivity {
    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<InbodyListDomain> listItems=new ArrayList<InbodyListDomain>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    CustomBaseAdapter adapter;

    //RECORDING HOW MANY TIMES THE BUTTON HAS BEEN CLICKED
    int clickCounter=0;
    Button button_fat;
    Button button_avg;
    Button button_muscle;
    ImageView bodytypeImage;
    TextView bodytypeText;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_auction_list);
      //R.id\
     
    
        adapter = new CustomBaseAdapter(this,listItems);
        /*
        adapter=new Adapter<InbodyListDomain>(this,
            R.layout.activity_auction_list,
            listItems);*/
        setListAdapter(adapter);
        bodytypeImage = (ImageView)findViewById(R.id.bodytypeimage);
        bodytypeText = (TextView)findViewById(R.id.bodytypetext);
        
        button_fat = (Button)findViewById(R.id.fatbodytypeBtn);
        button_muscle = (Button)findViewById(R.id.musclebodytypeBtn);
        button_avg = (Button)findViewById(R.id.avgbodytypeBtn);
       
        button_fat.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.i("auction", "click!!");
				// TODO Auto-generated method stub
				bodytypeImage.setImageResource(R.drawable.fatperson_white);
				bodytypeText.setText("체형 : 비만형");
				 listItems.clear();
				//  adapter.notifyDataSetChanged();
				  adapter.setBodyType("fat");
				for(int i=0; i<11; i++){
			     InbodyListDomain inbodyListDomain = new InbodyListDomain();
			        listItems.add(inbodyListDomain);
			        adapter.notifyDataSetChanged();
				}
			}
        	
        });
        button_avg.setOnClickListener(new Button.OnClickListener(){

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
        button_muscle.setOnClickListener(new Button.OnClickListener(){

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
 			    inbodyListDomain.setAuctionItemEndFlag("종료");
 		        inbodyListDomain.setAuctionItemName("아기용품");
 		        inbodyListDomain.setAuctionItemPrice("12000");
 			        listItems.add(inbodyListDomain);
 			        adapter.notifyDataSetChanged();
 				}
 			}
         	
         });
    }

    //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
    public void add(View v) {
    	//Intent intent = new Intent(AuctionListActivity.this, AuctionAddActivty.class);
    	
    	//startActivity(intent);
        InbodyListDomain inbodyListDomain = new InbodyListDomain();
        inbodyListDomain.setAuctionItemEndFlag("종료");
        inbodyListDomain.setAuctionItemName("아기용품");
        inbodyListDomain.setAuctionItemPrice("12000");
        listItems.add(inbodyListDomain);
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    //	listItems.clear();
    //	adapter.notifyDataSetChanged();
    	//여기서 항상 clear하고 db에서 불러와야겠네
    }
}